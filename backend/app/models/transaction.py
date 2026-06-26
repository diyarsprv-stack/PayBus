import uuid
from datetime import datetime, timezone

from sqlalchemy import Column, String, Float, DateTime, ForeignKey
from sqlalchemy.orm import Mapped, mapped_column
import enum

from app.database import Base


class PaymentProvider(str, enum.Enum):
    UZCARD = "uzcard"
    HUMO = "humo"
    CLICK = "click"
    PAYME = "payme"


class Transaction(Base):
    __tablename__ = "transactions"

    id: Mapped[str] = mapped_column(String(36), primary_key=True, default=lambda: str(uuid.uuid4()))
    user_id: Mapped[str] = mapped_column(String(36), ForeignKey("users.id"), nullable=False)
    amount: Mapped[float] = mapped_column(Float, nullable=False)
    provider: Mapped[str] = mapped_column(String(20), nullable=False)
    status: Mapped[str] = mapped_column(String(20), default="pending")
    payment_card_id: Mapped[str | None] = mapped_column(String(36), ForeignKey("payment_cards.id"), nullable=True)
    bus_route: Mapped[str | None] = mapped_column(String(50), nullable=True)
    external_transaction_id: Mapped[str | None] = mapped_column(String(100), nullable=True)
    created_at: Mapped[datetime] = mapped_column(DateTime, default=lambda: datetime.now(timezone.utc))
