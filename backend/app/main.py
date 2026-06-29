from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api import auth, payment, bus, telegram
from app.api.three_tm_client import three_tm
from app.database import engine, Base

app = FastAPI(
    title="PayBus API",
    description="Toshkent avtobuslari uchun to'lov tizimi",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router)
app.include_router(payment.router)
app.include_router(bus.router)
app.include_router(telegram.router)


@app.on_event("startup")
async def startup():
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)


@app.on_event("shutdown")
async def shutdown():
    await engine.dispose()
    await three_tm.close()


@app.get("/")
async def root():
    return {"message": "PayBus API ishlayapti", "version": "1.0.0", "status": "ready"}


@app.get("/health/eskiz")
async def check_eskiz():
    from app.services.eskiz import eskiz_service
    token = await eskiz_service._login()
    balance = await eskiz_service.get_balance() if token else None
    return {
        "email_configured": bool(eskiz_service.email),
        "password_configured": bool(eskiz_service.password),
        "token_obtained": token is not None,
        "balance": balance
    }
