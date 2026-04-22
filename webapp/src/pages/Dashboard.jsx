import { useEffect, useState } from 'react'
import { BACKEND_URL } from '../config'
import * as api from '../api/adminApi'
import { Activity, Users, AlertTriangle, CheckCircle2, Server, Globe } from 'lucide-react'

export default function Dashboard() {
  const [health, setHealth] = useState(null)
  const [dash, setDash] = useState(null)
  const [statsData, setStatsData] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const [h, d, s] = await Promise.all([
          api.getHealth(),
          api.getDashboard(),
          api.getAdminStats(),
        ])
        if (!cancelled) {
          setHealth(h)
          setDash(d)
          setStatsData(s)
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

  const stats = [
    { 
      label: 'Total Students', 
      value: statsData?.total_students ?? dash?.total_students ?? 0, 
      icon: <Users size={24} color="var(--primary)" />,
      subtitle: 'Registered in system'
    },
    { 
      label: 'Total Assessments', 
      value: statsData?.total_assessments ?? dash?.total_assessments ?? 0, 
      icon: <Activity size={24} color="var(--accent)" />,
      subtitle: 'Total records collected'
    },
    { 
      label: 'System Status', 
      value: health?.status === 'ok' ? 'Healthy' : 'Issues', 
      icon: health?.status === 'ok' ? <CheckCircle2 size={24} color="#00e676" /> : <AlertTriangle size={24} color="#ff8a80" />,
      subtitle: health?.db === 'connected' ? 'DB Connected' : 'DB Link Error'
    }
  ]

  return (
    <div>
      <div style={{ marginBottom: '2.5rem' }}>
        <h1 style={{ fontSize: '2rem', marginBottom: '0.25rem' }}>Admin Dashboard</h1>
        <p style={{ color: 'var(--text-muted)' }}>Real-time overview of the student mental health platform</p>
      </div>

      {error && (
        <div className="error-container">
          <AlertTriangle size={18} style={{ marginRight: '8px' }} />
          {error}
        </div>
      )}

      {/* ── Stats Grid ────────────────────────────────────────── */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))',
        gap: '1.5rem',
        marginBottom: '2.5rem'
      }}>
        {stats.map((stat, i) => (
          <div key={i} className="card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '1.5rem' }}>
              <div style={{ 
                backgroundColor: 'var(--bg-dark)', 
                padding: '12px', 
                borderRadius: '12px',
                border: '1px solid var(--border)' 
              }}>
                {stat.icon}
              </div>
              <span className="badge badge-success">Online</span>
            </div>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginBottom: '0.25rem' }}>{stat.label}</p>
            <h3 style={{ fontSize: '2rem', margin: 0 }}>{stat.value}</h3>
            <p style={{ fontSize: '0.75rem', color: 'var(--text-dark)', marginTop: '0.5rem' }}>{stat.subtitle}</p>
          </div>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '1.5rem' }}>
        {/* ── API Info ──────────────────────────────────────────── */}
        <div className="card">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
            <Server size={20} color="var(--primary)" />
            <h2 style={{ margin: 0, fontSize: '1.25rem' }}>Backend Environment</h2>
          </div>
          <div style={{ backgroundColor: 'var(--bg-dark)', padding: '1.5rem', borderRadius: '12px', border: '1px solid var(--border)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>Base URL</span>
              <code style={{ color: 'var(--accent)' }}>{BACKEND_URL}</code>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem' }}>
              <span style={{ color: 'var(--text-muted)' }}>API Status</span>
              <span style={{ color: '#00e676' }}>Active</span>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
              <span style={{ color: 'var(--text-muted)' }}>Version</span>
              <code>v1.0.4-stable</code>
            </div>
          </div>
        </div>

        {/* ── Deployment Card ───────────────────────────────────── */}
        <div className="card" style={{ background: 'linear-gradient(45deg, var(--bg-card), #1e1e40)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
            <Globe size={20} color="var(--accent)" />
            <h2 style={{ margin: 0, fontSize: '1.25rem' }}>Network</h2>
          </div>
          <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', lineHeight: '1.6' }}>
            The management console is currently connected to the primary prediction engine.
          </p>
          <div style={{ marginTop: '1.5rem' }}>
             <button className="btn btn-outline" style={{ width: '100%' }}>Check Detailed Logs</button>
          </div>
        </div>
      </div>
    </div>
  )
}
