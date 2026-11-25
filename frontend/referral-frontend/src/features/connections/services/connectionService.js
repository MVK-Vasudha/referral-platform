import axiosInstance from "../../../api/axiosInstance";

export const getMyConnections = async ({ page = 0, size = 10 }) => {
  const response = await axiosInstance.get("/connections/me", {
    params: { page, size },
  });

  return response.data.data;
};

export const sendConnectionRequest = async (userId) => {
  const response = await axiosInstance.post(`/connections/request/${userId}`);
  return response.data.data;
};

export const getPendingConnections = async ({ page = 0, size = 10 }) => {
  const response = await axiosInstance.get("/connections/pending", {
    params: { page, size },
  });

  return response.data.data;
};

export const getSentPendingConnections = async ({ page = 0, size = 10 }) => {
  const response = await axiosInstance.get("/connections/sent", {
    params: { page, size },
  });

  return response.data.data;
};

export const getMutualConnections = async (userId, { page = 0, size = 10 } = {}) => {
  const response = await axiosInstance.get(`/connections/${userId}/mutual`, {
    params: { page, size },
  });

  return response.data.data;
};

export const acceptConnectionRequest = async (id) => {
  const response = await axiosInstance.put(`/connections/${id}/accept`);
  return response.data;
};

export const rejectConnectionRequest = async (id) => {
  const response = await axiosInstance.put(`/connections/${id}/reject`);
  return response.data;
};

export const cancelOrRemoveConnection = async (id) => {
  const response = await axiosInstance.delete(`/connections/${id}`);
  return response.data;
};
