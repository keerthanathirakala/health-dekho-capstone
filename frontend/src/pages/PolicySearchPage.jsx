import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api.js'

function PolicySearchPage() {
  const [name, setName] = useState('')
  const [number, setNumber] = useState('')
  const [filters, setFilters] = useState({ policyName: '', policyNumber: '' })
  const [page, setPage] = useState(0)
  const [sort, setSort] = useState('createdAt,desc')
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    api.get('/policies/search', { params: { ...filters, page, size: 10, sort } })
      .then(({ data }) => { if (active) setResult(data) })
      .catch((err) => {
        if (active) {
          setResult(null)
          setError(err.response?.data?.message || 'Unable to load policies. Please try again.')
        }
      })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [filters, page, sort])

  function search(event) {
    event.preventDefault()
    setLoading(true)
    setError('')
    setPage(0)
    setFilters({ policyName: name.trim(), policyNumber: number.trim() })
  }

  return (
    <div className="dashboard-shell">
      <nav className="navbar bg-white border-bottom">
        <div className="container">
          <Link className="navbar-brand fw-bold text-primary" to="/dashboard">HealthDekho</Link>
          <Link to="/dashboard" className="btn btn-outline-secondary btn-sm">Back to Dashboard</Link>
        </div>
      </nav>
      <main className="container py-5">
        <h1 className="h3 fw-bold mb-4">Search my policies</h1>
        <form className="row g-3 mb-4" onSubmit={search}>
          <div className="col-md-4">
            <label htmlFor="policyName" className="form-label">Policy name</label>
            <input id="policyName" className="form-control" value={name} onChange={(e) => setName(e.target.value)} />
          </div>
          <div className="col-md-4">
            <label htmlFor="policyNumber" className="form-label">Policy number</label>
            <input id="policyNumber" className="form-control" value={number} onChange={(e) => setNumber(e.target.value)} />
          </div>
          <div className="col-md-4">
            <label htmlFor="sort" className="form-label">Sort by</label>
            <select id="sort" className="form-select" value={sort} onChange={(e) => { setLoading(true); setError(''); setPage(0); setSort(e.target.value) }}>
              <option value="createdAt,desc">Newest first</option>
              <option value="createdAt,asc">Oldest first</option>
              <option value="product.policyName,asc">Policy name A-Z</option>
              <option value="policyNumber,asc">Policy number A-Z</option>
            </select>
          </div>
          <div className="col-12"><button className="btn btn-primary" type="submit">Search</button></div>
        </form>
        {loading && <p role="status">Loading your policies...</p>}
        {error && <div className="alert alert-danger" role="alert">{error}</div>}
        {!loading && result && (
          <>
            {result.content.length === 0 ? <p>No policies match your search.</p> : (
              <div className="table-responsive">
                <table className="table">
                  <thead><tr><th>Policy name</th><th>Policy number</th><th>Status</th><th>Start date</th></tr></thead>
                  <tbody>{result.content.map((policy) => (
                    <tr key={policy.policyId}><td>{policy.policyName}</td><td>{policy.policyNumber}</td><td>{policy.status}</td><td>{policy.startDate}</td></tr>
                  ))}</tbody>
                </table>
              </div>
            )}
            {result.totalPages > 0 && (
              <div className="d-flex align-items-center gap-3">
                <button className="btn btn-outline-primary btn-sm" disabled={page === 0} onClick={() => { setLoading(true); setError(''); setPage(page - 1) }} type="button">Previous</button>
                <span>Page {result.page + 1} of {result.totalPages} ({result.totalElements} policies)</span>
                <button className="btn btn-outline-primary btn-sm" disabled={page + 1 >= result.totalPages} onClick={() => { setLoading(true); setError(''); setPage(page + 1) }} type="button">Next</button>
              </div>
            )}
          </>
        )}
      </main>
    </div>
  )
}

export default PolicySearchPage
