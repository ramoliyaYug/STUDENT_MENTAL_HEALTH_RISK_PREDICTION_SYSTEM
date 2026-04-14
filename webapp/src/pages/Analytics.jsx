import { useState } from 'react'
import * as api from '../api/adminApi'
import { BarChart3, TrendingUp, Grid, Download, Loader2, AlertCircle, RefreshCw, Layers } from 'lucide-react'

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
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '2.5rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.25rem' }}>Advanced Analytics</h1>
          <p style={{ color: 'var(--text-muted)' }}>Visualize system trends and multidimensional risk Heatmaps</p>
        </div>
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button className="btn btn-outline" onClick={() => { loadWeekly(); loadHeatmap(); }} disabled={loading}>
            <RefreshCw size={18} className={loading ? 'animate-spin' : ''} />
            Refresh All
          </button>
          <button className="btn btn-primary" onClick={loadExport} disabled={loading}>
            <Download size={18} />
            Export Analytics
          </button>
        </div>
      </div>

      {error && (
        <div className="error-container">
          <AlertCircle size={18} style={{ marginRight: '8px' }} />
          {error}
        </div>
      )}

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '2rem', marginBottom: '2rem' }}>
        {/* ── Weekly Trend ────────────────────────────────────── */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <TrendingUp size={20} color="var(--primary)" />
              <h2 style={{ margin: 0, fontSize: '1.25rem' }}>Weekly Assessment Trend</h2>
            </div>
            {!weekly && <button className="btn btn-outline btn-sm" onClick={loadWeekly}>Load</button>}
          </div>

          {weekly ? (
            <div style={{ display: 'flex', alignItems: 'flex-end', gap: '12px', height: '200px', paddingBottom: '2rem' }}>
              {weekly.map((item, i) => {
                // Sum up all integer values in the object (low, medium, high, Moderate Risk, etc.)
                const count = Object.entries(item)
                  .filter(([k]) => k !== 'week_start')
                  .reduce((sum, [_, v]) => sum + (typeof v === 'number' ? v : 0), 0)
                
                const maxCount = Math.max(...weekly.map(w => 
                  Object.entries(w)
                    .filter(([key]) => key !== 'week_start')
                    .reduce((s, [_, val]) => s + (typeof val === 'number' ? val : 0), 0)
                ), 1)

                const date = new Date(item.week_start).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })

                return (
                  <div key={i} style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
                    <div style={{ position: 'relative', width: '100%', display: 'flex', justifyContent: 'center' }}>
                      <div style={{ 
                        width: '80%', 
                        height: `${(count / maxCount) * 150}px`, 
                        background: 'linear-gradient(to top, var(--primary), var(--accent))',
                        borderRadius: '6px 6px 0 0',
                        boxShadow: '0 4px 12px var(--primary-glow)',
                        minHeight: count > 0 ? '5px' : '0'
                      }}></div>
                      <span style={{ position: 'absolute', top: '-25px', fontSize: '0.75rem', fontWeight: 700 }}>{count}</span>
                    </div>
                    <span style={{ fontSize: '0.65rem', color: 'var(--text-muted)', textAlign: 'center' }}>{date}</span>
                  </div>
                )
              })}
            </div>
          ) : (
            <div style={{ height: '200px', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1px dashed var(--border)', borderRadius: '12px', color: 'var(--text-dark)' }}>
              No weekly trend data loaded
            </div>
          )}
        </div>

        {/* ── Heatmap Stats ────────────────────────────────────── */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
              <Grid size={20} color="var(--accent)" />
              <h2 style={{ margin: 0, fontSize: '1.25rem' }}>Risk Heatmap Profile</h2>
            </div>
            {!heatmap && <button className="btn btn-outline btn-sm" onClick={loadHeatmap}>Load</button>}
          </div>

          {heatmap ? (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(130px, 1fr))', gap: '1rem' }}>
              {heatmap.map((item, i) => (
                <div key={i} style={{ padding: '1rem', backgroundColor: 'var(--bg-dark)', borderRadius: '12px', border: '1px solid var(--border)' }}>
                  <p style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginBottom: '0.5rem', textTransform: 'uppercase' }}>
                    {item.risk_level}
                  </p>
                  <p style={{ fontSize: '1.5rem', fontWeight: 800, margin: 0 }}>{item.count}</p>
                  <p style={{ fontSize: '0.6rem', color: 'var(--text-dark)', marginTop: '4px' }}>
                    {new Date(item.day).toLocaleDateString()}
                  </p>
                </div>
              ))}
            </div>
          ) : (
            <div style={{ height: '200px', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1px dashed var(--border)', borderRadius: '12px', color: 'var(--text-dark)' }}>
              No heatmap data loaded
            </div>
          )}
        </div>
      </div>

      {/* ── Export Section ───────────────────────────────────── */}
      <div className="card">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
          <Layers size={20} color="var(--primary)" />
          <h2 style={{ margin: 0, fontSize: '1.25rem' }}>Raw Export Pipeline</h2>
        </div>
        <div style={{ backgroundColor: 'var(--bg-dark)', borderRadius: '12px', padding: '1rem', position: 'relative' }}>
          <pre style={{ 
            fontSize: '0.8rem', 
            maxHeight: '300px', 
            overflow: 'auto', 
            color: exp ? 'var(--text-main)' : 'var(--text-dark)',
            lineHeight: '1.6'
          }}>
            {exp ? JSON.stringify(exp, null, 2) : '// Export details will appear here after loading.'}
          </pre>
        </div>
      </div>

      <style>{`
        .btn-sm { padding: 4px 12px; font-size: 0.8rem; }
      `}</style>
    </div>
  )
}
