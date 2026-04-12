from datetime import datetime
from typing import Any, Dict, List

from fastapi import APIRouter, Depends
from sqlalchemy import extract, func
from sqlalchemy.orm import Session

import auth
import models
from database import get_db


router = APIRouter()


@router.get("/trend/weekly")
def weekly_trend(
    db: Session = Depends(get_db),
    admin_user: models.User = Depends(auth.require_role("admin")),
) -> List[Dict[str, Any]]:
    # Group entries by ISO week and risk_level.
    rows = (
        db.query(
            func.date_trunc("week", models.StudentIndicatorEntry.created_at).label("week_start"),
            models.StudentIndicatorEntry.risk_level,
            func.count(models.StudentIndicatorEntry.id),
        )
        .group_by("week_start", models.StudentIndicatorEntry.risk_level)
        .order_by("week_start")
        .all()
    )
    trend: Dict[datetime, Dict[str, Any]] = {}
    for week_start, risk_level, count in rows:
        if week_start not in trend:
            trend[week_start] = {"week_start": week_start, "low": 0, "medium": 0, "high": 0}
        trend[week_start][risk_level] = count
    return list(trend.values())


@router.get("/heatmap")
def heatmap(
    db: Session = Depends(get_db),
    admin_user: models.User = Depends(auth.require_role("admin")),
) -> List[Dict[str, Any]]:
    rows = (
        db.query(
            func.date_trunc("day", models.StudentIndicatorEntry.created_at).label("day"),
            models.StudentIndicatorEntry.risk_level,
            func.count(models.StudentIndicatorEntry.id),
        )
        .group_by("day", models.StudentIndicatorEntry.risk_level)
        .order_by("day")
        .all()
    )
    return [
        {"day": day, "risk_level": risk_level, "count": count}
        for day, risk_level, count in rows
    ]


@router.get("/export")
def export(
    db: Session = Depends(get_db),
    admin_user: models.User = Depends(auth.require_role("admin")),
) -> List[Dict[str, Any]]:
    # Simple JSON "export"; frontend can convert to CSV as needed.
    entries = (
        db.query(models.StudentIndicatorEntry)
        .order_by(models.StudentIndicatorEntry.created_at.asc())
        .all()
    )
    return [
        {
            "student_id": e.student_id,
            "created_at": e.created_at,
            "risk_level": e.risk_level,
            "probability": e.probability,
            "model_used": e.model_used,
        }
        for e in entries
    ]

