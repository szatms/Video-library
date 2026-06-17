import api from "./api";

export const getTrashItems = async () => {
  const response = await api.get("/trash");
  return response.data;
};

export const restoreTrashItem = async (restoreId) => {
  const response = await api.post(`/trash/restore/${restoreId}`);
  return response.data;
};