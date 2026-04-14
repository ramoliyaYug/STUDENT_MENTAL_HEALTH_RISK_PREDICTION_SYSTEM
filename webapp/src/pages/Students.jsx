import { useState } from 'react'
import * as api from '../api/adminApi'

export default function Students() {
  const [risk, setRisk] = useState('')
  const [month, setMonth] = useState('')
  const [list, setList] = useState(null)
  const [analyticsId, setAnalyticsId] = useState('1')
  const [analytics, setAnalytics] = useState(null)
  const [batchId, setBatchId] = useState('1')
  const [batch, setBatch] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function loadStudents() {
    setError('')
    setLoading(true)
    try {
      const params = {}
      if (risk.trim()) params.risk = risk.trim()
      if (month.trim()) params.month = Number(month)
      const data = await api.listStudents(params)
      setList(data)
    } catch (err) {
      setError(err.response?.data?.detail || err.message || String(err))
      setList(null)
    } finally {
      setLoading(false)
    }
  }

  async function loadAnalytics() {
    setError('')
    setLoading(true)
    try {
      const id = Number(analyticsId)
      if (Number.isNaN(id)) {
        setError('Student id must be a number')
        return
      }
      const data = await api.getStudentAnalytics(id)
      setAnalytics(data)
    } catch (err) {
      setError(err.response?.data?.detail || err.message || String(err))
      setAnalytics(null)
    } finally {
      setLoading(false)
    }
  }

  async function loadBatch() {
    setError('')
    setLoading(true)
    try {
      const id = Number(batchId)
      if (Number.isNaN(id)) {
        setError('Batch id must be a number')
        return
      }
      const data = await api.getBatchJob(id)
      setBatch(data)
    } catch (err) {
      setError(err.response?.data?.detail || err.message || String(err))
      setBatch(null)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <h1>Students</h1>

      <section>
        <h2>List students</h2>
        <div className="row">
          <label>
            Risk filter (optional)
            <input
              value={risk}
              onChange={(e) => setRisk(e.target.value)}
              placeholder="e.g. Moderate Risk"
            />
          </label>
          <label>
            Month (optional)
            <input
              value={month}
              onChange={(e) => setMonth(e.target.value)}
              placeholder="4"
            />
          </label>
          <button type="button" onClick={loadStudents} disabled={loading}>
            Load
          </button>
        </div>
        <pre>{list ? JSON.stringify(list, null, 2) : '—'}</pre>
      </section>

      <section>
        <h2>Student analytics</h2>
        <div className="row">
          <label>
            Student id
            <input
              value={analyticsId}
              onChange={(e) => setAnalyticsId(e.target.value)}
            />
          </label>
          <button type="button" onClick={loadAnalytics} disabled={loading}>
            Load analytics
          </button>
        </div>
        <pre>{analytics ? JSON.stringify(analytics, null, 2) : '—'}</pre>
      </section>

      <section>
        <h2>Batch job status</h2>
        <div className="row">
          <label>
            Batch id
            <input
              value={batchId}
              onChange={(e) => setBatchId(e.target.value)}
            />
          </label>
          <button type="button" onClick={loadBatch} disabled={loading}>
            Load batch
          </button>
        </div>
        <pre>{batch ? JSON.stringify(batch, null, 2) : '—'}</pre>
      </section>

      {error && <p className="error">{error}</p>}
    </div>
  )
}
