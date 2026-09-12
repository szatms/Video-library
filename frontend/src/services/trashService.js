import api from "./api";

export const getDeletedUserVideos = async () => {
  try {
    const response = await api.get("/trash/videos");
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      throw new Error("Authentication required");
    }
    throw error;
  }
};

export const getDeletedUserPlaylists = async () => {
  try {
    const response = await api.get("/trash/playlists");
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      throw new Error("Authentication required");
    }
    throw error;
  }
};

export const getDeletedNotes = async () => {
  try {
    const response = await api.get("/trash/notes");
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      throw new Error("Authentication required");
    }
    throw error;
  }
};

export const getDeletedCodices = async () => {
  try {
    const response = await api.get("/trash/codices");
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      throw new Error("Authentication required");
    }
    throw error;
  }
};

export const restoreVideo = async (restoreId) => {
  try {
    const response = await api.post(`/trash/restore/videos/${restoreId}`);
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      throw new Error("Authentication required");
    }
    throw error;
  }
};

export const restorePlaylist = async (restoreId) => {
  try {
    const response = await api.post(`/trash/restore/playlists/${restoreId}`);
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      throw new Error("Authentication required");
    }
    throw error;
  }
};

export const restoreNote = async (restoreId) => {
  try {
    const response = await api.post(`/trash/restore/notes/${restoreId}`);
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      throw new Error("Authentication required");
    }
    throw error;
  }
};

export const restoreCodex = async (restoreId) => {
  try {
    const response = await api.post(`/trash/restore/codices/${restoreId}`);
    return response.data;
  } catch (error) {
    if (error.response?.status === 401) {
      throw new Error("Authentication required");
    }
    throw error;
  }
};