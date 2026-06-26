import random

import httpx

from app.config import settings


class SMSService:
    def __init__(self):
        self.provider = settings.SMS_PROVIDER
        self.api_key = settings.ISMS_API_KEY
        self.api_url = settings.ISMS_API_URL

    def generate_code(self) -> str:
        return str(random.randint(100000, 999999))

    async def send_sms(self, phone_number: str, code: str) -> bool:
        if self.provider == "telegram":
            return await self._send_via_telegram(phone_number, code)
        if self.provider == "isms" and self.api_key:
            return await self._send_via_isms(phone_number, code)
        return self._send_via_console(phone_number, code)

    def _send_via_console(self, phone_number: str, code: str) -> bool:
        print(f"[SMS] {phone_number} -> Tasdiqlash kodi: {code}")
        return True

    async def _send_via_telegram(self, phone_number: str, code: str) -> bool:
        from app.api.telegram import phone_chat_map
        chat_id = phone_chat_map.get(phone_number)
        if not chat_id:
            print(f"[Telegram] {phone_number} uchun chat_id topilmadi")
            return self._send_via_console(phone_number, code)
        from app.services.telegram import telegram_service
        return await telegram_service.send_code(chat_id, code)

    async def _send_via_isms(self, phone_number: str, code: str) -> bool:
        clean_number = phone_number.replace(" ", "")
        if not clean_number.startswith("+"):
            clean_number = "+" + clean_number

        try:
            async with httpx.AsyncClient() as client:
                response = await client.post(
                    self.api_url,
                    data={
                        "number": clean_number,
                        "message": f"PayBus: Tasdiqlash kodi: {code}",
                        "key": self.api_key,
                        "prioritize": "1"
                    },
                    timeout=10
                )
                return response.status_code == 200
        except Exception as e:
            print(f"[SMS xatolik] iSMS: {e}")
            return False


sms_service = SMSService()
