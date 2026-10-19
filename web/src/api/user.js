import http from "./http";

export const getMyProfileApi = () => http.get("/users/me");
export const updateMyProfileApi = (data) => http.put("/users/me", data);
export const changePasswordApi = (data) => http.put("/users/me/password", data);
export const pageUsersApi = (params) => http.get("/users", { params });
export const changeUserStatusApi = (id, status) => http.put(`/users/${id}/status`, null, { params: { status } });
