from typing import List

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy.orm import Session

import auth
import models
import schemas
from database import get_db


router = APIRouter()


@router.post("/student/indicators", response_model=schemas.StudentIndicatorOut)
def submit_indicators(
    payload: schemas.StudentIndicatorCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(auth.get_current_user),
):
    # Delegate prediction + persistence to ML router
    from routers.ml import predict as ml_predict

    return ml_predict(payload=payload, db=db, current_user=current_user)


@router.get("/student/history", response_model=List[schemas.StudentIndicatorOut])
def get_history(
    range: str = "weekly",  # noqa: A002
    db: Session = Depends(get_db),
    current_user: models.User = Depends(auth.get_current_user),
):
    q = (
        db.query(models.StudentIndicatorEntry)
        .filter(models.StudentIndicatorEntry.student_id == current_user.id)
        .order_by(models.StudentIndicatorEntry.created_at.desc())
    )
    entries = q.all()
    return entries

