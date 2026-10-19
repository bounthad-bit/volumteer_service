import http from "./http";

export const submitEvaluationApi = (data) => http.post("/evaluations", data);
export const pageActivityEvaluationsApi = (activityId, params) =>
  http.get(`/evaluations/activity/${activityId}`, { params });
export const pageOrganizerEvaluationsApi = (params) => http.get("/evaluations/organizer", { params });
