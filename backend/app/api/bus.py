from fastapi import APIRouter, Depends

from app.api.three_tm_client import three_tm, haversine
from app.database import get_db
from app.api.payment import get_current_user
from app.models.user import User

router = APIRouter(prefix="/api/bus", tags=["bus"])

BUS_FARE = 1700


@router.get("/routes")
async def get_bus_routes():
    routes = await three_tm.get_routes()
    return [
        {
            "id": str(r["number"]),
            "name": f"{r['uzNameA']} - {r['uzNameB']}",
            "price": BUS_FARE,
        }
        for r in routes
    ]


@router.get("/nearby-stops")
async def get_nearby_stops(
    lat: float = 41.2995, lng: float = 69.2401, user: User = Depends(get_current_user)
):
    stations = await three_tm.get_stations()
    nearby = []
    for s in stations:
        dist = haversine(lat, lng, s["lat"], s["lng"])
        if dist < 500:
            nearby.append({
                "id": str(s["id"]),
                "name": s.get("uzName") or s.get("name", ""),
                "lat": s["lat"],
                "lng": s["lng"],
                "address": s.get("uzName") or s.get("name", ""),
            })
    return {"stops": nearby[:50]}


@router.get("/arrivals/{stop_id}")
async def get_stop_arrivals(stop_id: str, user: User = Depends(get_current_user)):
    stations = await three_tm.get_stations()
    station = next((s for s in stations if str(s["id"]) == stop_id), None)
    if not station:
        return {"stop_id": stop_id, "arrivals": []}

    routes = await three_tm.get_routes()
    stop_lat, stop_lng = station["lat"], station["lng"]

    arrivals = []
    for route in routes[:20]:
        detail = await three_tm.get_route_detail(route["id"])
        route_stations = detail.get("stations") or []
        if not any(str(rs["id"]) == stop_id for rs in route_stations):
            continue
        buses = await three_tm.get_route_buses(route["id"])
        for bus in buses:
            if bus.get("status") != "ON_ROUTE":
                continue
            bus_lat, bus_lng = bus["lat"], bus["lng"]
            dist = haversine(bus_lat, bus_lng, stop_lat, stop_lng)
            speed = bus.get("speed", 0)
            eta_min = max(1, int(dist / max(speed * 1000 / 60, 1))) if speed > 1 else 10
            arrivals.append({
                "route": str(route["number"]),
                "destination": f"{detail.get('uzNameA', '')} - {detail.get('uzNameB', '')}",
                "arrival_minutes": eta_min,
            })
    arrivals.sort(key=lambda a: a["arrival_minutes"])
    return {"stop_id": stop_id, "arrivals": arrivals[:10]}


def _calc_arrival_times(start_time: str, duration_min: int, stations: list) -> list:
    if not stations or not start_time:
        return stations
    try:
        h, m = map(int, start_time.split(":"))
        start_seconds = h * 3600 + m * 60
    except (ValueError, AttributeError):
        return stations
    max_dist = max((s.get("distance") or 0) for s in stations) or 1
    result = []
    for rs in stations:
        ratio = (rs.get("distance") or 0) / max_dist
        offset = int(ratio * duration_min * 60)
        total = start_seconds + offset
        hh, mm = divmod(total // 60, 60)
        result.append({
            "stop_id": str(rs["id"]),
            "name": rs.get("uzName") or rs.get("name", ""),
            "arrival_time": f"{hh:02d}:{mm:02d}",
            "lat": rs.get("lat"),
            "lng": rs.get("lng"),
        })
    return result


@router.get("/route-schedule/{route_id}")
async def get_route_schedule(route_id: str, user: User = Depends(get_current_user)):
    routes = await three_tm.get_routes()
    route = next((r for r in routes if str(r["number"]) == route_id), None)
    if not route:
        return {"error": "Marshrut topilmadi"}
    detail = await three_tm.get_route_detail(route["id"])
    route_stations = detail.get("stations") or []
    stops = _calc_arrival_times(
        detail.get("startTime", ""),
        detail.get("avgTripDurationMin", 0),
        route_stations,
    )
    return {
        "route_id": route_id,
        "schedule": {
            "name": f"{detail.get('uzNameA', '')} - {detail.get('uzNameB', '')}",
            "price": BUS_FARE,
            "stops": stops,
        },
    }


@router.get("/nearby")
async def get_nearby_buses(
    lat: float = 41.2995,
    lng: float = 69.2401,
    user: User = Depends(get_current_user),
):
    routes = await three_tm.get_routes()
    buses_list = []
    for route in routes[:20]:
        buses = await three_tm.get_route_buses(route["id"])
        for bus in buses:
            if bus.get("status") != "ON_ROUTE":
                continue
            bl = haversine(lat, lng, bus["lat"], bus["lng"])
            if bl < 2000:
                buses_list.append({
                    "id": bus.get("busId", ""),
                    "route": str(route["number"]),
                    "lat": bus["lat"],
                    "lng": bus["lng"],
                    "speed": bus.get("speed", 0),
                    "direction": bus.get("course", 0),
                    "plate": bus.get("govPlate", ""),
                    "busType": bus.get("busTypeName", ""),
                })
    return {"message": "Atrofdagi avtobuslar", "buses": buses_list}


@router.get("/stop-location/{stop_id}")
async def get_stop_location(stop_id: str, user: User = Depends(get_current_user)):
    stations = await three_tm.get_stations()
    station = next((s for s in stations if str(s["id"]) == stop_id), None)
    if not station:
        return {"error": "Bekat topilmadi"}
    return {"id": stop_id, "name": station.get("uzName") or station.get("name", ""), "lat": station["lat"], "lng": station["lng"]}


@router.post("/pay-route/{route_id}")
async def pay_bus_route(
    route_id: str, user: User = Depends(get_current_user)
):
    return {"redirect": f"/api/payment/pay?route={route_id}&amount={BUS_FARE}"}
