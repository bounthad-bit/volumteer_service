import http from "./http";

export const pageNoticesApi = (params) => http.get("/notices", { params });
export const getNoticeApi = (id) => http.get(`/notices/${id}`);
export const createNoticeApi = (data) => http.post("/notices", data);
export const updateNoticeApi = (id, data) => http.put(`/notices/${id}`, data);
export const deleteNoticeApi = (id) => http.delete(`/notices/${id}`);
