import os
import shutil
import tempfile
import filecmp
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import logging
from pypdf import PdfReader

# Suppress harmless pypdf warnings
logging.getLogger("pypdf").setLevel(logging.ERROR)
from classifier import classify_contract
from negotiation_graph import negotiation_workflow

app = FastAPI(
    title="Autonomous Contract Negotiation System API",
    description="REST API for classifying and negotiating legal contracts.",
    version="1.0.0"
)

# Enable CORS for frontend connectivity
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:4200", "http://localhost:8080"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

from utils import extract_text, validate_contracts

@app.post("/api/negotiate")
async def negotiate_contracts(
    issuer_file: UploadFile = File(...),
    acquirer_file: UploadFile = File(...)
):
    # Create temp files to read the PDFs
    with tempfile.NamedTemporaryFile(delete=False, suffix=".pdf") as issuer_tmp, \
         tempfile.NamedTemporaryFile(delete=False, suffix=".pdf") as acquirer_tmp:
        
        try:
            shutil.copyfileobj(issuer_file.file, issuer_tmp)
            shutil.copyfileobj(acquirer_file.file, acquirer_tmp)
            issuer_tmp_path = issuer_tmp.name
            acquirer_tmp_path = acquirer_tmp.name
        except OSError as e:
            raise HTTPException(status_code=500, detail=f"Failed to process file uploads: {str(e)}")

    try:
        # Check if identical files
        if filecmp.cmp(issuer_tmp_path, acquirer_tmp_path, shallow=False):
            raise HTTPException(status_code=400, detail="Issuer and Acquirer contracts cannot be the same file.")

        issuer_text = extract_text(issuer_tmp_path)
        acquirer_text = extract_text(acquirer_tmp_path)

        validate_contracts(issuer_text, acquirer_text)

        # Run pipeline
        issuer_clauses = classify_contract(issuer_text)
        acquirer_clauses = classify_contract(acquirer_text)

        state = {
            "issuer_clauses": issuer_clauses,
            "acquirer_clauses": acquirer_clauses,
            "issuer_response": "",
            "acquirer_response": "",
            "final_clauses": []
        }

        result = negotiation_workflow.invoke(state)

        # Parse counts for negotiation summary/status
        agreed_count = 0
        modified_count = 0
        rejected_count = 0
        final_clauses = result.get("final_clauses", [])

        for clause in final_clauses:
            status = str(clause.get("status", "agreed")).lower()
            if status == "agreed":
                agreed_count += 1
            elif status == "modified":
                modified_count += 1
            else:
                rejected_count += 1

        negotiation_status = "SUCCESS" if rejected_count == 0 else "FAILED"

        return {
            "status": "success",
            "issuer_clauses_extracted": issuer_clauses,
            "acquirer_clauses_extracted": acquirer_clauses,
            "issuer_feedback": result.get("issuer_response"),
            "acquirer_feedback": result.get("acquirer_response"),
            "final_clauses": final_clauses,
            "summary": {
                "clauses_agreed": agreed_count,
                "clauses_modified": modified_count,
                "clauses_rejected": rejected_count,
                "negotiation_status": negotiation_status
            }
        }
    except HTTPException as he:
        raise he
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))
    finally:
        try:
            if 'issuer_tmp_path' in locals():
                os.remove(issuer_tmp_path)
            if 'acquirer_tmp_path' in locals():
                os.remove(acquirer_tmp_path)
        except OSError:
            pass
