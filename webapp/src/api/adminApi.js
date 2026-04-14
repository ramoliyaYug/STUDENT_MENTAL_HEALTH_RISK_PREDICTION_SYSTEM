import { http } from './client'

export async function getHealth() {
  const { data } = await http.get('/health')
  return data
}

export async function getDashboard() {
  const { data } = await http.get('/admin/dashboard')
  return data
}

export async function listStudents(params = {}) {
  const { data } = await http.get('/admin/students', { params })
  return data
}

export async function getStudentAnalytics(studentId) {
  const { data } = await http.get(`/admin/student/${studentId}/analytics`)
  return data
}

export async function getBatchJob(batchId) {
  const { data } = await http.get(`/admin/batch/${batchId}`)
  return data
}

/**
 * @param {File|null} file
 * @param {{ use_project_rawtest?: boolean, include_explainability?: boolean }} options
 */
export async function batchPredict(file, options = {}) {
  const form = new FormData()
  form.append('use_project_rawtest', String(!!options.use_project_rawtest))
  form.append('include_explainability', String(!!options.include_explainability))
  if (file) {
    form.append('file', file)
  }
  const { data } = await http.post('/admin/batch/predict', form)
  return data
}

export async function uploadBatchStub(file) {
  const form = new FormData()
  form.append('file', file)
  const { data } = await http.post('/ml/batch/upload', form)
  return data
}

export async function getWeeklyTrend() {
  const { data } = await http.get('/analytics/trend/weekly')
  return data
}

export async function getHeatmap() {
  const { data } = await http.get('/analytics/heatmap')
  return data
}

export async function getAnalyticsExport() {
  const { data } = await http.get('/analytics/export')
  return data
}

export async function getModelInfo() {
  const { data } = await http.get('/ml/modelInfo')
  return data
}

export async function getMlIndicators() {
  const { data } = await http.get('/ml/indicators')
  return data
}
