import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { TOKEN_STORAGE_KEY } from '../api.js'

function ProtectedRoute() {
  const location = useLocation()

  if (!localStorage.getItem(TOKEN_STORAGE_KEY)) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  return <Outlet />
}

export default ProtectedRoute
