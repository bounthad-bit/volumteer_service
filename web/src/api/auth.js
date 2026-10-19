import http from "./http";

export const loginApi = (data) => http.post("/auth/login", data);
export const registerApi = (data) => http.post("/auth/register", data);
