import httpx
from app.config import settings


class EskizService:
    BASE_URL = "https://notify.eskiz.uz/api"

    def __init__(self):
        self.email = settings.ESKIZ_EMAIL
        self.password = settings.ESKIZ_PASSWORD
        self.from_name = settings.ESKIZ_FROM
        self._token: str | None = None

    async def _login(self) -> str | None:
        if not self.email or not self.password:
            return None
        try:
            async with httpx.AsyncClient(timeout=10) as client:
                resp = await client.post(f"{self.BASE_URL}/auth/login", data={
                    "email": self.email,
                    "password": self.password,
                })
                if resp.status_code == 200:
                    data = resp.json()
                    self._token = data.get("data", {}).get("token")
                    return self._token
        except Exception as e:
            print(f"[Eskiz login xatolik] {e}")
        return None

    async def _refresh_token(self) -> str | None:
        if not self._token:
            return None
        try:
            async with httpx.AsyncClient(timeout=10) as client:
                resp = await client.post(
                    f"{self.BASE_URL}/auth/refresh",
                    headers={"Authorization": f"Bearer {self._token}"},
                )
                if resp.status_code == 200:
                    data = resp.json()
                    self._token = data.get("data", {}).get("token")
                    return self._token
        except Exception:
            pass
        return None

    async def send_sms(self, phone_number: str, message: str) -> bool:
        if not self._token:
            await self._login()
        if not self._token:
            return False

        phone_number = phone_number.replace("+", "").replace(" ", "")

        headers = {"Authorization": f"Bearer {self._token}"}
        payload = {
            "mobile_phone": phone_number,
            "message": message,
            "from": self.from_name,
        }

        try:
            async with httpx.AsyncClient(timeout=10) as client:
                resp = await client.post(
                    f"{self.BASE_URL}/message/sms/send",
                    headers=headers,
                    data=payload,
                )
                if resp.status_code in (200, 201):
                    return True
                if resp.status_code == 401:
                    await self._refresh_token()
                    headers["Authorization"] = f"Bearer {self._token}"
                    resp = await client.post(
                        f"{self.BASE_URL}/message/sms/send",
                        headers=headers,
                        data=payload,
                    )
                    return resp.status_code in (200, 201)
                print(f"[Eskiz SMS xatolik] {resp.status_code}: {resp.text[:200]}")
        except Exception as e:
            print(f"[Eskiz SMS xatolik] {e}")
        return False

    async def get_balance(self) -> float | None:
        if not self._token:
            await self._login()
        if not self._token:
            return None
        try:
            async with httpx.AsyncClient(timeout=10) as client:
                resp = await client.get(
                    f"{self.BASE_URL}/auth/user",
                    headers={"Authorization": f"Bearer {self._token}"},
                )
                if resp.status_code == 200:
                    data = resp.json()
                    return float(data.get("data", {}).get("balance", 0))
        except Exception:
            pass
        return None


eskiz_service = EskizService()
