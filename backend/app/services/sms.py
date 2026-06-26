import uuid
import random
import base64

import httpx

from app.config import settings


class SMSService:
    def __init__(self):
        self.provider = settings.SMS_PROVIDER
        self.login = settings.PLAYMOBILE_LOGIN
        self.password = settings.PLAYMOBILE_PASSWORD
        self.originator = settings.PLAYMOBILE_ORIGINATOR
        self.api_url = "https://send.smsxabar.uz/broker-api/send"

    def generate_code(self) -> str:
        return str(random.randint(100000, 999999))

    async def send_sms(self, phone_number: str, code: str) -> bool:
        if self.provider == "playmobile" and self.login and self.password:
            return await self._send_via_playmobile(phone_number, code)
        return self._send_via_console(phone_number, code)

    def _send_via_console(self, phone_number: str, code: str) -> bool:
        print(f"[SMS] {phone_number} -> Tasdiqlash kodi: {code}")
        return True

    async def _send_via_playmobile(self, phone_number: str, code: str) -> bool:
        clean_number = phone_number.replace("+", "").replace(" ", "")
        message_id = str(uuid.uuid4())

        payload = {
            "messages": [
                {
                    "recipient": clean_number,
                    "message-id": message_id,
                    "sms": {
                        "originator": self.originator,
                        "content": {
                            "text": f"PayBus tasdiqlash kodi: {code}"
                        }
                    }
                }
            ]
        }

        auth_str = base64.b64encode(f"{self.login}:{self.password}".encode()).decode()

        try:
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    self.api_url,
                    json=payload,
                    headers={
                        "Content-Type": "application/json",
                        "Authorization": f"Basic {auth_str}"
                    },
                    timeout=10
                )
                return response.status_code == 200
        except Exception as e:
            print(f"[SMS xatolik] Play Mobile: {e}")
            return False


sms_service = SMSService()
