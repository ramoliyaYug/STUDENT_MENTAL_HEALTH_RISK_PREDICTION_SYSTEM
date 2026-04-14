# API Testing Guide

This file contains reusable test steps for the Student Mental Health Risk Prediction backend.

## 1) Start the backend

From `backend/`:

```bash
source venv/bin/activate
uvicorn main:app --reload
```

Base URL:

```text
http://127.0.0.1:8000/api/v1
```

---

## 2) Test order (recommended)

1. Health check
2. Register student (or admin with `"role": "admin"`)
3. Login
4. Get profile (`/auth/me`)
5. Check model info + indicators
6. Submit indicators (`/student/indicators`) — student token
7. Check student history
8. Login as admin and run **batch CSV predict** (`/admin/batch/predict`) — optional: `use_project_rawtest=true` uses `dataset/rawtest.csv`
9. Other admin + analytics endpoints

---

## 3) Auth APIs

### 3.1 Register (Student)

- Method: `POST`
- URL: `/auth/register`
- Body (JSON):

```json
{
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "password": "StrongPass123!",
  "role": "student"
}
```

For an admin account, use `"role": "admin"` (same endpoint).

Response contains:
- `access_token`
- `token_type`

Save token as `STUDENT_TOKEN`.

### 3.2 Login

- Method: `POST`
- URL: `/auth/login`
- Body type: `x-www-form-urlencoded`
- Fields:
  - `username`: `alice@example.com`
  - `password`: `StrongPass123!`

Save returned token as `STUDENT_TOKEN`.

### 3.3 Me

- Method: `GET`
- URL: `/auth/me`
- Headers:
  - `Authorization: Bearer <STUDENT_TOKEN>`

---

## 4) Health API

### 4.1 Health

- Method: `GET`
- URL: `/health`
- No body

---

## 5) ML APIs

### 5.1 Model info

- Method: `GET`
- URL: `/ml/modelInfo`
- No body

This confirms all required `.pkl` models are loaded.

### 5.2 Get accepted indicators

- Method: `GET`
- URL: `/ml/indicators`
- No body

Use this response to see:
- `cleaned_feature_columns`
- `raw_question_to_feature_map`

### 5.3 Explain risk

- Method: `POST`
- URL: `/ml/explain`
- Headers:
  - `Authorization: Bearer <STUDENT_TOKEN>`
- Body (JSON):

```json
{
  "indicators": {
    "Age": "18-22",
    "Gender": "Female",
    "University": "Independent University, Bangladesh (IUB)",
    "Department": "Engineering - CS / CSE / CSC / Similar to CS",
    "Academic_Year": "Fourth Year or Equivalent",
    "CGPA": "2.50 - 2.99",
    "Scholarship": "No",
    "A1_Nervous": 1,
    "A2_Worrying": 1,
    "A3_Relaxing": 1,
    "A4_Irritated": 2,
    "A5_TooMuchWorry": 2,
    "A6_Restless": 2,
    "A7_Afraid": 1,
    "S1_Upset": 2,
    "S2_Uncontrolled": 2,
    "S3_NervousStressed": 3,
    "S4_CannotCope": 2,
    "S5_Confident": 2,
    "S6_ThingsGoingWell": 2,
    "S7_ControlIrritations": 2,
    "S8_PerformanceOnTop": 2,
    "S9_Angered": 2,
    "S10_PilingUp": 2,
    "D1_LittleInterest": 1,
    "D2_Hopeless": 2,
    "D3_SleepTrouble": 1,
    "D4_Tired": 1,
    "D5_Appetite": 2,
    "D6_Failure": 1,
    "D7_Concentration": 1,
    "D8_Psychomotor": 1,
    "D9_SuicidalThoughts": 1
  }
}
```

### 5.4 Direct predict

- Method: `POST`
- URL: `/ml/predict`
- Headers:
  - `Authorization: Bearer <STUDENT_TOKEN>`
- Body: same as above (`indicators` object)

Expected output includes:
- `anxiety_score`, `stress_score`, `depression_score`
- `anxiety_label`, `stress_label`, `depression_label`
- `risk_level`, `probability`
- `explainability` (top SHAP features)

---

## 6) Student APIs

### 6.1 Submit indicators

- Method: `POST`
- URL: `/student/indicators`
- Headers:
  - `Authorization: Bearer <STUDENT_TOKEN>`
- Body: same JSON used in `/ml/predict`

### 6.2 Student history

- Method: `GET`
- URL: `/student/history?range=weekly`
- Headers:
  - `Authorization: Bearer <STUDENT_TOKEN>`

---

## 7) Admin APIs

These endpoints require an admin token.

## 7.1 Create/login admin

The current register API creates `student` role by default.  
Create an admin directly in DB or update role in DB:

```sql
UPDATE users SET role='admin' WHERE email='admin@example.com';
```

Then login and save token as `ADMIN_TOKEN`.

### 7.2 List students

- Method: `GET`
- URL: `/admin/students?risk=Moderate Risk&month=4`
- Headers:
  - `Authorization: Bearer <ADMIN_TOKEN>`

### 7.3 Student analytics

- Method: `GET`
- URL: `/admin/student/1/analytics`
- Headers:
  - `Authorization: Bearer <ADMIN_TOKEN>`

### 7.4 Dashboard

- Method: `GET`
- URL: `/admin/dashboard`
- Headers:
  - `Authorization: Bearer <ADMIN_TOKEN>`

### 7.5 Batch status

- Method: `GET`
- URL: `/admin/batch/1`
- Headers:
  - `Authorization: Bearer <ADMIN_TOKEN>`

### 7.6 Batch CSV predict (all rows scored in response)

- Method: `POST`
- URL: `/admin/batch/predict`
- Headers:
  - `Authorization: Bearer <ADMIN_TOKEN>`
- Body type: `multipart/form-data`
- Fields:
  - `file` (optional): CSV file with the **same columns as** `dataset/rawtest.csv` (Raw Data format). Target columns such as Anxiety Value / Stress Value / Depression Value / labels are ignored if present; models compute predictions.
  - `use_project_rawtest` (optional, boolean): if `true`, the server reads **`dataset/rawtest.csv`** from the repo instead of an uploaded file (easy smoke test).
  - `include_explainability` (optional, boolean): if `true`, each row includes SHAP top features for risk (slower for large files).

**Response shape**

- `filename`: string
- `total_rows`: number of data rows scored
- `predictions`: array of objects, one per CSV row (0-based `row_index`):
  - `anxiety_score`, `stress_score`, `depression_score`
  - `anxiety_label`, `stress_label`, `depression_label`
  - `risk_level`, `risk_probability`
  - `explainability` (only if `include_explainability=true`)

---

## 8) Other batch / upload APIs

### 8.1 Upload CSV (job stub only)

- Method: `POST`
- URL: `/ml/batch/upload`
- Headers:
  - `Authorization: Bearer <ADMIN_TOKEN>`
- Body type: `form-data`
- Field:
  - `file`: choose CSV file

---

## 9) Analytics APIs

### 9.1 Weekly trend

- Method: `GET`
- URL: `/analytics/trend/weekly`
- Headers:
  - `Authorization: Bearer <ADMIN_TOKEN>`

### 9.2 Heatmap

- Method: `GET`
- URL: `/analytics/heatmap`
- Headers:
  - `Authorization: Bearer <ADMIN_TOKEN>`

### 9.3 Export

- Method: `GET`
- URL: `/analytics/export`
- Headers:
  - `Authorization: Bearer <ADMIN_TOKEN>`

---

## 10) Curl quick test examples

### Register

```bash
curl -X POST "http://127.0.0.1:8000/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice Johnson","email":"alice@example.com","password":"StrongPass123!","role":"student"}'
```

### Login

```bash
curl -X POST "http://127.0.0.1:8000/api/v1/auth/login" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=alice@example.com&password=StrongPass123!"
```

### Model info

```bash
curl "http://127.0.0.1:8000/api/v1/ml/modelInfo"
```

### Predict

```bash
curl -X POST "http://127.0.0.1:8000/api/v1/ml/predict" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <STUDENT_TOKEN>" \
  -d '{
    "indicators": {
      "Age": "18-22",
      "Gender": "Female",
      "University": "Independent University, Bangladesh (IUB)",
      "Department": "Engineering - CS / CSE / CSC / Similar to CS",
      "Academic_Year": "Fourth Year or Equivalent",
      "CGPA": "2.50 - 2.99",
      "Scholarship": "No",
      "A1_Nervous": 1,
      "A2_Worrying": 1,
      "A3_Relaxing": 1,
      "A4_Irritated": 2,
      "A5_TooMuchWorry": 2,
      "A6_Restless": 2,
      "A7_Afraid": 1,
      "S1_Upset": 2,
      "S2_Uncontrolled": 2,
      "S3_NervousStressed": 3,
      "S4_CannotCope": 2,
      "S5_Confident": 2,
      "S6_ThingsGoingWell": 2,
      "S7_ControlIrritations": 2,
      "S8_PerformanceOnTop": 2,
      "S9_Angered": 2,
      "S10_PilingUp": 2,
      "D1_LittleInterest": 1,
      "D2_Hopeless": 2,
      "D3_SleepTrouble": 1,
      "D4_Tired": 1,
      "D5_Appetite": 2,
      "D6_Failure": 1,
      "D7_Concentration": 1,
      "D8_Psychomotor": 1,
      "D9_SuicidalThoughts": 1
    }
  }'
```

### Batch predict using bundled `rawtest.csv` (no file upload)

From project root (so the path to `rawtest.csv` exists on disk next to `backend/`):

```bash
curl -X POST "http://127.0.0.1:8000/api/v1/admin/batch/predict" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -F "use_project_rawtest=true" \
  -F "include_explainability=false"
```

### Batch predict by uploading a CSV

Use the same format as `dataset/rawtest.csv` (or `dataset/Raw Data.csv`):

```bash
curl -X POST "http://127.0.0.1:8000/api/v1/admin/batch/predict" \
  -H "Authorization: Bearer <ADMIN_TOKEN>" \
  -F "file=@/home/mastermind/Desktop/extras/STUDENT_MENTAL_HEALTH_RISK_PREDICTION_SYSTEM/dataset/rawtest.csv" \
  -F "include_explainability=false"
```

---

## 11) Common issues

- `password authentication failed for user "postgres"`:
  - Update DB credentials in `database.py`.
- `No models loaded on the server`:
  - Ensure model files are in `backend/models` with `.pkl` extension.
- `Missing required indicators`:
  - Call `GET /api/v1/ml/indicators` and send all listed cleaned feature columns.
- `dataset/rawtest.csv not found` when using `use_project_rawtest=true`:
  - Run the API from the machine that has the repo checked out, or upload the file with `file=@...` instead.
