import { useEffect, useState } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import api from '../api.js'

function PolicyRegistrationPage() {
  const { productId } = useParams()
  const navigate = useNavigate()
  
  const [product, setProduct] = useState(null)
  const [customer, setCustomer] = useState(null)
  const [startDate, setStartDate] = useState(new Date().toISOString().split('T')[0])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [successMsg, setSuccessMsg] = useState('')

  // 1. Fetch product details and customer details for pre-filling
  useEffect(() => {
    // Assuming you have an endpoint that returns all products, or you fetch from dashboard
    // For simplicity, let's fetch the dashboard data to get customer info + available products
    api.get('/dashboard')
      .then(({ data }) => {
        setCustomer(data.customer)
        // Find the specific product from the available list
        const selectedProduct = data.availablePolicies.find(p => p.productId == productId)
        if (selectedProduct) {
          setProduct(selectedProduct)
        } else {
          setError('Product not found or already registered.')
        }
      })
      .catch(() => setError('Failed to load data.'))
      .finally(() => setLoading(false))
  }, [productId])

  // 2. Calculate Maturity Date and Total Premium dynamically
  let maturityDate = ''
  let totalPremium = 0

  if (product && startDate) {
    const start = new Date(startDate)
    // Calculate maturity date based on termMonths
    const maturity = new Date(start)
    maturity.setMonth(maturity.getMonth() + product.termMonths)
    maturityDate = maturity.toISOString().split('T')[0]

    // Calculate total premium based on frequency
    const termYears = product.termMonths / 12
    if (product.premiumFrequency === 'MONTHLY') totalPremium = product.premiumAmount * product.termMonths
    else if (product.premiumFrequency === 'QUARTERLY') totalPremium = product.premiumAmount * (termYears * 4)
    else if (product.premiumFrequency === 'HALF-YEARLY') totalPremium = product.premiumAmount * (termYears * 2)
    else if (product.premiumFrequency === 'YEARLY') totalPremium = product.premiumAmount * termYears
  }

  // 3. Handle Form Submit
  function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setSuccessMsg('')

    const requestPayload = {
      productId: product.productId,
      startDate: startDate
    }

    api.post('/policies/register', requestPayload)
            .then(({ data }) => {
        // Fallback in case the backend returns the policy ID instead of policyNumber
        const polNum = data.policyNumber || data.policyId || 'Generated Successfully';
        setSuccessMsg(`Success! Policy registered. Policy Number: ${polNum}`);
        
        // Wait 5 seconds so you can definitely read it before it redirects
        setTimeout(() => navigate('/dashboard'), 5000);
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Registration failed. Please check eligibility.')
      })
  }

  if (loading) return <div className="container py-5">Loading form...</div>
  if (!product) return <div className="container py-5 text-danger">{error}</div>

  return (
    <div className="dashboard-shell">
      <nav className="navbar navbar-expand-lg bg-white border-bottom">
        <div className="container">
          <Link className="navbar-brand fw-bold text-primary" to="/dashboard">HealthDekho</Link>
          <Link to="/available-policies" className="btn btn-outline-secondary btn-sm">Back to Products</Link>
        </div>
      </nav>

      <main className="container py-5">
        <div className="row justify-content-center">
          <div className="col-lg-8">
            <h1 className="h3 fw-bold mb-4">Register for {product.policyName}</h1>
            
            {error && <div className="alert alert-danger">{error}</div>}
            {successMsg && <div className="alert alert-success">{successMsg}</div>}

            <form onSubmit={handleSubmit} className="card border-0 shadow-sm p-4">
              
              <h5 className="mb-3 text-secondary border-bottom pb-2">Customer Details (Auto-filled)</h5>
              <div className="row mb-3">
                <div className="col-md-6">
                  <label className="form-label">Customer Name</label>
                  <input type="text" className="form-control" value={customer?.fullName || ''} disabled />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Date of Birth</label>
                  <input type="text" className="form-control" value={customer?.dateOfBirth || ''} disabled />
                </div>
              </div>

              <h5 className="mb-3 mt-4 text-secondary border-bottom pb-2">Policy Details</h5>
              <div className="row mb-3">
                <div className="col-md-6">
                  <label className="form-label">Policy Type</label>
                  <input type="text" className="form-control" value={product.policyType} disabled />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Policy Term</label>
                  <input type="text" className="form-control" value={`${product.termMonths} months`} disabled />
                </div>
              </div>

              <div className="row mb-3">
                <div className="col-md-6">
                  <label className="form-label">Policy Start Date <span className="text-danger">*</span></label>
                  <input 
                    type="date" 
                    className="form-control" 
                    value={startDate} 
                    onChange={(e) => setStartDate(e.target.value)} 
                    required 
                  />
                </div>
                <div className="col-md-6">
                  <label className="form-label">Policy Maturity Date</label>
                  <input type="date" className="form-control" value={maturityDate} disabled />
                </div>
              </div>

              <h5 className="mb-3 mt-4 text-secondary border-bottom pb-2">Financial Details</h5>
              <div className="row mb-4">
                <div className="col-md-4">
                  <label className="form-label">Premium Amount</label>
                  <input type="text" className="form-control" value={`₹${Number(product.premiumAmount).toLocaleString('en-IN')}`} disabled />
                </div>
                <div className="col-md-4">
                  <label className="form-label">Payment Frequency</label>
                  <input type="text" className="form-control" value={product.premiumFrequency} disabled />
                </div>
                <div className="col-md-4">
                  <label className="form-label fw-bold text-success">Total Premium Payable</label>
                  <input type="text" className="form-control fw-bold bg-success-subtle" value={`₹${Number(totalPremium).toLocaleString('en-IN')}`} disabled />
                </div>
              </div>

              <div className="d-grid mt-2">
                <button type="submit" className="btn btn-primary btn-lg" disabled={!!successMsg}>
                  Confirm & Register Policy
                </button>
              </div>
            </form>
          </div>
        </div>
      </main>
    </div>
  )
}

export default PolicyRegistrationPage