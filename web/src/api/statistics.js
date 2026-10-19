import http from "./http";

export const pageHoursStatsApi = (params) => http.get("/statistics/hours", { params });
export const exportHoursApi = (params) => http.get("/statistics/hours/export", { params });
