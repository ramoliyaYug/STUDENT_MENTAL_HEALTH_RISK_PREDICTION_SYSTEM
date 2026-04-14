/**
 * Backend origin. Set in `.env` as VITE_BACKEND_URL (see `.env.example`).
 * @type {string}
 */
export const BACKEND_URL =
  import.meta.env.VITE_BACKEND_URL?.replace(/\/$/, '') || 'http://127.0.0.1:8000'

export const API_BASE = `${BACKEND_URL}/api/v1`
