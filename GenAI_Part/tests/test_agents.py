from unittest.mock import MagicMock
import json
import pytest
from acquirer_agent import acquirer_agent
from issuer_agent import issuer_agent
from reconciliation_agent import reconciliation_agent

def test_acquirer_agent():
    mock_response = MagicMock()
    mock_response.content = "Acquirer Feedback content"
    
    import acquirer_agent as aa
    original_ll = aa.llm
    aa.llm = MagicMock()
    aa.llm.invoke.return_value = mock_response
    
    try:
        res = acquirer_agent({"payment": "30"}, {"payment": "45"})
        assert res == "Acquirer Feedback content"
        aa.llm.invoke.assert_called_once()
    finally:
        aa.llm = original_ll

def test_issuer_agent():
    mock_response = MagicMock()
    mock_response.content = "Issuer Feedback content"
    
    import issuer_agent as ia
    original_ll = ia.llm
    ia.llm = MagicMock()
    ia.llm.invoke.return_value = mock_response
    
    try:
        res = issuer_agent({"payment": "30"}, {"payment": "45"})
        assert res == "Issuer Feedback content"
        ia.llm.invoke.assert_called_once()
    finally:
        ia.llm = original_ll

def test_reconciliation_agent_success():
    mock_response = MagicMock()
    mock_response.content = """
    ```json
    [
      {
        "clause_key": "payment_terms",
        "clause_name": "Payment Terms",
        "display_type": "detailed",
        "issuer_value": "30 Days",
        "acquirer_value": "45 Days",
        "final_value": "40 Days",
        "status": "modified",
        "status_text": "Accepted with compromise"
      }
    ]
    ```
    """
    
    import reconciliation_agent as ra
    original_ll = ra.llm
    ra.llm = MagicMock()
    ra.llm.invoke.return_value = mock_response
    
    try:
        res = reconciliation_agent({"pay": "30"}, {"pay": "45"}, "issuer response", "acquirer response")
        assert len(res) == 1
        assert res[0]["clause_key"] == "payment_terms"
        assert res[0]["final_value"] == "40 Days"
        ra.llm.invoke.assert_called_once()
    finally:
        ra.llm = original_ll

def test_reconciliation_agent_parse_error():
    mock_response = MagicMock()
    mock_response.content = "not a valid json"
    
    import reconciliation_agent as ra
    original_ll = ra.llm
    ra.llm = MagicMock()
    ra.llm.invoke.return_value = mock_response
    
    try:
        res = reconciliation_agent({"pay": "30"}, {"pay": "45"}, "issuer response", "acquirer response")
        assert res == []
        ra.llm.invoke.assert_called_once()
    finally:
        ra.llm = original_ll
