import api from "./api";

export const login = async (username, password) => {
  const res = await api.post("/auth/login", {
    username,
    password,
  });

  const token = res.data.token;

  localStorage.setItem("token", token);

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
  localStorage.removeItem("token");
};