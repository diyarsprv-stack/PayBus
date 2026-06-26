from pydantic import BaseModel


class SendSMSRequest(BaseModel):
    phone_number: str


class VerifySMSRequest(BaseModel):
    phone_number: str
    code: str


class UserResponse(BaseModel):
    id: str
    phone_number: str
    full_name: str | None
    is_verified: bool

    class Config:
        from_attributes = True


class UpdateProfileRequest(BaseModel):
    full_name: str | None = None
    phone_number: str | None = None


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserResponse
