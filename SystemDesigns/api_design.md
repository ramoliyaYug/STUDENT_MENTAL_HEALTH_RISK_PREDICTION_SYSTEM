## API Endpoints – Student Mental Health Risk Prediction System

### Base URL
`/api/v1`

---

## Authentication APIs

| Method | Endpoint | Role | Description | Request Body | Response |
|--------|---------|------|------------|-------------|---------|
| POST | `/auth/register` | Public | Register new student | name, email, password | JWT token + role |
| POST | `/auth/login` | Public | User login | email, password | JWT token + role |
| GET | `/auth/me` | Student/Admin | Get logged-in profile | — | User profile data |

---

## Student APIs

| Method | Endpoint | Role | Description | Request Body | Response |
|--------|---------|------|------------|-------------|---------|
| POST | `/student/indicators` | Student | Submit questionnaire & trigger ML prediction | Indicator fields | Risk level + SHAP explanation |
| GET | `/student/history` | Student | Get historical entries | `range=weekly/monthly` | List of risk entries |

---

## ML / Prediction APIs

| Method | Endpoint | Role | Description | Request Body | Response |
|--------|---------|------|------------|-------------|---------|
| POST | `/ml/predict` | Internal/Admin | Direct risk prediction | Indicator JSON | Risk level |
| POST | `/ml/explain` | Internal/Admin | Generate SHAP explanation | Features JSON | SHAP factors |
| GET | `/ml/modelInfo` | Admin | Get current model details | — | Model name, metrics |

---

## Counsellor / Admin APIs

| Method | Endpoint | Role | Description | Request Body | Response |
|--------|---------|------|------------|-------------|---------|
| GET | `/admin/students` | Admin | List students with filters | `risk, month` | Student list |
| GET | `/admin/student/{id}/analytics` | Admin | Individual student analytics | — | Trends + variances |
| GET | `/admin/dashboard` | Admin | Institution summary dashboard | — | Risk distribution |

---

## Batch CSV APIs

| Method | Endpoint | Role | Description | Request Body | Response |
|--------|---------|------|------------|-------------|---------|
| POST | `/admin/batch/upload` | Admin | Upload CSV for batch scoring | CSV file | Batch ID + status |
| GET | `/admin/batch/{batch_id}` | Admin | Get batch results | — | Risk distribution report |

---

## Analytics APIs

| Method | Endpoint | Role | Description | Request Body | Response |
|--------|---------|------|------------|-------------|---------|
| GET | `/analytics/trend/weekly` | Admin | Weekly trend data | — | Trend dataset |
| GET | `/analytics/heatmap` | Admin | Heatmap analytics data | — | Heatmap dataset |
| GET | `/analytics/export` | Admin | Export analytics CSV | — | CSV file |