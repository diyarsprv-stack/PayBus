import random
from typing import Optional

import httpx

from app.config import settings


class SMSService:
    def __init__(self):
        self.api_key = settings.SMS_SERVICE_API_KEY
        self.api_url = settings.SMS_SERVICE_URL

    def generate_code(self) -> str:
        return str(random.randint(100000, 999999))

    async def send_sms(self, phone_number: str, code: str) -> bool:
        try:
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    self.api_url,
                    json={
                        "api_key": self.api_key,
                        "phone": phone_number,
                        "message": f"PayBus tasdiqlash kodi: {code}"
                    },
                    timeout=10
                )
                return response.status_code == 200
        except Exception:
            return False


sms_service = SMSService()
