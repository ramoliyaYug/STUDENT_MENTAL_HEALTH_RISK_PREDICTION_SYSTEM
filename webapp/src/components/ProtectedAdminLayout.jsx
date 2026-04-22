import { useEffect, useState } from 'react'
import { Navigate, Outlet, NavLink, useNavigate } from 'react-router-dom'
import { fetchMe, getToken, logout } from '../api/client'
import { 
  LayoutDashboard, 
  Users, 
  FileSpreadsheet, 
  BarChart3, 
  Info, 
  LogOut, 
  User,
  Brain,
  ChevronRight,
  Loader2
} from 'lucide-react'

export default function ProtectedAdminLayout() {
  const navigate = useNavigate()
  const [state, setState] = useState({ loading: true, ok: false, user: null })

  useEffect(() => {
    const token = getToken()
    if (!token) {
      setState({ loading: false, ok: false, user: null })
      return
    }
    fetchMe()
      .then((me) => {
        setState({ loading: false, ok: me.role === 'admin', user: me })
      })
      .catch(() => {
        setState({ loading: false, ok: false, user: null })
      })
  }, [])

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  if (state.loading) {
    return (
      <div className="auth-container">
        <Loader2 className="animate-spin" size={48} color="var(--primary)" />
      </div>
    )
  }

  if (!getToken() || !state.ok) {
    return <Navigate to="/login" replace />
  }

  const navItems = [
    { to: '/', icon: <LayoutDashboard size={20} />, label: 'Dashboard' },
    { to: '/students', icon: <Users size={20} />, label: 'Students' },
    { to: '/batch', icon: <FileSpreadsheet size={20} />, label: 'Batch CSV' },
    { to: '/analytics', icon: <BarChart3 size={20} />, label: 'Analytics' },
    { to: '/ml', icon: <Info size={20} />, label: 'ML Model' },
  ]

  return (
    <div className="app-container">
      {/* ── Sidebar ────────────────────────────────────────────── */}
      <aside style={{
        width: '280px',
        backgroundColor: 'var(--bg-card)',
        borderRight: '1px solid var(--border)',
        display: 'flex',
        flexDirection: 'column',
        position: 'fixed',
        top: 0,
        bottom: 0,
        left: 0,
        zIndex: 100,
        boxShadow: 'var(--shadow-md)'
      }}>
        <div style={{ padding: '2rem 1.5rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <div style={{
            background: 'linear-gradient(135deg, var(--primary), var(--accent))',
            padding: '8px',
            borderRadius: '12px'
          }}>
            <Brain size={24} color="white" />
          </div>
          <span style={{ fontWeight: 800, fontSize: '1.25rem', letterSpacing: '-0.5px' }}>
            Mind<span className="text-gradient">Guard</span>
          </span>
        </div>

        <nav style={{ flex: 1, padding: '1rem' }}>
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
            >
              {item.icon}
              <span>{item.label}</span>
              <ChevronRight className="arrow" size={14} />
            </NavLink>
          ))}
        </nav>

        <div style={{ padding: '1.5rem', borderTop: '1px solid var(--border)' }}>
          <div style={{ 
            display: 'flex', 
            alignItems: 'center', 
            gap: '0.75rem', 
            marginBottom: '1rem',
            padding: '0.75rem',
            backgroundColor: 'var(--bg-dark)',
            border: '1px solid var(--border)',
            borderRadius: '12px'
          }}>
            <div style={{
              width: '36px',
              height: '36px',
              borderRadius: '50%',
              backgroundColor: 'var(--border)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center'
            }}>
              <User size={20} color="var(--text-muted)" />
            </div>
            <div style={{ overflow: 'hidden' }}>
              <p style={{ fontWeight: 700, fontSize: '0.85rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                {state.user?.name || 'Admin'}
              </p>
              <p style={{ fontSize: '0.65rem', color: 'var(--text-muted)' }}>Administrator</p>
            </div>
          </div>
          <button 
            type="button" 
            onClick={handleLogout}
            className="btn btn-outline"
            style={{ width: '100%', justifyContent: 'flex-start', border: 'none', color: 'var(--danger)' }}
          >
            <LogOut size={18} />
            <span>Logout</span>
          </button>
        </div>
      </aside>

      {/* ── Main Content ────────────────────────────────────────── */}
      <main style={{ marginLeft: '280px', flex: 1, minHeight: '100vh' }}>
        <div className="page-container">
          <Outlet />
        </div>
      </main>

      <style>{`
        .nav-link {
          display: flex;
          align-items: center;
          gap: 0.75rem;
          padding: 0.85rem 1rem;
          color: var(--text-muted);
          border-radius: 12px;
          margin-bottom: 0.5rem;
          font-weight: 700;
          font-family: "Comic Relief", system-ui, sans-serif;
          transition: all 0.2s ease;
        }
        .nav-link:hover {
          color: var(--primary);
          background-color: var(--primary-glow);
        }
        .nav-link.active {
          color: #ffffff;
          background-color: var(--primary);
          box-shadow: 0 4px 12px var(--primary-glow);
        }
        .nav-link .arrow {
          margin-left: auto;
          opacity: 0;
          transition: opacity 0.2s ease;
        }
        .nav-link:hover .arrow, .nav-link.active .arrow {
          opacity: 1;
        }
      `}</style>
    </div>
  )
}
