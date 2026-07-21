import os
import shutil
import tempfile
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pypdf import PdfReader
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
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

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
        raise HTTPException(status_code=400, detail=f"Error reading PDF: {str(e)}")

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
        except Exception as e:
            raise HTTPException(status_code=500, detail=f"Failed to process file uploads: {str(e)}")

    try:
        # Check if identical files
        if os.path.samefile(issuer_tmp_path, acquirer_tmp_path):
            raise HTTPException(status_code=400, detail="Issuer and Acquirer contracts cannot be the same file.")

        issuer_text = extract_text(issuer_tmp_path)
        acquirer_text = extract_text(acquirer_tmp_path)

        if not issuer_text:
            raise HTTPException(status_code=400, detail="Issuer PDF contains no extractable text.")
        if not acquirer_text:
            raise HTTPException(status_code=400, detail="Acquirer PDF contains no extractable text.")

        # Check for swapped uploads
        if "Acquirer Company Contract" in issuer_text:
            raise HTTPException(status_code=400, detail="Swapped upload detected. The Acquirer contract was uploaded in place of the Issuer contract.")
        if "Issuer Company Contract" in acquirer_text:
            raise HTTPException(status_code=400, detail="Swapped upload detected. The Issuer contract was uploaded in place of the Acquirer contract.")

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
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    finally:
        # Clean up temp files
        try:
            os.remove(issuer_tmp_path)
            os.remove(acquirer_tmp_path)
        except Exception:
            pass
