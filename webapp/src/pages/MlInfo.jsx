import { useEffect, useState } from 'react'
import * as api from '../api/adminApi'
import { Info, Cpu, Database, CheckCircle2, ChevronRight, Loader2, AlertCircle } from 'lucide-react'

export default function MlInfo() {
  const [info, setInfo] = useState(null)
  const [indicators, setIndicators] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

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
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div>
      <div style={{ marginBottom: '2.5rem' }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '0.25rem' }}>Machine Learning Specs</h1>
        <p style={{ color: 'var(--text-muted)' }}>Underlying model architecture and feature engineering definitions</p>
      </div>

      {error && (
        <div className="error-container">
          <AlertCircle size={18} style={{ marginRight: '8px' }} />
          {error}
        </div>
      )}

      {loading ? (
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '300px', gap: '1rem' }}>
          <Loader2 className="animate-spin" size={40} color="var(--primary)" />
          <p style={{ color: 'var(--text-dark)' }}>Loading engine specs...</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem' }}>
          {/* ── Model Versioning ─────────────────────────────────── */}
          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
              <Cpu size={20} color="var(--primary)" />
              <h2 style={{ margin: 0, fontSize: '1.25rem' }}>Core Engine Details</h2>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              <div style={{ padding: '1rem', backgroundColor: 'var(--bg-dark)', borderRadius: '12px', border: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-muted)' }}>Deployment Name</span>
                <span style={{ fontWeight: 600 }}>{info?.name || 'Mental Risk Predictor'}</span>
              </div>
              <div style={{ padding: '1rem', backgroundColor: 'var(--bg-dark)', borderRadius: '12px', border: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-muted)' }}>Status</span>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#00e676' }}>
                  <CheckCircle2 size={14} />
                  <span>Production Ready</span>
                </div>
              </div>
              <div style={{ padding: '1rem', backgroundColor: 'var(--bg-dark)', borderRadius: '12px', border: '1px solid var(--border)', display: 'flex', justifyContent: 'space-between' }}>
                <span style={{ color: 'var(--text-muted)' }}>Version</span>
                <code style={{ color: 'var(--accent)' }}>v3.1.2-alpha</code>
              </div>
            </div>

            <div style={{ marginTop: '2rem', padding: '1rem', backgroundColor: 'rgba(108, 99, 255, 0.05)', borderRadius: '12px', border: '1px dashed var(--primary)' }}>
              <div style={{ display: 'flex', gap: '0.75rem' }}>
                <Info size={16} color="var(--primary)" />
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', lineHeight: '1.5' }}>
                   The model utilizes an ensemble of Gradient Boosting Trees for high-precision risk classification.
                </p>
              </div>
            </div>
          </div>

          {/* ── Feature Space ───────────────────────────────────── */}
          <div className="card">
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
              <Database size={20} color="var(--accent)" />
              <h2 style={{ margin: 0, fontSize: '1.25rem' }}>Feature Columns</h2>
            </div>
            
            <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginBottom: '1.5rem' }}>
              Total indicators processed by the neural pipeline: <strong>{indicators?.cleaned_feature_columns?.length || 0}</strong>
            </p>

            <div style={{ 
              display: 'flex', 
              flexWrap: 'wrap', 
              gap: '8px', 
              maxHeight: '300px', 
              overflowY: 'auto', 
              paddingRight: '8px'
            }}>
              {indicators?.cleaned_feature_columns?.map((ind, i) => (
                <div key={i} style={{ 
                  padding: '6px 12px', 
                  backgroundColor: 'var(--bg-dark)', 
                  border: '1px solid var(--border)', 
                  borderRadius: '8px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  fontSize: '0.75rem'
                }}>
                  <ChevronRight size={10} color="var(--primary)" />
                  {ind}
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
