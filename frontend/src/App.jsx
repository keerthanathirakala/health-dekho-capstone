import { Navigate, Route, Routes } from 'react-router-dom'
import DashboardPage from './pages/DashboardPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import ProtectedRoute from './pages/ProtectedRoute.jsx'
import AvailablePoliciesPage from './pages/AvailablePoliciesPage.jsx'
import PolicyRegistrationPage from './pages/PolicyRegistrationPage'
import PaymentHistoryPage from './pages/PaymentHistoryPage'
import RecommendationsPage from './pages/RecommendationsPage'
import PolicySearchPage from './pages/PolicySearchPage.jsx'
function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/available-policies" element={<AvailablePoliciesPage />} />
        <Route path="/policies/register/:productId" element={<PolicyRegistrationPage />} />
        <Route path="/payments" element={<PaymentHistoryPage />} />
        <Route path="/recommendations" element={<RecommendationsPage />} />
        <Route path="/policies/search" element={<PolicySearchPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}

export default App
