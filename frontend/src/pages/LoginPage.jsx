import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import api, { TOKEN_STORAGE_KEY } from '../api.js'

function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setIsSubmitting(true)
    try {
      const { data } = await api.post('/auth/login', { email, password })
      localStorage.setItem(TOKEN_STORAGE_KEY, data.token)
      const destination = location.state?.from?.pathname || '/dashboard'
      navigate(destination === '/login' ? '/dashboard' : destination, { replace: true })
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Unable to sign in. Check your credentials.')
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <div className="d-flex min-vh-100 bg-white p-3">
      {/* LEFT SIDE - IMAGE */}
      <div className="d-none d-lg-block col-lg-6 login-image-side position-relative">
        <div className="position-absolute bottom-0 start-0 p-5 text-white">
          <h1 className="display-5 fw-bold mb-2">Secure Your Future.</h1>
          <p className="lead mb-0">Manage your health and insurance in one place.</p>
        </div>
      </div>

      {/* RIGHT SIDE - FORM */}
      <div className="col-12 col-lg-6 d-flex align-items-center justify-content-center">
        <div className="w-100" style={{ maxWidth: '400px' }}>
          <div className="text-center mb-5">
            <h2 className="fw-bold text-primary mb-1">HealthDekho</h2>
            <p className="text-muted">Sign in to your account</p>
          </div>

          {error && <div className="alert alert-danger rounded-3 shadow-sm border-0">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="mb-4">
              <label className="form-label fw-semibold text-secondary small">Email address</label>
              <input
                type="email"
                className="form-control form-control-lg bg-light border-0"
                placeholder="Enter your email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
            </div>
            <div className="mb-4">
              <label className="form-label fw-semibold text-secondary small">Password</label>
              <input
                type="password"
                className="form-control form-control-lg bg-light border-0"
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
              />
            </div>
            <button className="btn btn-primary btn-lg w-100 shadow-sm" disabled={isSubmitting} type="submit">
              {isSubmitting ? 'Signing in...' : 'Sign In'}
            </button>
          </form>
          <div className="text-center mt-4">
            <span className="text-muted small">New to HealthDekho? </span>
            <button 
              type="button" 
              className="btn btn-link text-decoration-none p-0 small fw-bold text-primary"
              onClick={() => alert("Please contact HealthDekho support to register a new account.")}
            >
              Create an account
            </button>
          </div>
           <p className="small text-muted mt-4 mb-0 text-center">
            Your account and policies, in one place.
          </p>
        </div>
      </div>
    </div>
  )
}

export default LoginPage