import os
import json
from dotenv import load_dotenv
from langchain_groq import ChatGroq

load_dotenv()

llm = ChatGroq(
    model="llama-3.3-70b-versatile",
    api_key=os.getenv("GROQ_API_KEY"),
    temperature=0
)

def classify_contract(contract_text):

    prompt = f"""
You are a legal contract analyzer.

Extract the following clauses from the contract.

Return ONLY JSON.

{{
    "payment_terms":"",
    "delivery":"",
    "warranty":"",
    "confidentiality":"",
    "intellectual_property":"",
    "liability":"",
    "indemnification":"",
    "late_payment_penalty":"",
    "termination":"",
    "governing_law":"",
    "force_majeure":""
}}

Contract:

{contract_text}
"""

    response = llm.invoke(prompt)

    content = response.content.strip()


    if content.startswith("```json"):
        content = content.replace("```json", "", 1)

    if content.startswith("```"):
        content = content.replace("```", "", 1)

    if content.endswith("```"):
        content = content[:-3]

    content = content.strip()

    return json.loads(content)