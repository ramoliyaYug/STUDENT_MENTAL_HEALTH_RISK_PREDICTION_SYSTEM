import { useState, useEffect } from 'react'
import * as api from '../api/adminApi'
import { Filter, Search, User, Calendar, Brain, Download, Loader2, AlertCircle } from 'lucide-react'

export default function Students() {
  const [risk, setRisk] = useState('')
  const [month, setMonth] = useState('')
  const [list, setList] = useState(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  // Load students on mount
  useEffect(() => {
    loadStudents()
  }, [])

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

  const getRiskColor = (riskText) => {
    if (!riskText) return 'badge-success'
    const r = riskText.toLowerCase()
    if (r.includes('high')) return 'badge-danger'
    if (r.includes('moderate')) return 'badge-warning'
    return 'badge-success'
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginBottom: '2.5rem' }}>
        <div>
          <h1 style={{ fontSize: '2rem', marginBottom: '0.25rem' }}>Student Management</h1>
          <p style={{ color: 'var(--text-muted)' }}>Monitor and filter student records and risk metadata</p>
        </div>
        <button className="btn btn-primary" onClick={loadStudents} disabled={loading}>
          {loading ? <Loader2 className="animate-spin" size={18} /> : <Download size={18} />}
          Export Data
        </button>
      </div>

      <div className="card" style={{ marginBottom: '2rem' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', marginBottom: '1.5rem' }}>
          <Filter size={20} color="var(--primary)" />
          <h2 style={{ margin: 0, fontSize: '1.1rem' }}>Filter Records</h2>
        </div>
        
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr auto', gap: '1rem', alignItems: 'flex-end' }}>
          <div className="input-group" style={{ marginBottom: 0 }}>
            <label>Risk Level</label>
            <div style={{ position: 'relative' }}>
              <Brain size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-dark)' }} />
              <select 
                value={risk} 
                onChange={(e) => setRisk(e.target.value)}
                style={{ paddingLeft: '40px' }}
              >
                <option value="">All Risks</option>
                <option value="High Risk">High Risk</option>
                <option value="Moderate Risk">Moderate Risk</option>
                <option value="Low Risk">Low Risk</option>
              </select>
            </div>
          </div>
          
          <div className="input-group" style={{ marginBottom: 0 }}>
            <label>Month (Numeric)</label>
            <div style={{ position: 'relative' }}>
              <Calendar size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-dark)' }} />
              <input
                type="number"
                value={month}
                onChange={(e) => setMonth(e.target.value)}
                placeholder="e.g. 4 for April"
                style={{ paddingLeft: '40px' }}
              />
            </div>
          </div>

          <button 
            type="button" 
            onClick={loadStudents} 
            disabled={loading}
            className="btn btn-primary"
            style={{ height: '45px', padding: '0 2rem' }}
          >
            {loading ? <Loader2 className="animate-spin" size={20} /> : <Search size={20} />}
          </button>
        </div>
      </div>

      {error && (
        <div className="error-container">
          <AlertCircle size={18} style={{ marginRight: '8px' }} />
          {error}
        </div>
      )}

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ backgroundColor: 'var(--bg-dark)', borderBottom: '1px solid var(--border)' }}>
                <th style={{ padding: '1.25rem 1.5rem', color: 'var(--text-muted)', fontWeight: 600, fontSize: '0.85rem' }}>ID</th>
                <th style={{ padding: '1.25rem 1.5rem', color: 'var(--text-muted)', fontWeight: 600, fontSize: '0.85rem' }}>STUDENT</th>
                <th style={{ padding: '1.25rem 1.5rem', color: 'var(--text-muted)', fontWeight: 600, fontSize: '0.85rem' }}>RISK LEVEL</th>
                <th style={{ padding: '1.25rem 1.5rem', color: 'var(--text-muted)', fontWeight: 600, fontSize: '0.85rem' }}>CREATED AT</th>
              </tr>
            </thead>
            <tbody>
              {list && list.length > 0 ? (
                list.map((item) => (
                  <tr key={item.id} style={{ borderBottom: '1px solid var(--border)', transition: 'background 0.2s' }} className="table-row-hover">
                    <td style={{ padding: '1.25rem 1.5rem', fontWeight: 600 }}>#{item.id}</td>
                    <td style={{ padding: '1.25rem 1.5rem' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                        <div style={{ padding: '8px', backgroundColor: 'var(--bg-dark)', borderRadius: '8px' }}>
                          <User size={16} color="var(--primary)" />
                        </div>
                        <span style={{ fontWeight: 500 }}>Student ID: {item.student_id}</span>
                      </div>
                    </td>
                    <td style={{ padding: '1.25rem 1.5rem' }}>
                      <span className={`badge ${getRiskColor(item.risk_level)}`}>
                        {item.risk_level}
                      </span>
                    </td>
                    <td style={{ padding: '1.25rem 1.5rem', color: 'var(--text-muted)', fontSize: '0.9rem' }}>
                      {new Date(item.created_at || Date.now()).toLocaleDateString('en-US', {
                        month: 'short',
                        day: 'numeric',
                        year: 'numeric'
                      })}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="4" style={{ padding: '4rem', textAlign: 'center', color: 'var(--text-dark)' }}>
                    {loading ? (
                      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '1rem' }}>
                        <Loader2 className="animate-spin" size={32} color="var(--primary)" />
                        <p>Fetching records...</p>
                      </div>
                    ) : (
                      'No student records found matching the filters.'
                    )}
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <style>{`
        .table-row-hover:hover {
          background-color: var(--bg-dark);
        }
      `}</style>
    </div>
  )
}
