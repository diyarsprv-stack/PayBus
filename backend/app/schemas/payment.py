from pydantic import BaseModel


class AddCardRequest(BaseModel):
    card_number: str
    card_holder: str
    expire_date: str
    provider: str  # uzcard, humo


class CardResponse(BaseModel):
    id: str
    card_number: str
    card_holder: str
    provider: str
    is_default: bool

    class Config:
        from_attributes = True


class CardVerifyRequest(BaseModel):
    code: str


class PaymentRequest(BaseModel):
    amount: float
    provider: str
    card_id: str | None = None
    bus_route: str | None = None
