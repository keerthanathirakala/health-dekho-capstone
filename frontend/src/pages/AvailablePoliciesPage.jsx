import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api.js'

function AvailablePoliciesPage() {
  const [products, setProducts] = useState([])
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    api.get('/products/available')
      .then(({ data }) => setProducts(data))
      .catch((err) => setError(err.response?.data?.message || 'Could not load products.'))
      .finally(() => setLoading(false))
  }, [])

  // When they click register, we send them to the registration page
  // and pass the product ID in the URL so the next page knows what they picked.
  function handleRegister(productId) {
    navigate(`/policies/register/${productId}`)
  }

  return (
    <div className="dashboard-shell">
      <nav className="navbar navbar-expand-lg bg-white border-bottom">
        <div className="container">
          <Link className="navbar-brand fw-bold text-primary" to="/dashboard">HealthDekho</Link>
          <Link to="/dashboard" className="btn btn-outline-secondary btn-sm">Back to Dashboard</Link>
        </div>
      </nav>

      <main className="container py-5">
        <h1 className="h3 fw-bold mb-4">Available Insurance Products</h1>
        
        {error && <div className="alert alert-danger">{error}</div>}
        {loading && <p>Loading products...</p>}

        {!loading && !error && (
          <div className="table-responsive">
            <table className="table table-hover border shadow-sm">
              <thead className="table-light">
                <tr>
                  <th>Product Code</th>
                  <th>Policy Name</th>
                  <th>Type</th>
                  <th>Term</th>
                  <th>Sum Insured</th>
                  <th>Premium</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {products.map((product) => (
                  <tr key={product.productId}>
                    <td>{product.productCode}</td>
                    <td>{product.policyName}</td>
                    <td>{product.policyType}</td>
                    <td>{product.termMonths} months</td>
                    <td>₹{Number(product.sumInsured).toLocaleString('en-IN')}</td>
                    <td>₹{Number(product.premiumAmount).toLocaleString('en-IN')}</td>
                    <td>
                      <button 
                        className="btn btn-primary btn-sm" 
                        onClick={() => handleRegister(product.productId)}
                        type="button" 
                      >
                        Register
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </main>
    </div>
  )
}

export default AvailablePoliciesPage