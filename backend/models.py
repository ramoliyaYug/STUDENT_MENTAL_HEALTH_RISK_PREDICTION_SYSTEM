from datetime import datetime
from typing import Optional

from sqlalchemy import JSON, Boolean, Column, DateTime, Enum, Float, ForeignKey, Integer, String, Text
from sqlalchemy.orm import relationship

from database import Base


class UserRoleEnum(str):
    STUDENT = "student"
    ADMIN = "admin"


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    name = Column(String(255), nullable=False)
    email = Column(String(255), unique=True, index=True, nullable=False)
    hashed_password = Column(String(255), nullable=False)
    role = Column(String(50), nullable=False, default=UserRoleEnum.STUDENT)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    indicators = relationship("StudentIndicatorEntry", back_populates="student")


class StudentIndicatorEntry(Base):
    __tablename__ = "student_indicator_entries"

    id = Column(Integer, primary_key=True, index=True)
    student_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    indicators = Column(JSON, nullable=False)  # raw questionnaire / features
    risk_level = Column(String(50), nullable=False)
    probability = Column(Float, nullable=True)
    shap_values = Column(JSON, nullable=True)  # optional SHAP explanation
    anxiety_score = Column(Float, nullable=True)
    stress_score = Column(Float, nullable=True)
    depression_score = Column(Float, nullable=True)
    anxiety_label = Column(String(100), nullable=True)
    stress_label = Column(String(100), nullable=True)
    depression_label = Column(String(100), nullable=True)
    model_used = Column(String(255), nullable=False)
    created_at = Column(DateTime, default=datetime.utcnow, index=True)

    student = relationship("User", back_populates="indicators")


class BatchJob(Base):
    __tablename__ = "batch_jobs"

    id = Column(Integer, primary_key=True, index=True)
    filename = Column(String(255), nullable=False)
    status = Column(String(50), default="pending")
    created_at = Column(DateTime, default=datetime.utcnow)
    completed_at = Column(DateTime, nullable=True)

