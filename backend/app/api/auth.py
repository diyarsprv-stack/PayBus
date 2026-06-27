from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.models.user import User
from app.schemas.user import SendSMSRequest, VerifySMSRequest, UpdateProfileRequest, TokenResponse, UserResponse
from app.services.sms import sms_service
from app.utils.security import create_access_token
from app.api.payment import get_current_user

router = APIRouter(prefix="/api/auth", tags=["auth"])

# In-memory SMS codes (production da Redis ishlatiladi)
sms_codes: dict[str, str] = {}


@router.post("/send-code")
async def send_code(request: SendSMSRequest, db: AsyncSession = Depends(get_db)):
    code = sms_service.generate_code()
    sms_codes[request.phone_number] = code

    sent = await sms_service.send_sms(request.phone_number, code)
    if not sent:
        print(f"[Auth] SMS yuborilmadi: {request.phone_number}")

    return {"message": "SMS kod yuborildi"}


@router.post("/verify-code", response_model=TokenResponse)
async def verify_code(request: VerifySMSRequest, db: AsyncSession = Depends(get_db)):
    stored_code = sms_codes.get(request.phone_number)
    if not stored_code or stored_code != request.code:
        raise HTTPException(status_code=400, detail="Notog'ri kod")

    del sms_codes[request.phone_number]

    result = await db.execute(select(User).where(User.phone_number == request.phone_number))
    user = result.scalar_one_or_none()

    if not user:
        user = User(phone_number=request.phone_number, is_verified=True)
        db.add(user)
        await db.flush()
        await db.refresh(user)

    user.is_verified = True
    access_token = create_access_token(data={"sub": str(user.id)})

    user_resp = UserResponse(id=str(user.id), phone_number=user.phone_number, full_name=user.full_name, is_verified=user.is_verified)

    return TokenResponse(access_token=access_token, user=user_resp)


@router.get("/profile", response_model=UserResponse)
async def get_profile(user: User = Depends(get_current_user)):
    return UserResponse(id=str(user.id), phone_number=user.phone_number,
                        full_name=user.full_name, is_verified=user.is_verified)


@router.put("/profile", response_model=UserResponse)
async def update_profile(request: UpdateProfileRequest, db: AsyncSession = Depends(get_db),
                         user: User = Depends(get_current_user)):
    if request.full_name is not None:
        user.full_name = request.full_name
    if request.phone_number is not None:
        user.phone_number = request.phone_number
    await db.flush()
    await db.refresh(user)
    return UserResponse(id=str(user.id), phone_number=user.phone_number,
                        full_name=user.full_name, is_verified=user.is_verified)
