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
    SMS_PROVIDER: str = "console"  # "console" yoki "isms"
    ISMS_API_KEY: str = ""
    ISMS_API_URL: str = "https://isms.uz/gateway/services/send.php"

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
