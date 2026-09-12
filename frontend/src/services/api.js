import axios from "axios";
import { clearToken, getToken } from "./tokenService";

const publicEndpoints = [
  "/auth/has-users",
  "/auth/register",
  "/auth/login",
];

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
});

api.interceptors.request.use((config) => {
  const isPublic = publicEndpoints.includes(config.url);

  if (!isPublic) {
    const token = getToken();

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }

  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const isPublic = publicEndpoints.includes(error.config?.url);

      if (!isPublic) {
        clearToken();
        window.location.href = "/";
      }
    }

    return Promise.reject(error);
  }
);

export default api;