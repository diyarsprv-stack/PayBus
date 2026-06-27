import random


class SMSService:
    def generate_code(self) -> str:
        return str(random.randint(100000, 999999))

    async def send_sms(self, phone_number: str, code: str) -> bool:
        from app.services.eskiz import eskiz_service
        message = f"PayBus tasdiqlash kodi: {code}"
        sent = await eskiz_service.send_sms(phone_number, message)
        if sent:
            return True

        print(f"[Eskiz] {phone_number} ga SMS yuborilmadi, Telegram orqali yuborilmoqda")
        from app.api.telegram import phone_chat_map
        chat_id = phone_chat_map.get(phone_number)
        if not chat_id:
            print(f"[Telegram] {phone_number} uchun chat_id topilmadi")
            return False
        from app.services.telegram import telegram_service
        return await telegram_service.send_code(chat_id, code)


sms_service = SMSService()
