import { Navigate } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";

const ProtectedRoute = ({ children }) => {
  const { token, loading } = useAuth();

  // ⏳ Wait until auth state is restored
  if (loading) {
    return null; // or spinner
  }

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;