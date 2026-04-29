import api from "./api";
import { clearToken, setToken } from "./tokenService";

export const login = async (username, password) => {
  const res = await api.post("/auth/login", {
    username,
    password,
  });

  const token = res.data?.token;
  const tokenSaved = setToken(token);

  if (!tokenSaved) {
    throw new Error("Login response did not contain a valid token.");
  }

  await api.get("/users/me");

  return res.data;
};

export const register = async (username, password) => {
  const res = await api.post("/auth/register", {
    username,
    password,
  });

  return res.data;
};

export const logout = () => {
  clearToken();
};
