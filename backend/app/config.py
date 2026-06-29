import os
from pathlib import Path

from pydantic_settings import BaseSettings

BASE_DIR = Path(__file__).resolve().parent.parent


class Settings(BaseSettings):
    DATABASE_URL: str = f"sqlite+aiosqlite:///{BASE_DIR}/paybus.db"
    REDIS_URL: str = ""
    SECRET_KEY: str = "paybus-secret-key-change-in-production"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60
    TELEGRAM_BOT_TOKEN: str = ""

    # Eskiz.uz SMS credentials
    ESKIZ_EMAIL: str = ""
    ESKIZ_PASSWORD: str = ""
    ESKIZ_FROM: str = "4546"

    # 3TM API (Tashkent real-time bus tracking)
    THREE_TM_API_URL: str = "https://3tmapi.online"
    THREE_TM_CACHE_TTL: int = 60  # seconds

    # Payment provider credentials
    UZCARD_MERCHANT_ID: str = ""
    UZCARD_SECRET_KEY: str = ""
    HUMO_MERCHANT_ID: str = ""
    HUMO_SECRET_KEY: str = ""
    CLICK_MERCHANT_ID: str = ""
    CLICK_SECRET_KEY: str = ""
    PAYME_MERCHANT_ID: str = ""
    PAYME_SECRET_KEY: str = ""

    class Config:
        env_file = ".env"


settings = Settings()
