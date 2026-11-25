const TOKEN_KEY = "auth_token";

// ✅ get token
export const getToken = () => {
  return localStorage.getItem(TOKEN_KEY);
};

// ✅ set token
export const setToken = (token) => {
  localStorage.setItem(TOKEN_KEY, token);
};

// ✅ clear token
export const clearToken = () => {
  localStorage.removeItem(TOKEN_KEY);
};