import axiosInstance from "../../../api/axiosInstance";

export const searchUsers = async ({
  page = 0,
  size = 10,
  name = "",
  headline = "",
  company = "",
  skills = "",
  signal,
}) => {
  const params = { page, size };

  if (name.trim()) params.name = name.trim();
  if (headline.trim()) params.headline = headline.trim();
  if (company.trim()) params.company = company.trim();
  if (skills.trim()) params.skills = skills.trim();

  const response = await axiosInstance.get("/users/search", {
    params,
    signal,
  });

  return response.data.data;
};
