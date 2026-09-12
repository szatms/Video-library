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

  return res.data;
};

export const register = async (username, password) => {
  const res = await api.post("/auth/register", {
    username,
    password,
  });

  const token = res.data?.token;
  const tokenSaved = setToken(token);

  if (!tokenSaved) {
    throw new Error("Registration response did not contain a valid token.");
  }

  return res.data;
};

export const logout = () => {
  clearToken();
};
