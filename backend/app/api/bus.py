from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.database import get_db
from app.api.payment import get_current_user
from app.models.user import User

router = APIRouter(prefix="/api/bus", tags=["bus"])

BUS_STOPS = [
    {"id": "s1", "name": "Mustaqillik maydoni", "lat": 41.2995, "lng": 69.2401, "address": "Shahar markazi"},
    {"id": "s2", "name": "Chilonzor", "lat": 41.3095, "lng": 69.2501, "address": "Chilonzor tumani"},
    {"id": "s3", "name": "Yunusobod", "lat": 41.2895, "lng": 69.2301, "address": "Yunusobod tumani"},
    {"id": "s4", "name": "Sergeli", "lat": 41.2795, "lng": 69.2601, "address": "Sergeli tumani"},
    {"id": "s5", "name": "Olmazor", "lat": 41.3195, "lng": 69.2201, "address": "Olmazor tumani"},
    {"id": "s6", "name": "Mirobod", "lat": 41.2895, "lng": 69.2701, "address": "Mirobod tumani"},
]

BUS_ARRIVALS = {
    "s1": [
        {"route": "18", "destination": "Chilonzor - Yunusobod", "arrival_minutes": 2},
        {"route": "33", "destination": "Sergeli - Mustaqillik", "arrival_minutes": 5},
        {"route": "55", "destination": "Yunusobod - Chilonzor", "arrival_minutes": 8},
    ],
    "s2": [
        {"route": "11", "destination": "Olmazor - Sergeli", "arrival_minutes": 3},
        {"route": "18", "destination": "Chilonzor - Yunusobod", "arrival_minutes": 7},
    ],
    "s3": [
        {"route": "25", "destination": "Yangi hayot - Mustaqillik", "arrival_minutes": 1},
        {"route": "50", "destination": "Qatortol - Chilonzor", "arrival_minutes": 6},
    ],
    "s4": [
        {"route": "40", "destination": "Bodomzor - Chilonzor", "arrival_minutes": 4},
    ],
    "s5": [
        {"route": "30", "destination": "Mirobod - Sobir Rahimov", "arrival_minutes": 3},
    ],
    "s6": [
        {"route": "100", "destination": "Ko'kcha - Qo'yliq", "arrival_minutes": 2},
        {"route": "25", "destination": "Yangi hayot - Mustaqillik", "arrival_minutes": 9},
    ],
}

ROUTE_SCHEDULES = {
    "18": {
        "name": "Chilonzor - Yunusobod",
        "price": 1700,
        "stops": [
            {"stop_id": "s2", "name": "Chilonzor", "arrival_time": "06:00"},
            {"stop_id": "s5", "name": "Olmazor", "arrival_time": "06:12"},
            {"stop_id": "s1", "name": "Mustaqillik maydoni", "arrival_time": "06:25"},
            {"stop_id": "s6", "name": "Mirobod", "arrival_time": "06:38"},
            {"stop_id": "s3", "name": "Yunusobod", "arrival_time": "06:50"},
        ],
    },
    "33": {
        "name": "Sergeli - Mustaqillik",
        "price": 1700,
        "stops": [
            {"stop_id": "s4", "name": "Sergeli", "arrival_time": "06:05"},
            {"stop_id": "s2", "name": "Chilonzor", "arrival_time": "06:18"},
            {"stop_id": "s1", "name": "Mustaqillik maydoni", "arrival_time": "06:30"},
        ],
    },
    "55": {
        "name": "Yunusobod - Chilonzor",
        "price": 1700,
        "stops": [
            {"stop_id": "s3", "name": "Yunusobod", "arrival_time": "06:10"},
            {"stop_id": "s6", "name": "Mirobod", "arrival_time": "06:22"},
            {"stop_id": "s1", "name": "Mustaqillik maydoni", "arrival_time": "06:35"},
            {"stop_id": "s2", "name": "Chilonzor", "arrival_time": "06:48"},
        ],
    },
    "11": {
        "name": "Olmazor - Sergeli",
        "price": 1700,
        "stops": [
            {"stop_id": "s5", "name": "Olmazor", "arrival_time": "06:15"},
            {"stop_id": "s1", "name": "Mustaqillik maydoni", "arrival_time": "06:28"},
            {"stop_id": "s4", "name": "Sergeli", "arrival_time": "06:42"},
        ],
    },
    "25": {
        "name": "Yangi hayot - Mustaqillik",
        "price": 1700,
        "stops": [
            {"stop_id": "s3", "name": "Yunusobod", "arrival_time": "06:20"},
            {"stop_id": "s6", "name": "Mirobod", "arrival_time": "06:33"},
            {"stop_id": "s1", "name": "Mustaqillik maydoni", "arrival_time": "06:45"},
        ],
    },
    "50": {
        "name": "Qatortol - Chilonzor",
        "price": 1700,
        "stops": [
            {"stop_id": "s3", "name": "Yunusobod", "arrival_time": "06:08"},
            {"stop_id": "s1", "name": "Mustaqillik maydoni", "arrival_time": "06:20"},
            {"stop_id": "s2", "name": "Chilonzor", "arrival_time": "06:32"},
        ],
    },
    "40": {
        "name": "Bodomzor - Chilonzor",
        "price": 1700,
        "stops": [
            {"stop_id": "s4", "name": "Sergeli", "arrival_time": "06:12"},
            {"stop_id": "s2", "name": "Chilonzor", "arrival_time": "06:25"},
        ],
    },
    "30": {
        "name": "Mirobod - Sobir Rahimov",
        "price": 1700,
        "stops": [
            {"stop_id": "s6", "name": "Mirobod", "arrival_time": "06:30"},
            {"stop_id": "s5", "name": "Olmazor", "arrival_time": "06:42"},
        ],
    },
    "100": {
        "name": "Ko'kcha - Qo'yliq",
        "price": 1700,
        "stops": [
            {"stop_id": "s6", "name": "Mirobod", "arrival_time": "06:05"},
            {"stop_id": "s1", "name": "Mustaqillik maydoni", "arrival_time": "06:18"},
            {"stop_id": "s4", "name": "Sergeli", "arrival_time": "06:32"},
        ],
    },
}


@router.get("/routes")
async def get_bus_routes():
    routes = [
        {"id": "1", "name": "Temir yo'l vokzali - Chilonzor", "price": 1700},
        {"id": "2", "name": "Xalqlar Do'stligi - Minor", "price": 1700},
        {"id": "3", "name": "Beruniy - Yunusobod", "price": 1700},
        {"id": "11", "name": "Olmazor - Sergeli", "price": 1700},
        {"id": "18", "name": "Qo'yliq - Chilonzor", "price": 1700},
        {"id": "25", "name": "Yangi hayot - Mustaqillik maydoni", "price": 1700},
        {"id": "30", "name": "Mirobod - Sobir Rahimov", "price": 1700},
        {"id": "40", "name": "Bodomzor - Chilonzor", "price": 1700},
        {"id": "50", "name": "Qatortol - Chilonzor", "price": 1700},
        {"id": "100", "name": "Ko'kcha - Qo'yliq", "price": 1700},
    ]
    return routes


@router.get("/nearby-stops")
async def get_nearby_stops(lat: float = 41.2995, lng: float = 69.2401,
                           user: User = Depends(get_current_user)):
    import math
    nearby = []
    for stop in BUS_STOPS:
        dlat = stop["lat"] - lat
        dlng = stop["lng"] - lng
        dist = math.sqrt(dlat * dlat + dlng * dlng) * 111000
        if dist < 50000:
            nearby.append(stop)
    return {"stops": nearby}


@router.get("/arrivals/{stop_id}")
async def get_stop_arrivals(stop_id: str, user: User = Depends(get_current_user)):
    arrivals = BUS_ARRIVALS.get(stop_id, [])
    return {"stop_id": stop_id, "arrivals": arrivals}


@router.get("/route-schedule/{route_id}")
async def get_route_schedule(route_id: str, user: User = Depends(get_current_user)):
    schedule = ROUTE_SCHEDULES.get(route_id)
    if not schedule:
        return {"error": "Marshrut topilmadi"}
    return {"route_id": route_id, "schedule": schedule}


@router.get("/nearby")
async def get_nearby_buses(lat: float = 41.2995, lng: float = 69.2401,
                           user: User = Depends(get_current_user)):
    return {"message": "Atrofdagi avtobuslar", "buses": []}


@router.post("/pay-route/{route_id}")
async def pay_bus_route(route_id: str, user: User = Depends(get_current_user)):
    return {"redirect": f"/api/payment/pay?route={route_id}&amount=1700"}
