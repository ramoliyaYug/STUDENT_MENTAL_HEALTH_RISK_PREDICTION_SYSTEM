import { useEffect, useState } from 'react'
import { Navigate, Outlet, NavLink, useNavigate } from 'react-router-dom'
import { fetchMe, getToken, logout } from '../api/client'

export default function ProtectedAdminLayout() {
  const navigate = useNavigate()
  const [state, setState] = useState({ loading: true, ok: false })

  useEffect(() => {
    const token = getToken()
    if (!token) {
      setState({ loading: false, ok: false })
      return
    }
    fetchMe()
      .then((me) => {
        setState({ loading: false, ok: me.role === 'admin' })
      })
      .catch(() => {
        setState({ loading: false, ok: false })
      })
  }, [])

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  if (state.loading) {
    return (
      <div className="page">
        <p>Checking session…</p>
      </div>
    )
  }

  if (!getToken() || !state.ok) {
    return <Navigate to="/login" replace />
  }

  return (
    <div className="admin">
      <header className="admin-header">
        <strong>Admin</strong>
        <nav className="admin-nav">
          <NavLink to="/" end>
            Dashboard
          </NavLink>
          <NavLink to="/students">Students</NavLink>
          <NavLink to="/batch">Batch CSV</NavLink>
          <NavLink to="/analytics">Analytics</NavLink>
          <NavLink to="/ml">ML info</NavLink>
        </nav>
        <button type="button" onClick={handleLogout}>
          Log out
        </button>
      </header>
      <main className="admin-main">
        <Outlet />
      </main>
    </div>
  )
}
