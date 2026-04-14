import { useEffect, useState } from 'react'
import { BACKEND_URL } from '../config'
import * as api from '../api/adminApi'

export default function Dashboard() {
  const [health, setHealth] = useState(null)
  const [dash, setDash] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const [h, d] = await Promise.all([api.getHealth(), api.getDashboard()])
        if (!cancelled) {
          setHealth(h)
          setDash(d)
        }
      } catch (err) {
        if (!cancelled) {
          setError(err.response?.data?.detail || err.message || String(err))
        }
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div className="page">
      <h1>Dashboard</h1>
      <p className="muted">
        API: <code>{BACKEND_URL}</code>
      </p>
      {error && <p className="error">{error}</p>}
      <section>
        <h2>Health</h2>
        <pre>{health ? JSON.stringify(health, null, 2) : '…'}</pre>
      </section>
      <section>
        <h2>Admin summary</h2>
        <pre>{dash ? JSON.stringify(dash, null, 2) : '…'}</pre>
      </section>
    </div>
  )
}
