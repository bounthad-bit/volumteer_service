import http from "./http";

export const checkInApi = (data) => http.post("/check-ins", data);
export const checkOutApi = (data) => http.put("/check-ins/check-out", data);
export const pageMyCheckInsApi = (params) => http.get("/check-ins/mine", { params });
export const pageActivityCheckInsApi = (activityId, params) => http.get(`/check-ins/activity/${activityId}`, { params });
