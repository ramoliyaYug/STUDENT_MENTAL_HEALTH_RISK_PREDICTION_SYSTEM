from datetime import datetime
from typing import Any, Dict, List, Literal, Optional

from pydantic import BaseModel, EmailStr, Field


class Token(BaseModel):
    access_token: str
    token_type: str = "bearer"


class TokenData(BaseModel):
    user_id: int
    role: str


class UserBase(BaseModel):
    name: str
    email: EmailStr


class UserCreate(UserBase):
    password: str
    role: Literal["student", "admin"] = "student"


class UserLogin(BaseModel):
    email: EmailStr
    password: str


class UserOut(UserBase):
    id: int
    role: str
    created_at: datetime

    class Config:
        from_attributes = True


class StudentIndicatorCreate(BaseModel):
    indicators: Dict[str, Any]
    model_name: Optional[str] = None


class StudentIndicatorOut(BaseModel):
    id: int
    student_id: int
    indicators: Dict[str, Any]
    risk_level: str
    probability: Optional[float] = None
    anxiety_score: Optional[float] = None
    stress_score: Optional[float] = None
    depression_score: Optional[float] = None
    anxiety_label: Optional[str] = None
    stress_label: Optional[str] = None
    depression_label: Optional[str] = None
    explainability: Optional[List[Dict[str, Any]]] = None
    model_used: str
    created_at: datetime

    class Config:
        from_attributes = True


class WeeklyTrendPoint(BaseModel):
    week_start: datetime
    low: int
    medium: int
    high: int


class HeatmapPoint(BaseModel):
    day: datetime
    risk_level: str
    count: int


class ExplainabilityRequest(BaseModel):
    indicators: Dict[str, Any]


class ExplainabilityResponse(BaseModel):
    risk_level: str
    probability: Optional[float]
    class_probabilities: Dict[str, float]
    top_features: List[Dict[str, Any]]

