import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api, { TOKEN_STORAGE_KEY } from '../api.js'

function DashboardPage() {
  const navigate = useNavigate()
  const [dashboard, setDashboard] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true

    api.get('/dashboard')
      .then(({ data }) => {
        if (active) setDashboard(data)
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.response?.data?.message || 'Dashboard data could not be loaded.')
        }
      })

    return () => {
      active = false
    }
  }, [])

  function signOut() {
    localStorage.removeItem(TOKEN_STORAGE_KEY)
    navigate('/login', { replace: true })
  }

  return (
    <div className="d-flex min-vh-100 bg-light">
      
      {/* LEFT SIDEBAR */}
      <div className="d-flex flex-column flex-shrink-0 p-4 peach-sidebar" style={{ width: '280px' }}>
        <Link to="/dashboard" className="d-flex align-items-center mb-4 text-decoration-none">
          <span className="fs-4 fw-bold text-primary">HealthDekho</span>
        </Link>
        
        <ul className="nav nav-pills flex-column mb-auto">
          <li className="nav-item mb-1">
            <Link to="/dashboard" className="nav-link active">Dashboard</Link>
          </li>
          <li className="nav-item mb-1">
            <Link to="/available-policies" className="nav-link">Explore Products</Link>
          </li>
          <li className="nav-item mb-1">
            <Link to="/recommendations" className="nav-link">AI Recommendations</Link>
          </li>
          <li className="nav-item mb-1">
            <Link to="/payments" className="nav-link">Premium Payments</Link>
          </li>
          <li className="nav-item mb-1">
            <Link to="/policies/search" className="nav-link">Search Policies</Link>
          </li>
        </ul>
        
        <hr className="text-muted" />
        <button className="btn btn-outline-danger w-100 rounded-3" onClick={signOut} type="button">
          Sign out
        </button>
      </div>

      {/* MAIN CONTENT AREA */}
      <div className="flex-grow-1 p-4 p-md-5" style={{ height: '100vh', overflowY: 'auto' }}>
        
        {error && <div className="alert alert-danger rounded-3 border-0">{error}</div>}
        
        {!dashboard && !error && (
          <div className="d-flex justify-content-center align-items-center h-100">
            <div className="spinner-border text-primary" role="status">
              <span className="visually-hidden">Loading...</span>
            </div>
          </div>
        )}

        {dashboard && (
          <>
            {/* HEADER BANNER */}
            <div className="rounded-4 mb-5 p-4 text-white shadow-sm" style={{ background: 'linear-gradient(135deg, #FF8C69 0%, #FFB49E 100%)' }}>
              <p className="mb-1 fw-bold text-uppercase" style={{ fontSize: '0.8rem', letterSpacing: '1px', opacity: 0.9 }}>Your Overview</p>
              <h2 className="fw-bold mb-2">Good to see you, {dashboard.customer.fullName}😊</h2>
              <p className="mb-0 fst-italic" style={{ opacity: 0.9 }}>"Protecting what matters most, today and tomorrow."</p>
            </div>

            {/* STATS CARDS */}
            <section aria-label="Policy statistics" className="row g-4 mb-5">
              <StatisticCard label="Total policies" value={dashboard.statistics.totalPolicies} />
              <StatisticCard label="Active policies" value={dashboard.statistics.activePolicies} />
              <StatisticCard label="Pending installments" value={dashboard.statistics.pendingInstallments} />
              <StatisticCard label="Recommendations" value={dashboard.statistics.recommendationsGenerated} />
            </section>

            {/* POLICIES TABLE */}
            <section className="mb-5">
              <div className="d-flex justify-content-between align-items-center mb-3">
                <h4 className="fw-bold mb-0">My Registered Policies</h4>
                <span className="badge rounded-pill bg-primary bg-opacity-25 text-primary px-3 py-2">
                  {dashboard.registeredPolicies.length} Total
                </span>
              </div>
              
              <div className="card border-0 shadow-sm rounded-4 overflow-hidden">
                <div className="card-body p-0">
                  {dashboard.registeredPolicies.length === 0 ? (
                    <div className="p-5 text-center text-muted">
                      <p className="mb-0">No registered insurance policies found.</p>
                    </div>
                  ) : (
                    <div className="table-responsive">
                      <table className="table table-hover align-middle mb-0">
                        <thead className="table-light">
                          <tr>
                            <th className="px-4 py-3 text-uppercase text-muted small fw-bold">Policy Number</th>
                            <th className="py-3 text-uppercase text-muted small fw-bold">Policy Name</th>
                            <th className="py-3 text-uppercase text-muted small fw-bold">Status</th>
                            <th className="py-3 text-uppercase text-muted small fw-bold">Premium</th>
                          </tr>
                        </thead>
                        <tbody>
                          {dashboard.registeredPolicies.map((policy) => (
                            <tr key={policy.policyId}>
                              <td className="px-4 fw-medium text-dark">{policy.policyNumber}</td>
                              <td>{policy.policyName}</td>
                              <td>
                                <span className={`badge rounded-pill px-3 py-2 ${policy.status === 'ACTIVE' ? 'bg-success bg-opacity-25 text-success' : 'bg-secondary bg-opacity-25 text-secondary'}`}>
                                  {policy.status}
                                </span>
                              </td>
                              <td className="fw-semibold">₹{Number(policy.premiumAmount).toLocaleString('en-IN')}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  )}
                </div>
              </div>
            </section>

                        {/* AVAILABLE POLICIES TABLE */}
            <section>
              <div className="d-flex justify-content-between align-items-center mb-3">
                <h4 className="fw-bold mb-0">Available for you</h4>
                <Link to="/available-policies" className="btn btn-primary btn-sm rounded-3 px-3">
                  View All & Register
                </Link>
              </div>
              
              {dashboard.availablePolicies.length === 0 ? (
                <div className="p-4 bg-white rounded-4 text-muted text-center shadow-sm">No additional products available right now.</div>
              ) : (
                <div className="card border-0 shadow-sm rounded-4 overflow-hidden">
                  <div className="card-body p-0">
                    <div className="table-responsive">
                      <table className="table table-hover align-middle mb-0">
                        <thead className="table-light">
                          <tr>
                            <th className="px-4 py-3 text-uppercase text-muted small fw-bold">Code</th>
                            <th className="py-3 text-uppercase text-muted small fw-bold">Policy Name</th>
                            <th className="py-3 text-uppercase text-muted small fw-bold">Type</th>
                            <th className="py-3 text-uppercase text-muted small fw-bold">Premium</th>
                            <th className="py-3 text-uppercase text-muted small fw-bold">Sum Insured</th>
                          </tr>
                        </thead>
                        <tbody>
                          {dashboard.availablePolicies.map((product) => (
                            <tr key={product.productId}>
                              <td className="px-4 fw-medium text-dark">{product.productCode}</td>
                              <td>{product.policyName}</td>
                              <td>
                                <span className="badge bg-primary bg-opacity-10 text-primary px-3 py-2 rounded-pill">
                                  {product.policyType}
                                </span>
                              </td>
                              <td className="fw-semibold">₹{Number(product.premiumAmount).toLocaleString('en-IN')}</td>
                              <td className="text-muted">₹{Number(product.sumInsured).toLocaleString('en-IN')}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                </div>
              )}
            </section>
          </>
        )}
      </div>
    </div>
  )
}

function StatisticCard({ label, value }) {
  return (
    <div className="col-12 col-md-6 col-xl-3">
      <div className="card h-100 border-0 shadow-sm rounded-4 p-2" style={{ backgroundColor: '#ffffff', borderLeft: '5px solid #FF8C69' }}>
        <div className="card-body">
          <p className="small text-muted fw-bold mb-2 text-uppercase" style={{ letterSpacing: '0.5px' }}>{label}</p>
          <div className="d-flex align-items-center justify-content-between">
            <h2 className="display-5 fw-bold mb-0 text-dark">{value}</h2>
            <div className="bg-light rounded-circle d-flex align-items-center justify-content-center" style={{ width: '50px', height: '50px', color: '#FF8C69' }}>
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" fill="currentColor" className="bi bi-file-earmark-text" viewBox="0 0 16 16">
                <path d="M5.5 7a.5.5 0 0 0 0 1h5a.5.5 0 0 0 0-1h-5zM5 9.5a.5.5 0 0 1 .5-.5h5a.5.5 0 0 1 0 1h-5a.5.5 0 0 1-.5-.5zm0 2a.5.5 0 0 1 .5-.5h2a.5.5 0 0 1 0 1h-2a.5.5 0 0 1-.5-.5z"/>
                <path d="M9.5 0H4a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V4.5L9.5 0zm0 1v2A1.5 1.5 0 0 0 11 4.5h2V14a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1h5.5z"/>
              </svg>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default DashboardPage