from pypdf import PdfReader

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
    except (OSError, ValueError) as e:
        raise ValueError(f"Error reading PDF: {e}")

def validate_contracts(issuer_text, acquirer_text):
    if not issuer_text:
        raise ValueError("Issuer PDF contains no extractable text.")
    if not acquirer_text:
        raise ValueError("Acquirer PDF contains no extractable text.")
    if "Acquirer Company Contract" in issuer_text:
        raise ValueError("Swapped upload detected. The Acquirer contract was uploaded in place of the Issuer contract.")
    if "Issuer Company Contract" in acquirer_text:
        raise ValueError("Swapped upload detected. The Issuer contract was uploaded in place of the Acquirer contract.")
