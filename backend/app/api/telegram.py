from fastapi import APIRouter, Request, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.models.user import User
from app.api.payment import get_current_user
from app.services.telegram import telegram_service

router = APIRouter(prefix="/api/telegram", tags=["telegram"])

phone_chat_map: dict[str, int] = {}


@router.post("/setup")
async def setup_webhook(request: Request):
    base_url = str(request.base_url).rstrip("/")
    webhook_url = f"{base_url}/api/telegram/webhook"
    ok = await telegram_service.set_webhook(webhook_url)
    return {"status": "ok" if ok else "failed", "webhook_url": webhook_url}


@router.get("/info")
async def get_telegram_info():
    from app.config import settings
    token = settings.TELEGRAM_BOT_TOKEN
    if not token:
        return {"enabled": False}
    bot_username = None
    import httpx
    try:
        async with httpx.AsyncClient() as client:
            resp = await client.get(f"https://api.telegram.org/bot{token}/getMe", timeout=5)
            if resp.status_code == 200:
                bot_username = resp.json().get("result", {}).get("username")
    except Exception:
        pass
    return {"enabled": True, "bot_username": bot_username}


@router.post("/webhook")
async def telegram_webhook(request: Request):
    body = await request.json()

    message = body.get("message", {})
    contact = message.get("contact")
    chat_id = message.get("chat", {}).get("id")

    if not chat_id:
        return {"ok": True}

    if contact and contact.get("phone_number"):
        phone = contact["phone_number"]
        if not phone.startswith("+"):
            phone = "+" + phone
        phone_chat_map[phone] = chat_id
        await telegram_service.send_confirmation(chat_id, phone)
        return {"ok": True}

    text = (message.get("text") or "").strip()

    if text == "/start":
        await telegram_service.send_welcome(chat_id)
        return {"ok": True}

    phone = text.strip()
    if phone.startswith("+") and phone[1:].isdigit():
        phone_chat_map[phone] = chat_id
        await telegram_service.send_confirmation(chat_id, phone)
    else:
        await telegram_service.send_welcome(chat_id)

    return {"ok": True}
