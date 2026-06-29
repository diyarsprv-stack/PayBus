import math
import time
from typing import Any

import httpx

from app.config import settings


class ThreeTMClient:
    def __init__(self):
        self.base_url = settings.THREE_TM_API_URL
        self.client = httpx.AsyncClient(base_url=self.base_url, timeout=15)
        self._cache: dict[str, tuple[float, Any]] = {}
        self._cache_ttl = settings.THREE_TM_CACHE_TTL

    async def _get(self, path: str) -> Any:
        now = time.time()
        cached = self._cache.get(path)
        if cached and now - cached[0] < self._cache_ttl:
            return cached[1]
        resp = await self.client.get(path, headers={"Accept": "application/json"})
        data = resp.json()
        self._cache[path] = (now, data)
        return data

    async def get_routes(self) -> list[dict]:
        return await self._get("/api/v1/mobile/routes")

    async def get_route_detail(self, route_id: int) -> dict:
        return await self._get(f"/api/v1/mobile/routes/{route_id}")

    async def get_route_buses(self, route_id: int) -> list[dict]:
        return await self._get(f"/api/v1/mobile/routes/{route_id}/buses")

    async def get_stations(self) -> list[dict]:
        return await self._get("/api/v1/mobile/stations")

    async def get_regions(self) -> list[dict]:
        return await self._get("/api/v1/mobile/regions")

    async def get_station_routes(self, station_id: int) -> list[dict]:
        return await self._get(f"/api/v1/mobile/stations/{station_id}/routes")

    async def get_route_eta_profile(self, route_id: int) -> dict:
        return await self._get(f"/api/v1/mobile/routes/{route_id}/eta-profile")

    def invalidate_cache(self, path: str = None):
        if path:
            self._cache.pop(path, None)
        else:
            self._cache.clear()

    async def close(self):
        await self.client.aclose()


def haversine(lat1: float, lng1: float, lat2: float, lng2: float) -> float:
    dlat = math.radians(lat2 - lat1)
    dlng = math.radians(lng2 - lng1)
    a = math.sin(dlat / 2) ** 2 + math.cos(math.radians(lat1)) * math.cos(math.radians(lat2)) * math.sin(dlng / 2) ** 2
    return 2 * 6371000 * math.atan2(math.sqrt(a), math.sqrt(1 - a))


three_tm = ThreeTMClient()
