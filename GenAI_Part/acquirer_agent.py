import os
from dotenv import load_dotenv
from langchain_groq import ChatGroq


load_dotenv()

llm = ChatGroq(
    model="llama-3.3-70b-versatile",
    api_key=os.getenv("GROQ_API_KEY"),
    temperature=0
)

def acquirer_agent(issuer_clauses, acquirer_clauses):
    prompt = f"""
You are the Acquirer's AI Negotiation Agent.

Your goal is to negotiate in favor of the Acquirer while still trying to reach a fair agreement.

Issuer Clauses:
{issuer_clauses}

Acquirer Clauses:
{acquirer_clauses}

For each clause:
1. Compare both values.
2. State whether you ACCEPT, REJECT, or COUNTER.
3. If COUNTER, suggest a better value for the Acquirer.

Return ONLY in this format:

Payment Terms:
Decision:
Reason:
Counter Proposal:

Delivery:
Decision:
Reason:
Counter Proposal:

Warranty:
Decision:
Reason:
Counter Proposal:

Confidentiality:
Decision:
Reason:
Counter Proposal:

Intellectual Property:
Decision:
Reason:
Counter Proposal:

Liability:
Decision:
Reason:
Counter Proposal:

Indemnification:
Decision:
Reason:
Counter Proposal:

Termination:
Decision:
Reason:
Counter Proposal:

Late Payment Penalty:
Decision:
Reason:
Counter Proposal:

Governing Law:
Decision:
Reason:
Counter Proposal:

Force Majeure:
Decision:
Reason:
Counter Proposal:

Final Acquirer Position:
"""

    response = llm.invoke(prompt)

    return response.content