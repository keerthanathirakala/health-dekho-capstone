import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api.js'

function PaymentHistoryPage() {
  const [policies, setPolicies] = useState([])
  const [selectedPolicyId, setSelectedPolicyId] = useState('')
  const [installments, setInstallments] = useState([])
  const [payments, setPayments] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [successMsg, setSuccessMsg] = useState('')

  // 1. Fetch user's policies on mount
  useEffect(() => {
    api.get('/policies/my')
      .then(({ data }) => {
        setPolicies(data)
        if (data.length > 0) {
          setSelectedPolicyId(data[0].policyId) // Select first policy by default
        }
      })
      .catch(() => setError('Could not load policies.'))
  }, [])

  // 2. Fetch installments and payments when a policy is selected
  useEffect(() => {
    if (!selectedPolicyId) return
    setLoading(true)
    
    // Fetch both installments and payment history for the selected policy
    Promise.all([
      api.get(`/installments/policy/${selectedPolicyId}`),
      api.get(`/payments/policy/${selectedPolicyId}`)
    ])
      .then(([installmentsRes, paymentsRes]) => {
        setInstallments(installmentsRes.data)
        setPayments(paymentsRes.data)
      })
      .catch(() => setError('Could not load payment details.'))
      .finally(() => setLoading(false))
  }, [selectedPolicyId])

  // 3. Handle Payment
  function handlePay(installmentId, amount) {
    setError('')
    setSuccessMsg('')
    
    const payload = {
      installmentId: installmentId,
      amount: amount,
      paymentMethod: 'UPI' // Hardcoded for simplicity, you can add a dropdown later
    }

    api.post('/payments/pay', payload)
      .then(({ data }) => {
        setSuccessMsg(`Payment recorded. Principal: ₹${Number(data.amountPaid).toFixed(2)}; late penalty: ₹${Number(data.penaltyAmount).toFixed(2)}.`)
        // Refresh the data
        return Promise.all([
          api.get(`/installments/policy/${selectedPolicyId}`),
          api.get(`/payments/policy/${selectedPolicyId}`)
        ])
      })
      .then(([installmentsRes, paymentsRes]) => {
        setInstallments(installmentsRes.data)
        setPayments(paymentsRes.data)
      })
      .catch((err) => {
        setError(err.response?.data?.message || 'Payment failed.')
      })
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
        <h1 className="h3 fw-bold mb-4">Payments & Installments</h1>
        <p className="text-secondary">A 2% late penalty is added separately on the first payment after the grace period. Payments are recorded by this demo; no payment gateway is connected.</p>

        {error && <div className="alert alert-danger">{error}</div>}
        {successMsg && <div className="alert alert-success">{successMsg}</div>}

        {/* Policy Selector */}
        <div className="mb-4 col-md-6">
          <label className="form-label fw-bold">Select Policy</label>
          <select 
            className="form-select" 
            value={selectedPolicyId} 
            onChange={(e) => setSelectedPolicyId(e.target.value)}
          >
            {policies.map(p => (
              <option key={p.policyId} value={p.policyId}>
                {p.policyName} ({p.policyNumber})
              </option>
            ))}
          </select>
        </div>

        {loading && <p>Loading details...</p>}

        {!loading && selectedPolicyId && (
          <div className="row g-4">
            {/* Installments Table */}
            <div className="col-12 col-xl-7">
              <h4 className="h5 mb-3">Installment Schedule</h4>
              <div className="table-responsive card shadow-sm border-0">
                <table className="table table-hover mb-0">
                  <thead className="table-light">
                    <tr>
                      <th>#</th>
                      <th>Due Date</th>
                      <th>Amount</th>
                      <th>Status</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {installments.map((inst) => (
                      <tr key={inst.installmentId}>
                        <td>{inst.installmentNumber}</td>
                        <td>{inst.dueDate}</td>
                        <td>₹{Number(inst.scheduledAmount).toLocaleString('en-IN')}</td>
                        <td>
                          <span className={`badge ${inst.paymentStatus === 'PAID' ? 'text-bg-success' : inst.paymentStatus === 'PENDING' ? 'text-bg-warning' : 'text-bg-secondary'}`}>
                            {inst.paymentStatus}
                          </span>
                        </td>
                        <td>
                          {inst.paymentStatus !== 'PAID' && (
                            <button 
                              className="btn btn-primary btn-sm" 
                              type="button"
                              onClick={() => handlePay(inst.installmentId, ((Math.round(Number(inst.scheduledAmount) * 100) - Math.round(Number(inst.paidAmount) * 100)) / 100).toFixed(2))}
                            >
                              Pay Now
                            </button>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Payment History Table */}
            <div className="col-12 col-xl-5">
              <h4 className="h5 mb-3">Payment Transactions</h4>
              <div className="table-responsive card shadow-sm border-0">
                <table className="table table-sm table-hover mb-0">
                  <thead className="table-light">
                    <tr>
                      <th>Date</th>
                      <th>Ref ID</th>
                      <th>Amount</th>
                      <th>Penalty</th>
                    </tr>
                  </thead>
                  <tbody>
                    {payments.length === 0 ? (
                      <tr><td colSpan="4" className="text-center text-muted py-3">No payments yet</td></tr>
                    ) : (
                      payments.map((pay) => (
                        <tr key={pay.paymentId}>
                          <td>{pay.paymentDate}</td>
                          <td className="small text-muted">{pay.transactionReference}</td>
                          <td>₹{Number(pay.amountPaid).toLocaleString('en-IN')}</td>
                          <td>₹{Number(pay.penaltyAmount).toLocaleString('en-IN')}</td>
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  )
}

export default PaymentHistoryPage