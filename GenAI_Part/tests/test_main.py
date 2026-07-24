import io
import sys
import pytest
from unittest.mock import patch, MagicMock
from main import clean_val, extract_text, print_classification_table, print_negotiation_report

def test_clean_val():
    assert clean_val(None) == "N/A"
    assert clean_val("₹1500") == "Rs. 1500"
    assert clean_val("■1000") == "Rs. 1000"
    assert clean_val("ordinary text") == "ordinary text"

def test_main_extract_text():
    with patch("utils.PdfReader") as mock_pdf_reader:
        mock_page = MagicMock()
        mock_page.extract_text.return_value = "Main page text"
        
        mock_pdf = MagicMock()
        mock_pdf.pages = [mock_page]
        mock_pdf_reader.return_value = mock_pdf
        
        res = extract_text("dummy.pdf")
        assert res == "Main page text"

def test_main_extract_text_exception():
    with patch("utils.PdfReader", side_effect=ValueError("Error")):
        res = extract_text("fail.pdf")
        assert res == ""

def test_print_classification_table():
    issuer = {"terms": "val1"}
    acquirer = {"terms": "val2", "other": "val3"}
    
    captured_output = io.StringIO()
    sys.stdout = captured_output
    try:
        print_classification_table(issuer, acquirer)
    finally:
        sys.stdout = sys.__stdout__
        
    out = captured_output.getvalue()
    assert "Terms" in out
    assert "Other" in out
    assert "val1" in out
    assert "val2" in out

def test_print_negotiation_report():
    clauses = [
        {
            "clause_name": "Terms",
            "display_type": "detailed",
            "issuer_value": "30 days",
            "acquirer_value": "45 days",
            "final_value": "40 days",
            "status": "modified",
            "status_text": "Modified terms"
        },
        {
            "clause_name": "NDA",
            "display_type": "simple",
            "status": "agreed",
            "status_text": "Accepted NDA"
        }
    ]
    
    captured_output = io.StringIO()
    sys.stdout = captured_output
    try:
        print_negotiation_report(clauses)
    finally:
        sys.stdout = sys.__stdout__
        
    out = captured_output.getvalue()
    assert "Terms" in out
    assert "Issuer : 30 days" in out
    assert "Acquirer : 45 days" in out
    assert "Final : 40 days" in out
    assert "NDA" in out
    assert "Status : Accepted NDA" in out
    assert "Negotiation Summary" in out

def test_main_execution_flow(tmp_path):
    out_file = str(tmp_path / "out.json")
    test_args = ["main.py", "--issuer", "iss.pdf", "--acquirer", "acq.pdf", "--output", out_file]
    with patch("sys.argv", test_args), \
         patch("main.extract_text") as mock_extract, \
         patch("os.path.exists", return_value=True), \
         patch("os.path.samefile", return_value=False), \
         patch("main.classify_contract") as mock_classify, \
         patch("main.negotiation_workflow.invoke") as mock_workflow:
        
        mock_extract.side_effect = ["Issuer content", "Acquirer content"]
        mock_classify.side_effect = [
            {"payment_terms": "30"},
            {"payment_terms": "45"}
        ]
        mock_workflow.return_value = {
            "issuer_clauses": {"payment_terms": "30"},
            "acquirer_clauses": {"payment_terms": "45"},
            "issuer_response": "issuer feedback",
            "acquirer_response": "acquirer feedback",
            "final_clauses": [
                {
                    "clause_name": "Payment Terms",
                    "display_type": "detailed",
                    "issuer_value": "30",
                    "acquirer_value": "45",
                    "final_value": "40",
                    "status": "modified",
                    "status_text": "Modified"
                }
            ]
        }
        
        import main
        try:
            main.main()
        except SystemExit:
            pass
