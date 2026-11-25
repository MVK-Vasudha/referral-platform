import axiosInstance from "../../../api/axiosInstance";

export const login = async (credentials) => {
  try {
    const response = await axiosInstance.post("/auth/login", credentials);
    return response.data.data;
  } catch (error) {
    const message =
      error.response?.data?.message ||
      error.message ||
      "Login failed";

    throw new Error(message);
  }
};

export const register = async (data) => {
  try {
    const response = await axiosInstance.post("/auth/register", data);
    return response.data.data;
  } catch (error) {
    const message =
      error.response?.data?.message ||
      error.message ||
      "Registration failed";

    throw new Error(message);
  }
};
