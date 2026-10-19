import { defineStore } from "pinia";
import { loginApi, registerApi } from "../api/auth";

const KEYS = ["token", "userId", "username", "realName", "phone", "studentNo", "role", "roleLabel"];

function load() {
  const state = {};
  KEYS.forEach((k) => (state[k] = localStorage.getItem(k) || ""));
  state.userId = Number(state.userId || 0);
  state.role = Number(state.role || 0);
  state.token = state.token || "";
  return state;
}

export const useAuthStore = defineStore("auth", {
  state: () => load(),
  getters: {
    isLogin: (s) => !!s.token,
    isVolunteer: (s) => s.role === 1,
    isOrganizer: (s) => s.role === 2,
    isAdmin: (s) => s.role === 3
  },
  actions: {
    persist() {
      KEYS.forEach((k) => localStorage.setItem(k, String(this[k] ?? "")));
    },
    applyPayload(payload) {
      this.token = payload.token;
      this.userId = payload.userId;
      this.username = payload.username;
      this.realName = payload.realName;
      this.phone = payload.phone || "";
      this.studentNo = payload.studentNo || "";
      this.role = payload.role;
      this.roleLabel = payload.roleLabel;
      this.persist();
    },
    async login(username, password) {
      const res = await loginApi({ username, password });
      this.applyPayload(res.data);
      return res.data;
    },
    async register(form) {
      const res = await registerApi(form);
      this.applyPayload(res.data);
      return res.data;
    },
    logout() {
      KEYS.forEach((k) => localStorage.removeItem(k));
      this.token = "";
      this.userId = 0;
      this.username = "";
      this.realName = "";
      this.phone = "";
      this.studentNo = "";
      this.role = 0;
      this.roleLabel = "";
    },
    refreshLocal(payload) {
      if (payload.realName !== undefined) this.realName = payload.realName;
      if (payload.phone !== undefined) this.phone = payload.phone;
      if (payload.studentNo !== undefined) this.studentNo = payload.studentNo;
      this.persist();
    }
  }
});
