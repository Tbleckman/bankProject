from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

class Transaction(BaseModel):
    amount: float
    transaction_type: str
    recent_transactions: int

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/analyze")
def analyze(transaction: Transaction):
    score = 0.0
    reasons = []

    if transaction.amount > 1000:
        score += .5
        reasons.append("large transaction")
    if transaction.recent_transactions > 4:
        score += .3
        reasons.append("high recent transaction activity")

    if transaction.transaction_type.lower() == "withdrawal":
        score += .1

    score = min(score, 1.0)

    return {
        "risk_score": score,
        "flagged": score >= .6,
        "reason": ", ".join(reasons) if reasons else "no unusual activity"
    }