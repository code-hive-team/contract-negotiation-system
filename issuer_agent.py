import os
from dotenv import load_dotenv
from langchain_groq import ChatGroq

# Load .env
load_dotenv()

# Initialize Groq LLM
llm = ChatGroq(
    model="llama-3.3-70b-versatile",
    api_key=os.getenv("GROQ_API_KEY"),
    temperature=0
)

def issuer_agent(issuer_clauses, acquirer_clauses):
    prompt = f"""
You are the Issuer's AI Negotiation Agent.

Your goal is to negotiate in favor of the Issuer while still trying to reach a fair agreement.

Issuer Clauses:
{issuer_clauses}

Acquirer Clauses:
{acquirer_clauses}

For each clause:
1. Compare both values.
2. State whether you ACCEPT, REJECT, or COUNTER.
3. If COUNTER, suggest a better value for the Issuer.

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

Final Issuer Position:
"""

    response = llm.invoke(prompt)

    return response.content