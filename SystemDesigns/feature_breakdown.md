# 🚀 Feature Breakdown - Student Mental Health Risk Prediction System

---

## 1️⃣ Web Dashboard (Admin / Counsellor)

**Goal:** Monitor student risk and institutional analytics.

**Implementation:**
- React + Tailwind dashboard UI
- Risk distribution charts
- Student filtering (risk, month)
- Individual student analytics view
- CSV batch upload interface

**APIs:** `/admin/dashboard`, `/admin/students`, `/analytics/*`

---

## 2️⃣ Mobile App (Student)

**Goal:** Submit indicators and view personal risk.

**Implementation:**
- Compose Multiplatform app
- Questionnaire form
- Risk result screen
- Risk history screen

**APIs:** `/auth/*`, `/student/indicators`, `/student/history`

---

## 3️⃣ Questionnaire & Data Collection

**Goal:** Collect structured academic, lifestyle, and emotional indicators.

**Implementation:**
- Validated JSON input
- Feature engineering before prediction
- Store raw entries in `indicator_entries` table

---

## 4️⃣ Risk Prediction API

**Goal:** Return **Low / Medium / High** risk.

**Implementation:**
- FastAPI endpoint `/ml/predict`
- Load active model from MLflow
- Run scikit-learn pipeline
- Return risk + confidence
- Store result in `predictions` table

---

## 5️⃣ Data Storage (PostgreSQL)

**Goal:** Persist inputs and prediction results.

**Tables Used:**
- `users`
- `indicator_entries`
- `predictions`
- `shap_explanations`
- `risk_trends`
- `batch_jobs`
- `batch_results`

---

## 6️⃣ Student Risk History & Trends

**Goal:** Show prediction changes over time.

**Implementation:**
- Aggregate predictions weekly/monthly
- Use `risk_trends` table

**API:** `/student/history`

---

## 7️⃣ Explainable Prediction (SHAP)

**Goal:** Show top contributing features.

**Implementation:**
- Generate SHAP values per prediction
- Store in `shap_explanations`
- Display top positive/negative factors

**API:** `/ml/explain`

---

## 8️⃣ Batch CSV Prediction

**Goal:** Score multiple students at once.

**Implementation:**
- Admin uploads CSV
- Backend validates + runs batch inference
- Store in `batch_jobs` & `batch_results`

**API:** `/admin/batch/*`