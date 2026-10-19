import http from "./http";

export const pagePublicActivitiesApi = (params) => http.get("/activities", { params });
export const pageMyActivitiesApi = (params) => http.get("/activities/mine", { params });
export const pageAdminActivitiesApi = (params) => http.get("/activities/admin", { params });
export const getActivityDetailApi = (id) => http.get(`/activities/${id}`);
export const createActivityApi = (data) => http.post("/activities", data);
export const updateActivityApi = (id, data) => http.put(`/activities/${id}`, data);
export const cancelActivityApi = (id) => http.delete(`/activities/${id}`);
export const auditActivityApi = (id, pass, remark) => http.put(`/activities/${id}/audit`, { pass, remark });
