from typing import Any, Dict, List

from fastapi import APIRouter, Depends
from sqlalchemy.orm import Session

import auth
import models
import schemas  # noqa: F401
from database import get_db


router = APIRouter()


@router.get("/students")
def list_students(
    risk: str | None = None,
    month: int | None = None,
    db: Session = Depends(get_db),
    admin_user: models.User = Depends(auth.require_role("admin")),
) -> List[Dict[str, Any]]:
    # Basic implementation: return latest risk per student.
    subq = (
        db.query(
            models.StudentIndicatorEntry.student_id,
            models.StudentIndicatorEntry.risk_level,
            models.StudentIndicatorEntry.created_at,
        )
        .order_by(
            models.StudentIndicatorEntry.student_id,
            models.StudentIndicatorEntry.created_at.desc(),
        )
        .subquery()
    )

    query = (
        db.query(models.User, subq.c.risk_level, subq.c.created_at)
        .outerjoin(subq, models.User.id == subq.c.student_id)
        .filter(models.User.role == "student")
    )

    results = []
    for user, risk_level, created_at in query.all():
        if risk and risk_level != risk:
            continue
        results.append(
            {
                "id": user.id,
                "name": user.name,
                "email": user.email,
                "risk_level": risk_level,
                "last_updated": created_at,
            }
        )
    return results


@router.get("/student/{id}/analytics")
def student_analytics(
    id: int,
    db: Session = Depends(get_db),
    admin_user: models.User = Depends(auth.require_role("admin")),
) -> Dict[str, Any]:
    entries = (
        db.query(models.StudentIndicatorEntry)
        .filter(models.StudentIndicatorEntry.student_id == id)
        .order_by(models.StudentIndicatorEntry.created_at.asc())
        .all()
    )
    trend = [
        {
            "created_at": e.created_at,
            "risk_level": e.risk_level,
            "probability": e.probability,
        }
        for e in entries
    ]
    return {"student_id": id, "entries": trend}


@router.get("/dashboard")
def dashboard(
    db: Session = Depends(get_db),
    admin_user: models.User = Depends(auth.require_role("admin")),
) -> Dict[str, Any]:
    total_students = db.query(models.User).filter(models.User.role == "student").count()
    by_risk = (
        db.query(models.StudentIndicatorEntry.risk_level, models.StudentIndicatorEntry.id)
        .all()
    )
    dist: Dict[str, int] = {}
    for risk_level, _ in by_risk:
        if risk_level not in dist:
            dist[risk_level] = 0
        dist[risk_level] += 1
    return {"total_students": total_students, "risk_distribution": dist}


@router.get("/batch/{batch_id}")
def get_batch(
    batch_id: int,
    db: Session = Depends(get_db),
    admin_user: models.User = Depends(auth.require_role("admin")),
) -> Dict[str, Any]:
    job = db.get(models.BatchJob, batch_id)
    if not job:
        return {"detail": "Batch not found"}
    return {
        "batch_id": batch_id,
        "filename": job.filename,
        "status": job.status,
    }

