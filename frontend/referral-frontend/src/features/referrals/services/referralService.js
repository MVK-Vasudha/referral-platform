import axiosInstance from "../../../api/axiosInstance";

export const getSentReferrals = async ({ page = 0, size = 10 }) => {
  const response = await axiosInstance.get("/referrals/sent", {
    params: { page, size },
  });

  return response.data.data;
};

export const createReferral = async (payload) => {
  const response = await axiosInstance.post("/referrals/request", payload);
  return response.data.data;
};

export const getReceivedReferrals = async ({ page = 0, size = 10 }) => {
  const response = await axiosInstance.get("/referrals/received", {
    params: { page, size },
  });

  return response.data.data;
};

export const acceptReferral = async (id) => {
  const response = await axiosInstance.put(`/referrals/${id}/accept`);
  return response.data.data;
};

export const rejectReferral = async (id) => {
  const response = await axiosInstance.put(`/referrals/${id}/reject`);
  return response.data.data;
};
