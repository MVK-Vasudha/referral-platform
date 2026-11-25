import axiosInstance from "../../../api/axiosInstance";

/**
 * GET /users/me
 */
export const getMyProfile = async () => {
  const response = await axiosInstance.get("/users/me");

  // Unwrap ApiResponse
  return response.data.data;
};

/**
 * PUT /users/me
 */
export const updateMyProfile = async (payload) => {
  const response = await axiosInstance.put("/users/me", payload);

  // Unwrap ApiResponse
  return response.data.data;
};