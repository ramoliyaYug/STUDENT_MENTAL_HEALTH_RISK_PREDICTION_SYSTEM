from pathlib import Path
from typing import Any, Dict, List, Optional, Tuple

import joblib
import numpy as np
import pandas as pd
from fastapi import APIRouter, Depends, HTTPException, UploadFile, File
from sklearn.preprocessing import LabelEncoder, OrdinalEncoder
from sqlalchemy.orm import Session

import auth
import models
import schemas
from database import get_db


BASE_DIR = Path(__file__).resolve().parent.parent
MODELS_DIR = BASE_DIR / "models"
PROJECT_DIR = BASE_DIR.parent
RAW_DATA_PATH = PROJECT_DIR / "dataset" / "Raw Data.csv"
CLEAN_DATA_PATH = PROJECT_DIR / "dataset" / "cleaned_data_with_risk.csv"
RAW_TEST_PATH = PROJECT_DIR / "dataset" / "rawtest.csv"

# Columns to ignore if present (raw CSV like Raw Data.csv / rawtest.csv)
RAW_CSV_DROP_COLUMNS = {
    "Anxiety Value",
    "Anxiety Label",
    "Stress Value",
    "Stress Label",
    "Depression Value",
    "Depression Label",
}
CLEAN_CSV_DROP_COLUMNS = {
    "Anxiety_Score",
    "Stress_Score",
    "Depression_Score",
    "Anxiety_Label",
    "Stress_Label",
    "Depression_Label",
    "Risk_Level",
}

router = APIRouter()

_MODEL_REGISTRY: Dict[str, Any] = {}
FEATURE_COLS: List[str] = []
RAW_TO_CLEAN_FEATURE_MAP: Dict[str, str] = {}

ORDINAL_FEATURES = ["Age", "Academic_Year", "CGPA"]
BINARY_FEATURE_MAP = {
    "Gender": {"Male": 1, "Female": 0},
    "Scholarship": {"Yes": 1, "No": 0},
}

_ORDINAL_ENCODER: Optional[OrdinalEncoder] = None
_UNIVERSITY_ENCODER: Optional[LabelEncoder] = None
_DEPARTMENT_ENCODER: Optional[LabelEncoder] = None

MODEL_NAMES = {
    "anxiety_score": "anxiety_score_model_rforest",
    "stress_score": "stress_score_model_rforest",
    "depression_score": "depression_score_model_rforest",
    "anxiety_label": "anxiety_label_model_rforest",
    "stress_label": "stress_label_model_rforest",
    "depression_label": "depression_label_model_rforest",
    "risk_level": "risk_level_model_rforest",
    "risk_shap_bundle": "risk_level_shap_explainer_bundle",
}


def _discover_model_files() -> List[Path]:
    if not MODELS_DIR.exists():
        return []
    candidates: List[Path] = []
    for ext in ("*.pkl", "*.joblib", "*.sav"):
        candidates.extend(MODELS_DIR.glob(ext))
    return sorted(candidates)


def _model_name_from_path(path: Path) -> str:
    return path.stem


def load_models() -> None:
    global _MODEL_REGISTRY

    model_files = _discover_model_files()
    if not model_files:
        return

    for mf in model_files:
        try:
            model = joblib.load(mf)
        except Exception:
            continue
        name = _model_name_from_path(mf)
        _MODEL_REGISTRY[name] = {"model": model, "path": str(mf)}

def load_feature_map_and_encoders() -> None:
    global FEATURE_COLS, RAW_TO_CLEAN_FEATURE_MAP
    global _ORDINAL_ENCODER, _UNIVERSITY_ENCODER, _DEPARTMENT_ENCODER

    if not RAW_DATA_PATH.exists() or not CLEAN_DATA_PATH.exists():
        return

    raw_df = pd.read_csv(RAW_DATA_PATH, nrows=1)
    clean_df = pd.read_csv(CLEAN_DATA_PATH)

    clean_targets = {
        "Anxiety_Score",
        "Stress_Score",
        "Depression_Score",
        "Anxiety_Label",
        "Stress_Label",
        "Depression_Label",
        "Risk_Level",
    }
    raw_targets = {
        "Anxiety Value",
        "Stress Value",
        "Depression Value",
        "Anxiety Label",
        "Stress Label",
        "Depression Label",
    }

    raw_feature_cols = [c for c in raw_df.columns if c not in raw_targets]
    FEATURE_COLS = [c for c in clean_df.columns if c not in clean_targets]

    if len(raw_feature_cols) == len(FEATURE_COLS):
        RAW_TO_CLEAN_FEATURE_MAP = dict(zip(raw_feature_cols, FEATURE_COLS))
    else:
        RAW_TO_CLEAN_FEATURE_MAP = {c: c for c in FEATURE_COLS}

    _ORDINAL_ENCODER = OrdinalEncoder()
    _ORDINAL_ENCODER.fit(clean_df[ORDINAL_FEATURES])

    _UNIVERSITY_ENCODER = LabelEncoder()
    _UNIVERSITY_ENCODER.fit(clean_df["University"])

    _DEPARTMENT_ENCODER = LabelEncoder()
    _DEPARTMENT_ENCODER.fit(clean_df["Department"])


def normalize_batch_dataframe(df: pd.DataFrame) -> pd.DataFrame:
    if not FEATURE_COLS:
        raise HTTPException(status_code=500, detail="Feature configuration is not loaded.")

    out = df.copy()
    for col in list(out.columns):
        if col in RAW_CSV_DROP_COLUMNS or col in CLEAN_CSV_DROP_COLUMNS:
            out = out.drop(columns=[col])

    rename_map = {k: v for k, v in RAW_TO_CLEAN_FEATURE_MAP.items() if k in out.columns}
    if rename_map:
        out = out.rename(columns=rename_map)

    missing = [col for col in FEATURE_COLS if col not in out.columns]
    if missing:
        raise HTTPException(
            status_code=422,
            detail=f"Missing required columns ({len(missing)}): {missing}. "
            "CSV must match dataset/rawtest.csv or Raw Data.csv feature columns.",
        )

    out = out[FEATURE_COLS].copy()

    question_cols = [
        c
        for c in FEATURE_COLS
        if c not in ORDINAL_FEATURES and c not in ("Gender", "Scholarship", "University", "Department")
    ]
    for col in question_cols:
        out[col] = pd.to_numeric(out[col], errors="coerce")
    if out[question_cols].isna().any().any():
        bad = out[question_cols].isna().stack()
        bad_idx = bad[bad].index.tolist()[:20]
        raise HTTPException(
            status_code=422,
            detail=f"Invalid or non-numeric questionnaire value(s) in CSV (first issues): {bad_idx}",
        )

    assert _ORDINAL_ENCODER is not None
    assert _UNIVERSITY_ENCODER is not None
    assert _DEPARTMENT_ENCODER is not None

    try:
        out[ORDINAL_FEATURES] = _ORDINAL_ENCODER.transform(out[ORDINAL_FEATURES])
    except Exception as exc:
        raise HTTPException(status_code=422, detail=f"Invalid ordinal field value: {exc}")

    for col, mapping in BINARY_FEATURE_MAP.items():
        out[col] = out[col].map(mapping)
        if out[col].isna().any():
            raise HTTPException(
                status_code=422,
                detail=f"Invalid value for '{col}'. Allowed: {list(mapping.keys())}",
            )

    try:
        out["University"] = _UNIVERSITY_ENCODER.transform(out["University"])
        out["Department"] = _DEPARTMENT_ENCODER.transform(out["Department"])
    except Exception as exc:
        raise HTTPException(status_code=422, detail=f"Unknown University/Department value: {exc}")

    return out


def _normalize_and_encode_input(indicators: Dict[str, Any]) -> pd.DataFrame:
    return normalize_batch_dataframe(pd.DataFrame([indicators]))


def _predict_model_array(model_name: str, X: pd.DataFrame) -> Tuple[np.ndarray, Optional[np.ndarray]]:
    if model_name not in _MODEL_REGISTRY:
        raise HTTPException(status_code=500, detail=f"Required model '{model_name}' not found.")
    model = _MODEL_REGISTRY[model_name]["model"]
    preds = model.predict(X)
    proba = model.predict_proba(X) if hasattr(model, "predict_proba") else None
    return preds, proba


def _predict_with_model(model_name: str, X: pd.DataFrame) -> Tuple[Any, Optional[np.ndarray]]:
    preds, proba = _predict_model_array(model_name, X)
    p0 = preds[0] if getattr(preds, "shape", ()) != () else preds
    pr0 = proba[0] if proba is not None else None
    return p0, pr0


def run_batch_predictions(X: pd.DataFrame, include_explainability: bool = False) -> List[Dict[str, Any]]:
    if not _MODEL_REGISTRY:
        raise HTTPException(status_code=500, detail="No models loaded on the server.")

    anxiety_s, _ = _predict_model_array(MODEL_NAMES["anxiety_score"], X)
    stress_s, _ = _predict_model_array(MODEL_NAMES["stress_score"], X)
    depression_s, _ = _predict_model_array(MODEL_NAMES["depression_score"], X)
    anxiety_l, _ = _predict_model_array(MODEL_NAMES["anxiety_label"], X)
    stress_l, _ = _predict_model_array(MODEL_NAMES["stress_label"], X)
    depression_l, _ = _predict_model_array(MODEL_NAMES["depression_label"], X)
    risk_level, risk_proba = _predict_model_array(MODEL_NAMES["risk_level"], X)

    risk_prob_per_row: Optional[np.ndarray] = None
    if risk_proba is not None:
        risk_prob_per_row = np.max(risk_proba, axis=1)

    n = len(X)
    rows: List[Dict[str, Any]] = []
    for i in range(n):
        rk = str(risk_level[i])
        ex = _explain_risk(X.iloc[[i]], rk) if include_explainability else None
        rows.append(
            {
                "row_index": i,
                "anxiety_score": float(anxiety_s[i]),
                "stress_score": float(stress_s[i]),
                "depression_score": float(depression_s[i]),
                "anxiety_label": str(anxiety_l[i]),
                "stress_label": str(stress_l[i]),
                "depression_label": str(depression_l[i]),
                "risk_level": rk,
                "risk_probability": float(risk_prob_per_row[i]) if risk_prob_per_row is not None else None,
                "explainability": ex,
            }
        )
    return rows


def _explain_risk(X: pd.DataFrame, predicted_risk: str) -> List[Dict[str, Any]]:
    bundle_name = MODEL_NAMES["risk_shap_bundle"]
    if bundle_name not in _MODEL_REGISTRY:
        return []

    try:
        bundle = _MODEL_REGISTRY[bundle_name]["model"]
        model = bundle["model"]
        scaler = bundle["scaler"]
        feature_cols = bundle["feature_cols"]
        class_names = bundle["class_names"]
        explainer = bundle["explainer"]

        sample_scaled = scaler.transform(X[feature_cols])
        sample_scaled_df = pd.DataFrame(sample_scaled, columns=feature_cols, index=X.index)
        shap_values = explainer.shap_values(sample_scaled_df)

        class_idx = class_names.index(predicted_risk) if predicted_risk in class_names else 0

        if isinstance(shap_values, np.ndarray):
            pred_shap = shap_values[0, :, class_idx]
        else:
            pred_shap = shap_values[class_idx][0]

        top_idx = np.argsort(np.abs(pred_shap))[::-1][:10]
        return [
            {
                "feature": str(np.array(feature_cols)[i]),
                "shap_value": float(pred_shap[i]),
                "abs_shap_value": float(abs(pred_shap[i])),
            }
            for i in top_idx
        ]
    except Exception:
        return []


@router.on_event("startup")
def startup_event() -> None:
    load_models()
    load_feature_map_and_encoders()


@router.post("/predict", response_model=schemas.StudentIndicatorOut)
def predict(
    payload: schemas.StudentIndicatorCreate,
    db: Session = Depends(get_db),
    current_user: models.User = Depends(auth.get_current_user),
) -> schemas.StudentIndicatorOut:
    if not _MODEL_REGISTRY:
        raise HTTPException(status_code=500, detail="No models loaded on the server.")

    X = _normalize_and_encode_input(payload.indicators)

    anxiety_score, _ = _predict_with_model(MODEL_NAMES["anxiety_score"], X)
    stress_score, _ = _predict_with_model(MODEL_NAMES["stress_score"], X)
    depression_score, _ = _predict_with_model(MODEL_NAMES["depression_score"], X)

    anxiety_label, _ = _predict_with_model(MODEL_NAMES["anxiety_label"], X)
    stress_label, _ = _predict_with_model(MODEL_NAMES["stress_label"], X)
    depression_label, _ = _predict_with_model(MODEL_NAMES["depression_label"], X)

    risk_level, risk_proba = _predict_with_model(MODEL_NAMES["risk_level"], X)
    prob = float(np.max(risk_proba)) if risk_proba is not None else None
    explainability = _explain_risk(X, str(risk_level))

    entry = models.StudentIndicatorEntry(
        student_id=current_user.id,
        indicators=payload.indicators,
        risk_level=str(risk_level),
        probability=prob,
        model_used="multi_model_bundle",
        shap_values=explainability,
        anxiety_score=float(anxiety_score),
        stress_score=float(stress_score),
        depression_score=float(depression_score),
        anxiety_label=str(anxiety_label),
        stress_label=str(stress_label),
        depression_label=str(depression_label),
    )
    db.add(entry)
    db.commit()
    db.refresh(entry)
    result = schemas.StudentIndicatorOut.model_validate(entry)
    result.explainability = explainability
    return result


@router.get("/modelInfo")
def model_info() -> Dict[str, Any]:
    return {
        "models": [
            {"name": name, "path": entry["path"]} for name, entry in _MODEL_REGISTRY.items()
        ],
        "required_models": MODEL_NAMES,
        "feature_count": len(FEATURE_COLS),
    }


@router.get("/indicators")
def indicators() -> Dict[str, Any]:
    return {
        "cleaned_feature_columns": FEATURE_COLS,
        "raw_question_to_feature_map": RAW_TO_CLEAN_FEATURE_MAP,
    }


@router.post("/explain", response_model=schemas.ExplainabilityResponse)
def explain(
    payload: schemas.ExplainabilityRequest,
    current_user: models.User = Depends(auth.get_current_user),
) -> schemas.ExplainabilityResponse:
    X = _normalize_and_encode_input(payload.indicators)
    risk_level, risk_proba = _predict_with_model(MODEL_NAMES["risk_level"], X)

    class_probs: Dict[str, float] = {}
    if risk_proba is not None:
        risk_model = _MODEL_REGISTRY[MODEL_NAMES["risk_level"]]["model"]
        classes = [str(c) for c in getattr(risk_model, "classes_", [])]
        class_probs = {c: float(p) for c, p in zip(classes, risk_proba)}

    top_features = _explain_risk(X, str(risk_level))
    return schemas.ExplainabilityResponse(
        risk_level=str(risk_level),
        probability=float(np.max(risk_proba)) if risk_proba is not None else None,
        class_probabilities=class_probs,
        top_features=top_features,
    )


@router.post("/batch/upload")
async def batch_upload(
    file: UploadFile = File(...),
    db: Session = Depends(get_db),
    current_user: models.User = Depends(auth.require_role("admin")),
) -> Dict[str, str]:
    filename = file.filename or "uploaded.csv"
    job = models.BatchJob(filename=filename, status="received")
    db.add(job)
    db.commit()
    db.refresh(job)
    return {"batch_id": str(job.id), "status": job.status}

