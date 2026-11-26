import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api.js'

function RecommendationsPage() {
  const [formData, setFormData] = useState({
    dateOfBirth: '',
    insuranceGoals: '',
    preferredTerm: '',
    preferredPolicyType: ''
  })
  const [recommendations, setRecommendations] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [submitted, setSubmitted] = useState(false)

  function handleChange(e) {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)
    setError('')
    setRecommendations([])
    setSubmitted(false)

    const payload = {
      ...formData,
      preferredTerm: formData.preferredTerm ? parseInt(formData.preferredTerm) : null
    }

    api.post('/recommendations/generate', payload)
      .then(({ data }) => {
        const recs = data.recommendations || data;
        setRecommendations(recs);
        setSubmitted(true)
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Recommendations are unavailable right now. Please try again later.')
      })
      .finally(() => setLoading(false))
  }

  return (
    <div className="dashboard-shell" style={{ backgroundColor: 'var(--peach-light)', minHeight: '100vh' }}>
      <nav className="navbar navbar-expand-lg bg-white border-bottom shadow-sm">
        <div className="container">
          <Link className="navbar-brand fw-bold text-primary" to="/dashboard">HealthDekho</Link>
          <Link to="/dashboard" className="btn btn-outline-secondary btn-sm rounded-3">Back to Dashboard</Link>
        </div>
      </nav>

      <main className="container py-5">
        <h1 className="h3 fw-bold mb-4 text-dark">AI Policy Recommendations</h1>

        {error && <div className="alert alert-danger rounded-3 border-0 shadow-sm">{error}</div>}

        <div className="row g-4">
          {/* Form Side */}
          <div className="col-12 col-lg-4">
            <div className="card border-0 shadow-sm rounded-4 p-4">
              <form onSubmit={handleSubmit}>
                <div className="mb-3">
                  <label className="form-label fw-semibold text-secondary small">Date of Birth</label>
                  <input type="date" className="form-control bg-light border-0" name="dateOfBirth" value={formData.dateOfBirth} onChange={handleChange} required />
                </div>
                <div className="mb-3">
                  <label className="form-label fw-semibold text-secondary small">Insurance Goals</label>
                  <textarea className="form-control bg-light border-0" name="insuranceGoals" rows="2" value={formData.insuranceGoals} onChange={handleChange} placeholder="e.g. Family protection" required />
                </div>
                <div className="mb-3">
                  <label className="form-label fw-semibold text-secondary small">Preferred Term (Months)</label>
                  <input type="number" min="1" className="form-control bg-light border-0" name="preferredTerm" value={formData.preferredTerm} onChange={handleChange} placeholder="e.g. 240" required />
                </div>
                <div className="mb-4">
                  <label className="form-label fw-semibold text-secondary small">Preferred Policy Type</label>
                  <select className="form-select bg-light border-0" name="preferredPolicyType" value={formData.preferredPolicyType} onChange={handleChange}>
                    <option value="">Any</option>
                    <option value="TERM">TERM</option>
                    <option value="HEALTH">HEALTH</option>
                    <option value="SAVINGS">SAVINGS</option>
                  </select>
                </div>
                <div className="d-grid mt-2">
                  <button type="submit" className="btn btn-primary btn-lg shadow-sm" disabled={loading}>
                    {loading ? 'Generating with AI...' : 'Generate Recommendations'}
                  </button>
                </div>
              </form>
            </div>
          </div>

          {/* Results Side */}
          <div className="col-12 col-lg-8">
            <div className="card border-0 shadow-sm rounded-4 h-100 p-4">
              <h5 className="mb-4 fw-bold text-dark">Results</h5>
              
              {loading && (
                <div className="d-flex flex-column align-items-center justify-content-center py-5">
                  <div className="spinner-border mb-3" role="status" style={{ width: '3rem', height: '3rem', color: 'var(--peach-primary)' }}>
                    <span className="visually-hidden">Loading...</span>
                  </div>
                  <p className="text-muted fw-bold">AI is analyzing your profile...</p>
                </div>
              )}
              
              {!loading && recommendations.length === 0 && !error && (
                <div className="text-muted p-4 text-center bg-light rounded-3">
                  {submitted ? 'No eligible recommendations are available right now.' : 'Fill out the form to get personalized AI recommendations.'}
                </div>
              )}

              {!loading && recommendations.length > 0 && (
                <div className="table-responsive rounded-4 overflow-hidden">
                  <table className="table table-hover align-middle mb-0">
                    <thead className="table-light">
                      <tr>
                        <th className="py-3 text-uppercase text-muted small fw-bold">Product</th>
                        <th className="py-3 text-uppercase text-muted small fw-bold">Type</th>
                        <th className="py-3 text-uppercase text-muted small fw-bold">Suitability</th>
                        <th className="py-3 text-uppercase text-muted small fw-bold">Reason</th>
                        <th className="py-3 text-uppercase text-muted small fw-bold">Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {recommendations.map((rec, index) => (
                        <tr key={index}>
                          <td>
                            <div className="fw-bold text-dark">{rec.policyName}</div>
                            <div className="small text-muted">{rec.productCode}</div>
                          </td>
                          <td>
                            <span className="badge bg-primary bg-opacity-10 text-primary px-3 py-2 rounded-pill">
                              {rec.policyType}
                            </span>
                          </td>
                          <td>
                            <span className={`badge rounded-pill px-3 py-2 ${
                              rec.suitability === 'HIGH' ? 'bg-success bg-opacity-25 text-success' : 
                              rec.suitability === 'MEDIUM' ? 'bg-warning bg-opacity-25 text-warning' : 
                              'bg-danger bg-opacity-25 text-danger'
                            }`}>
                              {rec.suitability}
                            </span>
                          </td>
                          <td className="small text-muted" style={{ maxWidth: '300px' }}>{rec.reason}</td>
                          <td>
                            <Link to={`/policies/register/${rec.productId}`} className="btn btn-primary btn-sm rounded-3 px-3">
                              Register
                            </Link>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}

export default RecommendationsPage