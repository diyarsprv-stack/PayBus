from datetime import datetime

from pydantic import BaseModel


class TransactionResponse(BaseModel):
    id: str
    amount: float
    provider: str
    status: str
    bus_route: str | None
    created_at: datetime

    class Config:
        from_attributes = True
