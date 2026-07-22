from unittest.mock import MagicMock, patch
import pytest
from negotiation_graph import NegotiationState, issuer_node, acquirer_node, reconciler_node, negotiation_workflow

def test_issuer_node():
    state: NegotiationState = {
        "issuer_clauses": {"key": "val1"},
        "acquirer_clauses": {"key": "val2"},
        "issuer_response": "",
        "acquirer_response": "",
        "final_clauses": []
    }
    with patch("negotiation_graph.issuer_agent") as mock_agent:
        mock_agent.return_value = "issuer mock reply"
        res = issuer_node(state)
        assert res["issuer_response"] == "issuer mock reply"
        mock_agent.assert_called_once_with({"key": "val1"}, {"key": "val2"})

def test_acquirer_node():
    state: NegotiationState = {
        "issuer_clauses": {"key": "val1"},
        "acquirer_clauses": {"key": "val2"},
        "issuer_response": "",
        "acquirer_response": "",
        "final_clauses": []
    }
    with patch("negotiation_graph.acquirer_agent") as mock_agent:
        mock_agent.return_value = "acquirer mock reply"
        res = acquirer_node(state)
        assert res["acquirer_response"] == "acquirer mock reply"
        mock_agent.assert_called_once_with({"key": "val1"}, {"key": "val2"})

def test_reconciler_node():
    state: NegotiationState = {
        "issuer_clauses": {"key": "val1"},
        "acquirer_clauses": {"key": "val2"},
        "issuer_response": "issuer mock reply",
        "acquirer_response": "acquirer mock reply",
        "final_clauses": []
    }
    with patch("negotiation_graph.reconciliation_agent") as mock_agent:
        mock_agent.return_value = [{"clause_key": "some_clause"}]
        res = reconciler_node(state)
        assert res["final_clauses"] == [{"clause_key": "some_clause"}]
        mock_agent.assert_called_once_with(
            {"key": "val1"}, {"key": "val2"}, "issuer mock reply", "acquirer mock reply"
        )

def test_negotiation_workflow():
    state: NegotiationState = {
        "issuer_clauses": {"key": "val1"},
        "acquirer_clauses": {"key": "val2"},
        "issuer_response": "",
        "acquirer_response": "",
        "final_clauses": []
    }
    with patch("negotiation_graph.issuer_agent") as mock_issuer, \
         patch("negotiation_graph.acquirer_agent") as mock_acquirer, \
         patch("negotiation_graph.reconciliation_agent") as mock_reconciliation:
        
        mock_issuer.return_value = "issuer feedback"
        mock_acquirer.return_value = "acquirer feedback"
        mock_reconciliation.return_value = [{"clause_key": "payment", "status": "agreed"}]
        
        result = negotiation_workflow.invoke(state)
        assert result["issuer_response"] == "issuer feedback"
        assert result["acquirer_response"] == "acquirer feedback"
        assert result["final_clauses"] == [{"clause_key": "payment", "status": "agreed"}]
