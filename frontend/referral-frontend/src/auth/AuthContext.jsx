import { useState } from "react";
import {
  login as loginService,
  register as registerService,
} from "../features/auth/services/authService";
import {
  getToken,
  setToken as storeToken,
  clearToken,
} from "../utils/tokenStorage";
import { AuthContext } from "./authContext";

export const AuthProvider = ({ children }) => {
  const [token, setTokenState] = useState(() => getToken());
  const [user, setUser] = useState(null);
  const [loading] = useState(false);

  const login = async (credentials) => {
    const data = await loginService(credentials);
    const jwt = data.token;

    if (!jwt) {
      throw new Error("Token not found in response");
    }

    storeToken(jwt);
    setTokenState(jwt);
    setUser(null);

    return true;
  };

  const register = async (formData) => {
    await registerService(formData);
    return true;
  };

  const logout = () => {
    clearToken();
    setTokenState(null);
    setUser(null);
  };

  const value = {
    token,
    user,
    login,
    register,
    logout,
    isAuthenticated: !!token,
    loading,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};
