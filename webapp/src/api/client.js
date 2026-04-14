import axios from 'axios'
import { API_BASE } from '../config'

const TOKEN_KEY = 'smh_admin_token'

export const http = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  return config
})

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export async function login(email, password) {
  const params = new URLSearchParams()
  params.set('username', email)
  params.set('password', password)
  const { data } = await http.post('/auth/login', params, {
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
  })
  if (data.access_token) setToken(data.access_token)
  return data
}

export async function register(name, email, password, role = 'admin') {
  const { data } = await http.post('/auth/register', {
    name,
    email,
    password,
    role,
  })
  if (data.access_token) setToken(data.access_token)
  return data
}

export async function fetchMe() {
  const { data } = await http.get('/auth/me')
  return data
}

export function logout() {
  setToken(null)
}
