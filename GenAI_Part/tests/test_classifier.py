from unittest.mock import MagicMock
import pytest
from classifier import classify_contract

def test_classify_contract():
    # Mock LLM and response
    mock_response = MagicMock()
    mock_response.content = """
    ```json
    {
        "payment_terms": "30 days net",
        "delivery": "FOB Destination",
        "warranty": "12 months",
        "confidentiality": "Standard NDA",
        "intellectual_property": "Seller retains all rights",
        "liability": "Limited to contract value",
        "indemnification": "Mutual indemnification",
        "late_payment_penalty": "1.5% per month",
        "termination": "30 days notice",
        "governing_law": "New York",
        "force_majeure": "Standard force majeure"
    }
    ```
    """
    
    import classifier
    original_llm = classifier.llm
    classifier.llm = MagicMock()
    classifier.llm.invoke.return_value = mock_response
    
    try:
        result = classify_contract("Test contract text")
        
        assert result["payment_terms"] == "30 days net"
        assert result["delivery"] == "FOB Destination"
        assert result["governing_law"] == "New York"
        classifier.llm.invoke.assert_called_once()
    finally:
        classifier.llm = original_llm
