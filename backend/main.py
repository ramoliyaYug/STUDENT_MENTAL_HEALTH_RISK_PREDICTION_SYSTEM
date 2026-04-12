from pathlib import Path

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

import models  # noqa: F401
from database import Base, engine, run_startup_migrations
from routers import admin, analytics, auth_routes, ml, student


BASE_DIR = Path(__file__).resolve().parent


app = FastAPI(
    title="Student Mental Health Risk Prediction API",
    version="1.0.0",
    description="Backend API for student mental health risk prediction and analytics.",
)


app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.on_event("startup")
def on_startup() -> None:
    # Ensure tables exist. In production, prefer Alembic migrations instead.
    Base.metadata.create_all(bind=engine)
    run_startup_migrations()


app.include_router(auth_routes.router, prefix="/api/v1/auth", tags=["auth"])
app.include_router(student.router, prefix="/api/v1", tags=["student"])
app.include_router(ml.router, prefix="/api/v1/ml", tags=["ml"])
app.include_router(admin.router, prefix="/api/v1/admin", tags=["admin"])
app.include_router(analytics.router, prefix="/api/v1/analytics", tags=["analytics"])


@app.get("/api/v1/health")
def health_check() -> dict:
    return {"status": "ok"}


@app.get("/api/v1")
def api_root() -> dict:
    return {"message": "Student Mental Health Risk Prediction API v1"}