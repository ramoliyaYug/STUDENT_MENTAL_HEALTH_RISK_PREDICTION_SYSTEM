import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import ProtectedAdminLayout from './components/ProtectedAdminLayout'
import Analytics from './pages/Analytics'
import BatchPredict from './pages/BatchPredict'
import Dashboard from './pages/Dashboard'
import Login from './pages/Login'
import MlInfo from './pages/MlInfo'
import Register from './pages/Register'
import Students from './pages/Students'
import './App.css'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/" element={<ProtectedAdminLayout />}>
          <Route index element={<Dashboard />} />
          <Route path="students" element={<Students />} />
          <Route path="batch" element={<BatchPredict />} />
          <Route path="analytics" element={<Analytics />} />
          <Route path="ml" element={<MlInfo />} />
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  )
}
