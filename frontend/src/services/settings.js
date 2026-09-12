// src/services/settings.js
import api from "./api";

export const fetchUserSettings = async () => {
  try {
    const response = await api.get("/settings/user");
    return response.data;
  } catch (error) {
    console.error("Failed to fetch user settings:", error);
    return null;
  }
};

export const updateUserSettings = async (settings) => {
  try {
    const response = await api.put("/settings/user", settings);
    return response.data;
  } catch (error) {
    console.error("Failed to update user settings:", error);
    throw error;
  }
};