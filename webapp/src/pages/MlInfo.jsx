import { useEffect, useState } from 'react'
import * as api from '../api/adminApi'

export default function MlInfo() {
  const [info, setInfo] = useState(null)
  const [indicators, setIndicators] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const [i, ind] = await Promise.all([
          api.getModelInfo(),
          api.getMlIndicators(),
        ])
        if (!cancelled) {
          setInfo(i)
          setIndicators(ind)
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
      <h1>ML info</h1>
      <p className="muted">Public endpoints: model list and indicator columns.</p>
      {error && <p className="error">{error}</p>}
      <section>
        <h2>Model info</h2>
        <pre>{info ? JSON.stringify(info, null, 2) : '…'}</pre>
      </section>
      <section>
        <h2>Indicators</h2>
        <pre>{indicators ? JSON.stringify(indicators, null, 2) : '…'}</pre>
      </section>
    </div>
  )
}
