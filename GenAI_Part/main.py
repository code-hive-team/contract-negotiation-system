import os
import sys
import json
import argparse
from dotenv import load_dotenv

sys.stdout.reconfigure(encoding='utf-8')
import logging
from pypdf import PdfReader

# Suppress harmless pypdf warnings
logging.getLogger("pypdf").setLevel(logging.ERROR)

from classifier import classify_contract
from negotiation_graph import negotiation_workflow

load_dotenv()


def extract_text(pdf_path):
    """Extract text from a PDF file."""
    try:
        reader = PdfReader(pdf_path)
        text = ""

        for page in reader.pages:
            page_text = page.extract_text()
            if page_text:
                text += page_text + "\n"

        return text.strip()

    except Exception as e:
        print(f"Error reading PDF: {e}")
        return ""


import textwrap

def clean_val(val):
    """Sanitize currency and other unprintable unicode symbols for terminal safety."""
    if val is None:
        return "N/A"
    val = str(val)
    
    val = val.replace("₹", "Rs. ").replace("₨", "Rs. ").replace("₨ ", "Rs. ").replace("\u20b9", "Rs. ").replace("\u20a8", "Rs. ")
    if val.startswith("■"):
        val = "Rs. " + val[1:]
    val = val.replace("■", "")
    return val.strip()

def print_classification_table(issuer_clauses, acquirer_clauses):
    """Print classification results in a formatted table with text wrapping and cleaned unicode symbols."""
    all_keys = list(dict.fromkeys(list(issuer_clauses.keys()) + list(acquirer_clauses.keys())))

    def format_key(key):
        return key.replace("_", " ").title()

    clause_col = max(len(format_key(k)) for k in all_keys) + 2
    issuer_col = 40
    acquirer_col = 40

    sep = "+" + "-" * clause_col + "+" + "-" * issuer_col + "+" + "-" * acquirer_col + "+"

    print(sep)
    print(f"|{'CLAUSE':^{clause_col}}|{'ISSUER':^{issuer_col}}|{'ACQUIRER':^{acquirer_col}}|")
    print(sep)

    for key in all_keys:
        name = format_key(key)
        iv = clean_val(issuer_clauses.get(key, "N/A"))
        av = clean_val(acquirer_clauses.get(key, "N/A"))


        iv_wrapped = textwrap.wrap(iv, width=issuer_col - 2) or [""]
        av_wrapped = textwrap.wrap(av, width=acquirer_col - 2) or [""]
        name_wrapped = textwrap.wrap(name, width=clause_col - 2) or [""]

        max_lines = max(len(name_wrapped), len(iv_wrapped), len(av_wrapped))

        for i in range(max_lines):
            n_part = name_wrapped[i] if i < len(name_wrapped) else ""
            i_part = iv_wrapped[i] if i < len(iv_wrapped) else ""
            a_part = av_wrapped[i] if i < len(av_wrapped) else ""
            print(f"| {n_part:<{clause_col - 2}} | {i_part:<{issuer_col - 2}} | {a_part:<{acquirer_col - 2}} |")
        print(sep)


def print_negotiation_report(final_clauses):
    """Print the final negotiation report in the exact format shown in the image."""
    print("AUTONOMOUS CONTRACT NEGOTIATION REPORT")
    print("=" * 60)
    print("Contract Analysis Completed")
    total_clauses = len(final_clauses)
    print(f"Total Clauses Identified: {total_clauses}")
    print("-" * 60)
    print("Negotiation Results")
    print("-" * 60)

    agreed_count = 0
    modified_count = 0
    rejected_count = 0

    for clause in final_clauses:
        name = clause.get("clause_name")
        print(name)
        display_type = clause.get("display_type", "simple")
        if display_type == "detailed":
            issuer_v = clean_val(clause.get("issuer_value"))
            acquirer_v = clean_val(clause.get("acquirer_value"))
            final_v = clean_val(clause.get("final_value"))
            print(f"Issuer : {issuer_v}")
            print(f"Acquirer : {acquirer_v}")
            print(f"Final : {final_v}")
        else:
            status_t = clean_val(clause.get("status_text"))
            print(f"Status : {status_t}")
        print() 

        status = str(clause.get("status", "agreed")).lower()
        if status == "agreed":
            agreed_count += 1
        elif status == "modified":
            modified_count += 1
        else:
            rejected_count += 1

    print("-" * 60)
    print("Negotiation Summary")
    print("-" * 60)
    print(f"Clauses Agreed : {agreed_count}")
    print(f"Clauses Modified : {modified_count}")
    print(f"Clauses Rejected : {rejected_count}")
    print()
    status_str = "SUCCESS" if rejected_count == 0 else "FAILED"
    print(f"Negotiation Status : {status_str}")
    print("=" * 60)
    print("FINAL NEGOTIATED CONTRACT GENERATED")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Autonomous Contract Negotiation System GenAI Pipeline")
    parser.add_argument("--issuer", help="Path to the issuer contract PDF")
    parser.add_argument("--acquirer", help="Path to the acquirer contract PDF")
    parser.add_argument("--output", help="Path to save the negotiation results JSON")
    args = parser.parse_args()

    issuer_pdf = args.issuer
    acquirer_pdf = args.acquirer
    output_path = args.output

    if not issuer_pdf or not acquirer_pdf:
        # Fallback to interactive prompts if not provided via CLI args
        if not issuer_pdf:
            issuer_pdf = input("Upload Issuer Contract PDF: ").strip()
        if not acquirer_pdf:
            acquirer_pdf = input("Upload Acquirer Contract PDF: ").strip()

    if issuer_pdf and acquirer_pdf:
        try:
            if os.path.exists(issuer_pdf) and os.path.exists(acquirer_pdf) and os.path.samefile(issuer_pdf, acquirer_pdf):
                print("❌ Error: Issuer and Acquirer contracts cannot be the same file.")
                exit()
        except Exception:
            if os.path.abspath(issuer_pdf) == os.path.abspath(acquirer_pdf):
                print("❌ Error: Issuer and Acquirer contracts cannot be the same file.")
                exit()

    print("\nReading PDFs...\n")

    issuer_text = extract_text(issuer_pdf)
    acquirer_text = extract_text(acquirer_pdf)

    if not issuer_text:
        print("❌ Issuer PDF contains no extractable text.")
        exit()

    if not acquirer_text:
        print("❌ Acquirer PDF contains no extractable text.")
        exit()

    if "Acquirer Company Contract" in issuer_text:
        print("❌ Error: Swapped upload detected. The Acquirer contract was uploaded in place of the Issuer contract.")
        exit()
    if "Issuer Company Contract" in acquirer_text:
        print("❌ Error: Swapped upload detected. The Issuer contract was uploaded in place of the Acquirer contract.")
        exit()

    print("✅ PDFs Loaded Successfully\n")

    print("Classifying Issuer Contract...")
    issuer_clauses = classify_contract(issuer_text)

    print("Classifying Acquirer Contract...")
    acquirer_clauses = classify_contract(acquirer_text)

    print("\n✅ Classification Completed\n")

    print("=" * 60)
    print("       CONTRACT CLASSIFICATION COMPARISON")
    print("=" * 60)
    print()
    print_classification_table(issuer_clauses, acquirer_clauses)

    print("\n" + "=" * 60)
    print("       STARTING AI NEGOTIATION")
    print("=" * 60)

    state = {
        "issuer_clauses": issuer_clauses,
        "acquirer_clauses": acquirer_clauses,
        "issuer_response": "",
        "acquirer_response": "",
        "final_clauses": []
    }

    result = negotiation_workflow.invoke(state)

    print("\n" + "=" * 60)
    print("       ISSUER NEGOTIATION FEEDBACK")
    print("=" * 60)
    print(result["issuer_response"])

    print("\n" + "=" * 60)
    print("       ACQUIRER NEGOTIATION FEEDBACK")
    print("=" * 60)
    print(result["acquirer_response"])

    print("\n" + "=" * 60)
    print("       ✅ NEGOTIATION COMPLETED\n")
    print("=" * 60)

    final_clauses = result.get("final_clauses", [])
    print_negotiation_report(final_clauses)

    if output_path:
        try:
            with open(output_path, "w", encoding="utf-8") as f:
                json.dump(final_clauses, f, indent=2)
            print(f"\n✅ Saved final negotiation results to: {output_path}")
        except Exception as e:
            print(f"\n❌ Error saving output JSON to {output_path}: {e}")