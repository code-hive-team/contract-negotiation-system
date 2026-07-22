import os
import json
from dotenv import load_dotenv
from langchain_groq import ChatGroq


load_dotenv()


llm = ChatGroq(
    model="llama-3.1-8b-instant",
    api_key=os.getenv("GROQ_API_KEY"),
    temperature=0
)

def reconciliation_agent(issuer_clauses, acquirer_clauses, issuer_response, acquirer_response):
    prompt = f"""
You are the Final Contract Reconciler and Negotiation Agent.
Your job is to compare the original clauses suggested by the Issuer and Acquirer, along with their respective negotiation feedback/counter-proposals, and formulate a final, fair contract compromise for each clause.

Input Details:
- Issuer Clauses (Original):
{json.dumps(issuer_clauses, indent=2)}

- Acquirer Clauses (Original):
{json.dumps(acquirer_clauses, indent=2)}

- Issuer Agent Feedback & Counters:
{issuer_response}

- Acquirer Agent Feedback & Counters:
{acquirer_response}

Instructions:
1. For each clause (e.g. payment_terms, delivery, warranty, confidentiality, intellectual_property, liability, indemnification, late_payment_penalty, termination, governing_law, force_majeure):
   - Compare the original Issuer and Acquirer positions.
   - Review their negotiation arguments and counter-proposals.
   - Decide a final reconciled term/compromise:
     - For numerical/duration terms (like payment terms: 30 days vs 45 days, or warranty: 12 months vs 24 months), calculate a reasonable middle-ground value (e.g., 40 days, 18 months).
     - For clauses where one party accepted the other's version with no/minimal friction (like governing law or confidentiality), set the status to "agreed".
     - For clauses with differing terms that were modified/compromised, set the status to "modified".
     - If they are completely irreconcilable (which is rare), set the status to "rejected".
   - Determine the display representation:
     - Use display_type = "detailed" if the clause is numerical, has a duration, or has a distinct financial value where a direct three-way comparison (Issuer vs Acquirer vs Final) is highly valuable.
     - Use display_type = "simple" if it is a text-based clause, legal boilerplate, or agreed term best represented with a single resolution status (e.g. "Status: Accepted without changes").
     - If display_type is "detailed", ensure issuer_value, acquirer_value, and final_value are populated.
     - If display_type is "simple", ensure status_text is populated (e.g., "Accepted without changes", "Accepted with minor modification").

2. Return ONLY a valid JSON array of objects representing all the processed clauses. Do not output any thinking or extra text.

The return format MUST be a JSON array of objects like this:
[
  {{
    "clause_key": "payment_terms",
    "clause_name": "Payment Terms",
    "display_type": "detailed",
    "issuer_value": "30 Days",
    "acquirer_value": "45 Days",
    "final_value": "40 Days",
    "status": "modified",
    "status_text": "Accepted with compromise"
  }},
  {{
    "clause_key": "confidentiality",
    "clause_name": "Confidentiality",
    "display_type": "simple",
    "issuer_value": null,
    "acquirer_value": null,
    "final_value": null,
    "status": "agreed",
    "status_text": "Accepted without changes"
  }}
]
"""

    response = llm.invoke(prompt)
    content = response.content.strip()

    # Clean markdown formatting if present
    if content.startswith("```json"):
        content = content.replace("```json", "", 1)
    if content.startswith("```"):
        content = content.replace("```", "", 1)
    if content.endswith("```"):
        content = content[:-3]
    content = content.strip()

    try:
        data = json.loads(content)
        return data
    except json.JSONDecodeError as e:
        print(f"Error parsing reconciler JSON: {e}")
        print("Raw response was:")
        print(content)
        return []
