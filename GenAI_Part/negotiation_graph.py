from typing import TypedDict
from langgraph.graph import StateGraph, END

from issuer_agent import issuer_agent
from acquirer_agent import acquirer_agent
from reconciliation_agent import reconciliation_agent


class NegotiationState(TypedDict):
    issuer_clauses: dict
    acquirer_clauses: dict
    issuer_response: str
    acquirer_response: str
    final_clauses: list


def issuer_node(state: NegotiationState):
    response = issuer_agent(
        state["issuer_clauses"],
        state["acquirer_clauses"]
    )

    state["issuer_response"] = response
    return state


def acquirer_node(state: NegotiationState):
    response = acquirer_agent(
        state["issuer_clauses"],
        state["acquirer_clauses"]
    )

    state["acquirer_response"] = response
    return state


def reconciler_node(state: NegotiationState):
    final_clauses = reconciliation_agent(
        state["issuer_clauses"],
        state["acquirer_clauses"],
        state["issuer_response"],
        state["acquirer_response"]
    )

    state["final_clauses"] = final_clauses
    return state


graph = StateGraph(NegotiationState)

graph.add_node("issuer", issuer_node)
graph.add_node("acquirer", acquirer_node)
graph.add_node("reconciler", reconciler_node)

graph.set_entry_point("issuer")

graph.add_edge("issuer", "acquirer")
graph.add_edge("acquirer", "reconciler")
graph.add_edge("reconciler", END)

negotiation_workflow = graph.compile()