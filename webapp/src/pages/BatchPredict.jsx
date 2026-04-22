import { useState } from 'react'
import * as api from '../api/adminApi'
import { FileUp, FileSpreadsheet, ShieldCheck, Zap, Loader2, AlertCircle, CheckCircle2, FileText, ChevronDown, Download } from 'lucide-react'

export default function BatchPredict() {
  const [file, setFile] = useState(null)
  const [useSample, setUseSample] = useState(false)
  const [explain, setExplain] = useState(false)
  const [result, setResult] = useState(null)
  const [stubResult, setStubResult] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  function escapeCsvCell(value) {
    if (value === null || value === undefined) return ''
    const stringValue = String(value)
    if (stringValue.includes('"') || stringValue.includes(',') || stringValue.includes('\n')) {
      return `"${stringValue.replace(/"/g, '""')}"`
    }
    return stringValue
  }

  function exportResultsCsv() {
    if (!result?.predictions?.length) return

    const headers = [
      'row_index',
      'anxiety_score',
      'stress_score',
      'depression_score',
      'anxiety_label',
      'stress_label',
      'depression_label',
      'risk_level',
      'risk_probability',
    ]

    const rows = result.predictions.map((p) => [
      p.row_index,
      p.anxiety_score,
      p.stress_score,
      p.depression_score,
      p.anxiety_label,
      p.stress_label,
      p.depression_label,
      p.risk_level,
      p.risk_probability,
    ])

    const csv = [headers, ...rows]
      .map((row) => row.map(escapeCsvCell).join(','))
      .join('\n')

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `batch_predictions_${new Date().toISOString().replace(/[:.]/g, '-')}.csv`
    anchor.click()
    URL.revokeObjectURL(url)
  }

  async function runPredict() {
    setError('')
    setLoading(true)
    setResult(null)
    try {
      if (!useSample && !file) {
        setError('Please select a valid CSV file or use the server sample.')
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
          'Batch processing failed'
      )
    } finally {
      setLoading(false)
    }
  }

  async function runStubUpload() {
    if (!file) {
      setError('Select a file to upload to the ML stub.')
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
    <div>
      <div style={{ marginBottom: '2.5rem' }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '0.25rem' }}>Batch Predictions</h1>
        <p style={{ color: 'var(--text-muted)' }}>Perform high-volume assessments using CSV dataset uploads</p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', alignItems: 'start' }}>
        {/* ── Configuration ────────────────────────────────────────── */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
            <FileUp size={20} color="var(--primary)" />
            <h2 style={{ margin: 0, fontSize: '1.1rem' }}>Upload Configuration</h2>
          </div>

          <div style={{ marginBottom: '1.5rem' }}>
            <label style={{ 
              display: 'flex', 
              alignItems: 'center', 
              gap: '0.75rem', 
              padding: '1rem', 
              backgroundColor: useSample ? 'var(--primary-glow)' : 'var(--bg-dark)',
              border: `1px solid ${useSample ? 'var(--primary)' : 'var(--border)'}`,
              borderRadius: '12px',
              cursor: 'pointer',
              transition: 'all 0.2s'
            }}>
              <input
                type="checkbox"
                checked={useSample}
                onChange={(e) => setUseSample(e.target.checked)}
                style={{ width: '18px', height: '18px' }}
              />
              <div>
                <p style={{ fontSize: '0.9rem', fontWeight: 600 }}>Use Server Sample File</p>
                <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Uses project-local rawtest.csv (no upload needed)</p>
              </div>
            </label>
          </div>

          <div className="input-group" style={{ opacity: useSample ? 0.5 : 1 }}>
            <label>Upload CSV File</label>
            <div style={{ 
              border: '2px dashed var(--border)', 
              borderRadius: '12px', 
              padding: '2rem', 
              textAlign: 'center',
              backgroundColor: 'var(--bg-dark)',
              cursor: useSample ? 'not-allowed' : 'pointer',
              position: 'relative'
            }}>
              <input
                type="file"
                accept=".csv,text/csv"
                disabled={useSample}
                onChange={(e) => setFile(e.target.files?.[0] || null)}
                style={{ position: 'absolute', inset: 0, opacity: 0, cursor: 'inherit' }}
              />
              <FileSpreadsheet size={32} color={file ? 'var(--primary)' : 'var(--text-dark)'} style={{ marginBottom: '0.75rem' }} />
              <p style={{ fontSize: '0.9rem', color: file ? 'var(--text-main)' : 'var(--text-muted)' }}>
                {file ? file.name : 'Click or drop CSV file here'}
              </p>
              {file && <p style={{ fontSize: '0.7rem', color: 'var(--accent)', marginTop: '0.5rem' }}>Ready to process</p>}
            </div>
          </div>

          <div style={{ margin: '1.5rem 0' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={explain}
                onChange={(e) => setExplain(e.target.checked)}
                style={{ width: '18px', height: '18px' }}
              />
              <span style={{ fontSize: '0.9rem', color: 'var(--text-muted)' }}>Include SHAP Explainability (Slower)</span>
            </label>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '1rem' }}>
            <button className="btn btn-primary" onClick={runPredict} disabled={loading} style={{ height: '50px' }}>
              {loading ? <Loader2 className="animate-spin" size={20} /> : <Zap size={20} />}
              Run Engine
            </button>
            <button className="btn btn-outline" onClick={runStubUpload} disabled={loading || !file} style={{ height: '50px' }}>
              Upload to ML Stub
            </button>
          </div>
        </div>

        {/* ── Help / Instructions ───────────────────────────────────── */}
        <div className="card" style={{ backgroundColor: 'transparent', borderStyle: 'dashed' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1rem' }}>
            <ShieldCheck size={20} color="var(--accent)" />
            <h2 style={{ margin: 0, fontSize: '1.1rem' }}>Batch Instructions</h2>
          </div>
          <ul style={{ color: 'var(--text-muted)', fontSize: '0.9rem', paddingLeft: '1.25rem', lineHeight: '1.8' }}>
            <li>Ensure CSV file utilizes standard UTF-8 encoding.</li>
            <li>Required columns: Age, Gender, University, etc.</li>
            <li>Large batches (&gt;500 rows) may take several seconds.</li>
            <li>SHAP values provide row-level feature importance.</li>
          </ul>
          
          {error && (
            <div className="error-container" style={{ marginTop: '1.5rem' }}>
              <AlertCircle size={18} style={{ marginRight: '8px' }} />
              {error}
            </div>
          )}

          {stubResult && (
            <div className="card" style={{ marginTop: '1.5rem', backgroundColor: 'var(--bg-dark)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', color: 'var(--success)' }}>
                <CheckCircle2 size={18} />
                <span style={{ fontWeight: 600 }}>Stub Uploaded</span>
              </div>
              <pre style={{ fontSize: '0.7rem', marginTop: '0.5rem', color: 'var(--text-muted)' }}>
                {JSON.stringify(stubResult, null, 2)}
              </pre>
            </div>
          )}
        </div>
      </div>

      {/* ── Results Table ────────────────────────────────────────── */}
      {result && (
        <div className="card" style={{ marginTop: '2.5rem', padding: 0, overflow: 'hidden' }}>
          <div style={{ padding: '1.5rem', borderBottom: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <FileText size={20} color="var(--primary)" />
              <h2 style={{ margin: 0, fontSize: '1.25rem' }}>
                Engine Results ({result.total_rows} rows)
              </h2>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <span className="badge badge-success">{result.filename || 'Source Cache'}</span>
              <button className="btn btn-outline" onClick={exportResultsCsv}>
                <Download size={16} />
                Export CSV
              </button>
            </div>
          </div>

          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
              <thead>
                <tr style={{ backgroundColor: 'var(--bg-dark)', borderBottom: '1px solid var(--border)' }}>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontSize: '0.8rem' }}>#</th>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontSize: '0.8rem' }}>SCORES (A/S/D)</th>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontSize: '0.8rem' }}>LABELS</th>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontSize: '0.8rem' }}>RISK LEVEL</th>
                  <th style={{ padding: '1rem 1.5rem', color: 'var(--text-muted)', fontSize: '0.8rem' }}>PROBABILITY</th>
                </tr>
              </thead>
              <tbody>
                {result.predictions?.map((p) => (
                  <tr key={p.row_index} style={{ borderBottom: '1px solid var(--border)' }}>
                    <td style={{ padding: '1rem 1.5rem', fontWeight: 600 }}>{p.row_index}</td>
                    <td style={{ padding: '1rem 1.5rem' }}>
                      <code style={{ backgroundColor: 'var(--bg-dark)', padding: '4px 8px', borderRadius: '4px' }}>
                        {p.anxiety_score?.toFixed(1)} / {p.stress_score?.toFixed(1)} / {p.depression_score?.toFixed(1)}
                      </code>
                    </td>
                    <td style={{ padding: '1rem 1.5rem', fontSize: '0.85rem' }}>
                      <span style={{ color: 'var(--primary)' }}>{p.anxiety_label}</span> • 
                      <span style={{ color: 'var(--accent)' }}> {p.stress_label}</span> • 
                      <span style={{ color: 'var(--success)' }}> {p.depression_label}</span>
                    </td>
                    <td style={{ padding: '1rem 1.5rem' }}>
                      <span className={`badge ${p.risk_level?.toLowerCase()?.includes('high') ? 'badge-danger' : 'badge-warning'}`}>
                        {p.risk_level}
                      </span>
                    </td>
                    <td style={{ padding: '1rem 1.5rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                        <div style={{ flex: 1, height: '6px', backgroundColor: 'var(--bg-dark)', borderRadius: '3px', overflow: 'hidden' }}>
                          <div style={{ width: `${(p.risk_probability || 0) * 100}%`, height: '100%', backgroundColor: 'var(--primary)' }}></div>
                        </div>
                        <span style={{ fontSize: '0.8rem', fontWeight: 700 }}>
                          {((p.risk_probability || 0) * 100).toFixed(1)}%
                        </span>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <details style={{ padding: '1rem' }}>
            <summary style={{ fontSize: '0.8rem', color: 'var(--text-dark)', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '4px' }}>
              <ChevronDown size={14} /> View Raw JSON Meta
            </summary>
            <pre style={{ fontSize: '0.7rem', marginTop: '1rem', backgroundColor: 'var(--bg-dark)', padding: '1rem', borderRadius: '8px' }}>
              {JSON.stringify(result, null, 2)}
            </pre>
          </details>
        </div>
      )}
    </div>
  )
}
