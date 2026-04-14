# Student Android app

Jetpack Compose UI for **students**: register (`role: student`), login (OAuth2 form), profile (`/auth/me`), health ping, submit questionnaire (`POST /student/indicators`), history (`GET /student/history`).

## API base URL

Edit **`app/src/main/java/yug/ramoliya/ojtapp/data/ApiConfig.kt`**:

- **Emulator → host machine:** `http://10.0.2.2:8000/api/v1/`
- **Physical device:** your PC’s LAN IP, e.g. `http://192.168.1.10:8000/api/v1/`

The value must end with `/`. Paths are defined in `StudentApiService`.

## Backend

Run FastAPI (`uvicorn`) with CORS enabled. The manifest allows **cleartext HTTP** for local dev only; use HTTPS in production and remove `usesCleartextTraffic` or scope it with a network security config.

## Questionnaire JSON

The **Assess** tab expects a JSON **object** of indicator keys/values (same shape as `backend/apitesting.md`), not wrapped in `"indicators"`. Use **Load sample** for a full template.
