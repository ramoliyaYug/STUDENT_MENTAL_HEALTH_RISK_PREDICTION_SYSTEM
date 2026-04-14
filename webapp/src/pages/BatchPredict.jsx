import { useState } from 'react'
import * as api from '../api/adminApi'

export default function BatchPredict() {
  const [file, setFile] = useState(null)
  const [useSample, setUseSample] = useState(false)
  const [explain, setExplain] = useState(false)
  const [result, setResult] = useState(null)
  const [stubResult, setStubResult] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  async function runPredict() {
    setError('')
    setLoading(true)
    setResult(null)
    try {
      if (!useSample && !file) {
        setError('Choose a CSV file or enable “Use server rawtest.csv”.')
        return
      }
      const data = await api.batchPredict(useSample ? null : file, {
        use_project_rawtest: useSample,
        include_explainability: explain,
      })
      setResult(data)
    } catch (err) {
      setError(
        err.response?.data?.detail ||
          (typeof err.response?.data === 'string'
            ? err.response.data
            : JSON.stringify(err.response?.data)) ||
          err.message ||
          String(err)
      )
    } finally {
      setLoading(false)
    }
  }

  async function runStubUpload() {
    if (!file) {
      setError('Choose a file for stub upload.')
      return
    }
    setError('')
    setLoading(true)
    setStubResult(null)
    try {
      const data = await api.uploadBatchStub(file)
      setStubResult(data)
    } catch (err) {
      setError(err.response?.data?.detail || err.message || String(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <h1>Batch CSV predict</h1>
      <p className="muted">
        Same as <code>POST /api/v1/admin/batch/predict</code>. Upload a CSV like{' '}
        <code>dataset/rawtest.csv</code>, or use the server-side sample file.
      </p>

      <section className="form">
        <label className="check">
          <input
            type="checkbox"
            checked={useSample}
            onChange={(e) => setUseSample(e.target.checked)}
          />
          Use server <code>rawtest.csv</code> (no file needed)
        </label>
        <label>
          CSV file
          <input
            type="file"
            accept=".csv,text/csv"
            disabled={useSample}
            onChange={(e) => setFile(e.target.files?.[0] || null)}
          />
        </label>
        <label className="check">
          <input
            type="checkbox"
            checked={explain}
            onChange={(e) => setExplain(e.target.checked)}
          />
          Include SHAP explainability (slower)
        </label>
        <div className="row">
          <button type="button" onClick={runPredict} disabled={loading}>
            {loading ? 'Running…' : 'Run batch predict'}
          </button>
          <button type="button" onClick={runStubUpload} disabled={loading || !file}>
            Stub: POST /ml/batch/upload
          </button>
        </div>
      </section>

      {error && <p className="error">{error}</p>}

      {result && (
        <section>
          <h2>
            Results ({result.total_rows} rows) — {result.filename}
          </h2>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Anxiety</th>
                  <th>Stress</th>
                  <th>Depression</th>
                  <th>Risk</th>
                  <th>P(risk)</th>
                </tr>
              </thead>
              <tbody>
                {result.predictions?.map((p) => (
                  <tr key={p.row_index}>
                    <td>{p.row_index}</td>
                    <td>
                      {p.anxiety_score?.toFixed?.(2) ?? p.anxiety_score}{' '}
                      <small>({p.anxiety_label})</small>
                    </td>
                    <td>
                      {p.stress_score?.toFixed?.(2) ?? p.stress_score}{' '}
                      <small>({p.stress_label})</small>
                    </td>
                    <td>
                      {p.depression_score?.toFixed?.(2) ?? p.depression_score}{' '}
                      <small>({p.depression_label})</small>
                    </td>
                    <td>{p.risk_level}</td>
                    <td>
                      {p.risk_probability != null
                        ? p.risk_probability.toFixed(4)
                        : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <details>
            <summary>Raw JSON</summary>
            <pre>{JSON.stringify(result, null, 2)}</pre>
          </details>
        </section>
      )}

      {stubResult && (
        <section>
          <h2>Stub upload response</h2>
          <pre>{JSON.stringify(stubResult, null, 2)}</pre>
        </section>
      )}
    </div>
  )
}
