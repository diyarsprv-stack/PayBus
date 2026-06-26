from app.config import settings


class PaymeService:
    def __init__(self):
        self.merchant_id = settings.PAYME_MERCHANT_ID
        self.secret_key = settings.PAYME_SECRET_KEY
        self.base_url = "https://api.payme.uz/v1"

    async def pay(self, card_number: str, amount: float) -> dict:
        import hashlib
        import time
        trx_id = f"PAY{int(time.time())}{hashlib.md5(card_number.encode()).hexdigest()[:6]}"
        return {
            "success": True,
            "transaction_id": trx_id,
            "amount": amount
        }

    async def check_status(self, transaction_id: str) -> dict:
        return {"status": "success", "transaction_id": transaction_id}


payme_service = PaymeService()
