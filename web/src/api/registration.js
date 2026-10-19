import http from "./http";

export const applyActivityApi = (activityId) => http.post("/registrations", null, { params: { activityId } });
export const cancelMyRegistrationApi = (id) => http.delete(`/registrations/${id}`);
export const pageMyRegistrationsApi = (params) => http.get("/registrations/mine", { params });
export const pageActivityRegistrationsApi = (activityId, params) =>
  http.get(`/registrations/activity/${activityId}`, { params });
export const pageOrganizerRegistrationsApi = (params) => http.get("/registrations/organizer", { params });
export const reviewRegistrationApi = (id, pass, remark) => http.put(`/registrations/${id}/review`, { pass, remark });
