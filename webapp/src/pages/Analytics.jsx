import { useState } from 'react'
import * as api from '../api/adminApi'

export default function Analytics() {
  const [weekly, setWeekly] = useState(null)
  const [heatmap, setHeatmap] = useState(null)
  const [exp, setExp] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function loadWeekly() {
    setError('')
    setLoading(true)
    try {
      setWeekly(await api.getWeeklyTrend())
    } catch (err) {
      setError(err.response?.data?.detail || err.message)
      setWeekly(null)
    } finally {
      setLoading(false)
    }
  }

  async function loadHeatmap() {
    setError('')
    setLoading(true)
    try {
      setHeatmap(await api.getHeatmap())
    } catch (err) {
      setError(err.response?.data?.detail || err.message)
      setHeatmap(null)
    } finally {
      setLoading(false)
    }
  }

  async function loadExport() {
    setError('')
    setLoading(true)
    try {
      setExp(await api.getAnalyticsExport())
    } catch (err) {
      setError(err.response?.data?.detail || err.message)
      setExp(null)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <h1>Analytics</h1>
      <div className="row">
        <button type="button" onClick={loadWeekly} disabled={loading}>
          Weekly trend
        </button>
        <button type="button" onClick={loadHeatmap} disabled={loading}>
          Heatmap
        </button>
        <button type="button" onClick={loadExport} disabled={loading}>
          Export (JSON)
        </button>
      </div>
      {error && <p className="error">{error}</p>}
      <section>
        <h2>Weekly trend</h2>
        <pre>{weekly ? JSON.stringify(weekly, null, 2) : '—'}</pre>
      </section>
      <section>
        <h2>Heatmap</h2>
        <pre>{heatmap ? JSON.stringify(heatmap, null, 2) : '—'}</pre>
      </section>
      <section>
        <h2>Export</h2>
        <pre>{exp ? JSON.stringify(exp, null, 2) : '—'}</pre>
      </section>
    </div>
  )
}
