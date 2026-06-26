from fastapi import APIRouter, Depends, HTTPException, Header
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from jose import jwt

from app.config import settings
from app.database import get_db
from app.models.user import User
from app.models.payment import PaymentCard
from app.models.transaction import Transaction, PaymentProvider
from app.schemas.payment import AddCardRequest, CardResponse, PaymentRequest, CardVerifyRequest
from app.schemas.transaction import TransactionResponse
from app.services.uzcard import uzcard_service
from app.services.humo import humo_service
from app.services.click import click_service
from app.services.payme import payme_service
from app.services.sms import sms_service

router = APIRouter(prefix="/api/payment", tags=["payment"])

card_verify_codes: dict[str, dict] = {}

MOCK_BALANCES: dict[str, float] = {}

MOCK_BALANCE_DEFAULT = 50000


async def get_current_user(authorization: str = Header(...), db: AsyncSession = Depends(get_db)) -> User:
    token = authorization.replace("Bearer ", "")
    payload = jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
    user_id = payload.get("sub")
    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()
    if not user:
        raise HTTPException(status_code=401, detail="Token notog'ri")
    return user


@router.post("/send-card-verify-code")
async def send_card_verify_code(request: AddCardRequest, user: User = Depends(get_current_user)):
    import random
    code = str(random.randint(100000, 999999))
    card_verify_codes[user.phone_number] = {
        "code": code,
        "card_number": request.card_number,
        "card_holder": request.card_holder,
        "expire_date": request.expire_date,
        "provider": request.provider
    }
    MOCK_BALANCES[request.card_number] = MOCK_BALANCE_DEFAULT
    await sms_service.send_sms(user.phone_number, code)
    return {"message": "Karta egasining telefon raqamiga SMS kod yuborildi"}


@router.post("/verify-card-code", response_model=CardResponse)
async def verify_card_code(request: CardVerifyRequest, user: User = Depends(get_current_user),
                           db: AsyncSession = Depends(get_db)):
    stored = card_verify_codes.get(user.phone_number)
    if not stored or stored["code"] != request.code:
        raise HTTPException(status_code=400, detail="Notog'ri kod")

    del card_verify_codes[user.phone_number]

    card = PaymentCard(
        user_id=user.id,
        card_number=stored["card_number"][-4:],
        card_holder=stored["card_holder"],
        expire_date=stored["expire_date"],
        provider=stored["provider"]
    )
    db.add(card)
    await db.flush()
    await db.refresh(card)

    return CardResponse(id=str(card.id), card_number=card.card_number,
                        card_holder=card.card_holder, provider=card.provider,
                        is_default=card.is_default)


@router.post("/add-card", response_model=CardResponse)
async def add_card(request: AddCardRequest, user: User = Depends(get_current_user),
                   db: AsyncSession = Depends(get_db)):
    card = PaymentCard(
        user_id=user.id,
        card_number=request.card_number[-4:],
        card_holder=request.card_holder,
        expire_date=request.expire_date,
        provider=request.provider
    )
    db.add(card)
    await db.flush()
    await db.refresh(card)
    MOCK_BALANCES[card.id] = MOCK_BALANCE_DEFAULT

    return CardResponse(id=str(card.id), card_number=card.card_number,
                        card_holder=card.card_holder, provider=card.provider,
                        is_default=card.is_default)


@router.get("/cards", response_model=list[CardResponse])
async def get_cards(user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(PaymentCard).where(PaymentCard.user_id == user.id))
    cards = result.scalars().all()
    return [CardResponse(id=str(c.id), card_number=c.card_number,
                         card_holder=c.card_holder, provider=c.provider,
                         is_default=c.is_default) for c in cards]


@router.post("/pay", response_model=TransactionResponse)
async def make_payment(request: PaymentRequest, user: User = Depends(get_current_user),
                       db: AsyncSession = Depends(get_db)):
    provider_map = {
        "uzcard": uzcard_service,
        "humo": humo_service,
        "click": click_service,
        "payme": payme_service
    }

    service = provider_map.get(request.provider)
    if not service:
        raise HTTPException(status_code=400, detail="Notog'ri to'lov provideri")

    card_number = ""
    if request.card_id:
        result = await db.execute(select(PaymentCard).where(
            PaymentCard.id == request.card_id, PaymentCard.user_id == user.id))
        card = result.scalar_one_or_none()
        if not card:
            raise HTTPException(status_code=404, detail="Karta topilmadi")
        card_number = card.card_number
        card_id = card.id

        balance = MOCK_BALANCES.get(card_id, MOCK_BALANCE_DEFAULT)
        if balance < request.amount:
            raise HTTPException(status_code=400, detail="Balans yetarli emas")
    else:
        card_id = None

    payment_result = await service.pay(user.phone_number, request.amount)

    if payment_result["success"] and card_id:
        MOCK_BALANCES[card_id] = MOCK_BALANCES.get(card_id, MOCK_BALANCE_DEFAULT) - request.amount

    transaction = Transaction(
        user_id=user.id,
        amount=request.amount,
        provider=request.provider,
        status="success" if payment_result["success"] else "failed",
        payment_card_id=request.card_id,
        bus_route=request.bus_route,
        external_transaction_id=payment_result.get("transaction_id")
    )
    db.add(transaction)
    await db.flush()
    await db.refresh(transaction)

    return TransactionResponse(id=str(transaction.id), amount=transaction.amount,
                               provider=transaction.provider, status=transaction.status,
                               bus_route=transaction.bus_route,
                               created_at=transaction.created_at)


@router.get("/transactions", response_model=list[TransactionResponse])
async def get_transactions(user: User = Depends(get_current_user), db: AsyncSession = Depends(get_db)):
    result = await db.execute(select(Transaction).where(
        Transaction.user_id == user.id).order_by(Transaction.created_at.desc()))
    transactions = result.scalars().all()
    return [TransactionResponse(id=str(t.id), amount=t.amount, provider=t.provider,
                                status=t.status, bus_route=t.bus_route,
                                created_at=t.created_at) for t in transactions]
