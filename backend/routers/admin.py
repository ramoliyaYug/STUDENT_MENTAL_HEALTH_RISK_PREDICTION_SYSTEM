import io
from typing import Any, Dict, List

import pandas as pd
from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile
from sqlalchemy.orm import Session

import auth
import models
import schemas
from database import get_db
from routers import ml as ml_router


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


@router.post("/batch/predict", response_model=schemas.AdminBatchPredictResponse)
async def admin_batch_predict_csv(
    file: UploadFile | None = File(None),
    use_project_rawtest: bool = Form(
        False,
        description="If true, uses dataset/rawtest.csv from the repository (no file upload needed).",
    ),
    include_explainability: bool = Form(
        False,
        description="If true, includes per-row SHAP top features for risk (slower on large files).",
    ),
    db: Session = Depends(get_db),
    admin_user: models.User = Depends(auth.require_role("admin")),
) -> schemas.AdminBatchPredictResponse:
    """
    Admin-only: upload a CSV in the same format as dataset/rawtest.csv (Raw Data columns),
    or set use_project_rawtest=true to score the bundled rawtest.csv.
    Returns model predictions for every row (scores, labels, risk, optional explainability).
    """
    filename = "upload.csv"

    if use_project_rawtest:
        if not ml_router.RAW_TEST_PATH.exists():
            raise HTTPException(status_code=404, detail="dataset/rawtest.csv not found in project.")
        df = pd.read_csv(ml_router.RAW_TEST_PATH)
        filename = ml_router.RAW_TEST_PATH.name
    elif file is not None and file.filename:
        raw = await file.read()
        if not raw:
            raise HTTPException(status_code=422, detail="Uploaded file is empty.")
        try:
            df = pd.read_csv(io.BytesIO(raw))
        except Exception as exc:
            raise HTTPException(status_code=422, detail=f"Could not parse CSV: {exc}")
        filename = file.filename
    else:
        raise HTTPException(
            status_code=422,
            detail="Upload a CSV (same columns as dataset/rawtest.csv) or set use_project_rawtest=true.",
        )

    if df.empty:
        raise HTTPException(status_code=422, detail="CSV contains no data rows.")

    X = ml_router.normalize_batch_dataframe(df)
    predictions_raw = ml_router.run_batch_predictions(
        X, include_explainability=include_explainability
    )
    predictions = [schemas.AdminBatchPredictionRow(**row) for row in predictions_raw]

    job = models.BatchJob(filename=filename, status=f"predicted:{len(predictions)}_rows")
    db.add(job)
    db.commit()

    return schemas.AdminBatchPredictResponse(
        filename=filename,
        total_rows=len(predictions),
        predictions=predictions,
    )

