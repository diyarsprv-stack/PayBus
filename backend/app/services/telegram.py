import httpx
from fastapi import Request

from app.config import settings


class TelegramService:
    def __init__(self):
        self.token = settings.TELEGRAM_BOT_TOKEN
        self.api_base = f"https://api.telegram.org/bot{self.token}"

    async def _post(self, method: str, json: dict) -> dict | None:
        if not self.token:
            return None
        try:
            async with httpx.AsyncClient() as client:
                resp = await client.post(
                    f"{self.api_base}/{method}",
                    json=json,
                    timeout=10
                )
                return resp.json() if resp.status_code == 200 else None
        except Exception as e:
            print(f"[Telegram xatolik] {e}")
            return None

    async def send_code(self, chat_id: int, code: str) -> bool:
        result = await self._post("sendMessage", {
            "chat_id": chat_id,
            "text": f"<b>PayBus tasdiqlash kodi</b>\n\n"
                    f"<code>{code}</code>\n\n"
                    f"Kodni nusxalash uchun yuqoridagi raqamni bosing va ushlab turing.",
            "parse_mode": "HTML"
        })
        return result is not None

    async def send_welcome(self, chat_id: int):
        result = await self._post("sendMessage", {
            "chat_id": chat_id,
            "text": "Assalomu alaykum! PayBus botiga xush kelibsiz.\n\n"
                    "Tasdiqlash kodlarini olish uchun pastdagi tugmani bosing.",
            "reply_markup": {
                "keyboard": [[{
                    "text": "📱 Telefon raqamni ulashish",
                    "request_contact": True
                }]],
                "resize_keyboard": True,
                "one_time_keyboard": True
            }
        })
        return result is not None

    async def send_confirmation(self, chat_id: int, phone: str):
        result = await self._post("sendMessage", {
            "chat_id": chat_id,
            "text": f"✅ <b>{phone}</b> raqami muvaffaqiyatli bog'landi!\n\n"
                    f"Endi PayBus ilovasidagi tasdiqlash kodlari sizga shu bot orqali keladi.\n"
                    f"Kodni <b>nusxalash</b> uchun bosing va ushlab turing.",
            "parse_mode": "HTML",
            "reply_markup": {"remove_keyboard": True}
        })
        return result is not None

    async def set_webhook(self, url: str) -> bool:
        result = await self._post("setWebhook", {"url": url})
        return result is not None


telegram_service = TelegramService()
