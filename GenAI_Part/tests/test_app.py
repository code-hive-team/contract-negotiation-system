import io
import pytest
from unittest.mock import patch, MagicMock
from fastapi.testclient import TestClient
from app import app, extract_text

client = TestClient(app)

def create_mock_file(name="issuer.pdf", content=b"dummy"):
    return (name, io.BytesIO(content), "application/pdf")

def test_extract_text_success():
    # Test extract_text with mock PdfReader
    with patch("app.PdfReader") as mock_pdf_reader:
        mock_page = MagicMock()
        mock_page.extract_text.return_value = "Page content"
        
        mock_pdf = MagicMock()
        mock_pdf.pages = [mock_page]
        mock_pdf_reader.return_value = mock_pdf
        
        text = extract_text("dummy_path.pdf")
        assert text == "Page content"

def test_extract_text_exception():
    with patch("app.PdfReader", side_effect=RuntimeError("Read fail")):
        # Since PdfReader(pdf_path) raises exception, let's test it raises HTTPException
        from fastapi import HTTPException
        with pytest.raises(HTTPException):
            extract_text("dummy_path.pdf")

def test_negotiate_contracts_success():
    # Setup files
    file1 = create_mock_file("issuer.pdf", b"dummy pdf content 1")
    file2 = create_mock_file("acquirer.pdf", b"dummy pdf content 2")
    
    with patch("app.extract_text") as mock_extract, \
         patch("app.classify_contract") as mock_classify, \
         patch("app.negotiation_workflow.invoke") as mock_workflow_invoke, \
         patch("filecmp.cmp", return_value=False):
        
        # Setup mock behavior
        mock_extract.side_effect = [
            "This is the Issuer Company test text...",
            "This is the Acquirer Company test text..."
        ]
        
        mock_classify.side_effect = [
            {"payment_terms": "30 days"},
            {"payment_terms": "45 days"}
        ]
        
        mock_workflow_invoke.return_value = {
            "issuer_clauses": {"payment_terms": "30 days"},
            "acquirer_clauses": {"payment_terms": "45 days"},
            "issuer_response": "issuer feedback",
            "acquirer_response": "acquirer feedback",
            "final_clauses": [
                {
                    "clause_key": "payment_terms",
                    "clause_name": "Payment Terms",
                    "display_type": "detailed",
                    "issuer_value": "30 days",
                    "acquirer_value": "45 days",
                    "final_value": "40 days",
                    "status": "modified",
                    "status_text": "Accepted with compromise"
                }
            ]
        }
        
        response = client.post(
            "/api/negotiate",
            files={"issuer_file": file1, "acquirer_file": file2}
        )
        
        assert response.status_code == 200
        data = response.json()
        assert data["status"] == "success"
        assert data["summary"]["clauses_modified"] == 1
        assert data["summary"]["negotiation_status"] == "SUCCESS"

def test_negotiate_contracts_identical_files():
    file1 = create_mock_file("issuer.pdf", b"same content")
    file2 = create_mock_file("acquirer.pdf", b"same content")
    
    # We force filecmp.cmp to return True to simulate identical files check
    with patch("filecmp.cmp", return_value=True):
        response = client.post(
            "/api/negotiate",
            files={"issuer_file": file1, "acquirer_file": file2}
        )
        assert response.status_code == 400
        assert "cannot be the same file" in response.json()["detail"]

def test_negotiate_contracts_swapped_issuer():
    file1 = create_mock_file("issuer.pdf")
    file2 = create_mock_file("acquirer.pdf")
    
    with patch("app.extract_text") as mock_extract, \
         patch("filecmp.cmp", return_value=False):
        
        mock_extract.side_effect = [
            "This contract contains Acquirer Company Contract keyword",
            "Valid acquirer text"
        ]
        
        response = client.post(
            "/api/negotiate",
            files={"issuer_file": file1, "acquirer_file": file2}
        )
        assert response.status_code == 400
        assert "Swapped upload detected. The Acquirer contract was uploaded" in response.json()["detail"]

def test_negotiate_contracts_swapped_acquirer():
    file1 = create_mock_file("issuer.pdf")
    file2 = create_mock_file("acquirer.pdf")
    
    with patch("app.extract_text") as mock_extract, \
         patch("filecmp.cmp", return_value=False):
        
        mock_extract.side_effect = [
            "Valid issuer text",
            "This contract contains Issuer Company Contract keyword"
        ]
        
        response = client.post(
            "/api/negotiate",
            files={"issuer_file": file1, "acquirer_file": file2}
        )
        assert response.status_code == 400
        assert "Swapped upload detected. The Issuer contract was uploaded" in response.json()["detail"]

def test_negotiate_contracts_empty_issuer():
    file1 = create_mock_file("issuer.pdf")
    file2 = create_mock_file("acquirer.pdf")
    
    with patch("app.extract_text") as mock_extract, \
         patch("filecmp.cmp", return_value=False):
        
        mock_extract.side_effect = [
            "",
            "Valid acquirer text"
        ]
        
        response = client.post(
            "/api/negotiate",
            files={"issuer_file": file1, "acquirer_file": file2}
        )
        assert response.status_code == 400
        assert "Issuer PDF contains no extractable text" in response.json()["detail"]

def test_negotiate_contracts_empty_acquirer():
    file1 = create_mock_file("issuer.pdf")
    file2 = create_mock_file("acquirer.pdf")
    
    with patch("app.extract_text") as mock_extract, \
         patch("filecmp.cmp", return_value=False):
        
        mock_extract.side_effect = [
            "Valid issuer text",
            ""
        ]
        
        response = client.post(
            "/api/negotiate",
            files={"issuer_file": file1, "acquirer_file": file2}
        )
        assert response.status_code == 400
        assert "Acquirer PDF contains no extractable text" in response.json()["detail"]

def test_negotiate_contracts_workflow_exception():
    file1 = create_mock_file("issuer.pdf")
    file2 = create_mock_file("acquirer.pdf")
    
    with patch("app.extract_text") as mock_extract, \
         patch("app.classify_contract") as mock_classify, \
         patch("app.negotiation_workflow.invoke", side_effect=RuntimeError("Workflow execution failed")), \
         patch("filecmp.cmp", return_value=False):
        
        mock_extract.side_effect = [
            "Valid issuer text",
            "Valid acquirer text"
        ]
        mock_classify.side_effect = [
            {"payment_terms": "30 days"},
            {"payment_terms": "45 days"}
        ]
        
        response = client.post(
            "/api/negotiate",
            files={"issuer_file": file1, "acquirer_file": file2}
        )
        assert response.status_code == 500
        assert "Workflow execution failed" in response.json()["detail"]
