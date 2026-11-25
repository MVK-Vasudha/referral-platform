import { createBrowserRouter } from "react-router-dom";
import ProtectedRoute from "../components/layout/ProtectedRoute";
import AppShell from "../components/layout/AppShell";

import LoginPage from "../features/auth/pages/LoginPage";
import RegisterPage from "../features/auth/pages/RegisterPage";
import ProfilePage from "../features/profile/pages/ProfilePage";
import ConnectionsPage from "../features/connections/pages/ConnectionsPage";
import ReferralsPage from "../features/referrals/pages/ReferralsPage";
import SearchPage from "../features/search/pages/SearchPage";

const router = createBrowserRouter([
  {
    path: "/login",
    element: <LoginPage />,
  },
  {
    path: "/register",
    element: <RegisterPage />,
  },
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <AppShell />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <ProfilePage /> },
      { path: "profile", element: <ProfilePage /> },
      { path: "connections", element: <ConnectionsPage /> },
      { path: "referrals", element: <ReferralsPage /> },
      { path: "search", element: <SearchPage /> },
    ],
  },
]);

export default router;
