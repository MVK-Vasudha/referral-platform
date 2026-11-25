import axios from "axios";
import { getToken, clearToken } from "../utils/tokenStorage";

const axiosInstance = axios.create({
  baseURL: "http://localhost:8081",
  headers: {
    "Content-Type": "application/json",
  },
});

axiosInstance.interceptors.request.use(
  (config) => {
    const token = getToken();

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      "Request failed";

    if (status === 401) {
      clearToken();

      if (window.location.pathname !== "/login") {
        window.location.href = "/login";
      }
    }

    error.message = message;
    return Promise.reject(error);
  }
);

export default axiosInstance;
