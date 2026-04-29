const TOKEN_KEY = "token";

export const getToken = () => {
  const token = localStorage.getItem(TOKEN_KEY);

  if (!token || token === "undefined" || token === "null") {
    return null;
  }

  return token;
};

export const setToken = (token) => {
  if (typeof token !== "string" || token.trim() === "") {
    localStorage.removeItem(TOKEN_KEY);
    return false;
  }

  localStorage.setItem(TOKEN_KEY, token);
  return true;
};

export const clearToken = () => {
  localStorage.removeItem(TOKEN_KEY);
};

export const isAuthenticated = () => getToken() !== null;
