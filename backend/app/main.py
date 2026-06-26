from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api import auth, payment, bus, telegram
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


@app.get("/")
async def root():
    return {"message": "PayBus API ishlayapti", "version": "1.0.0", "status": "ready"}
