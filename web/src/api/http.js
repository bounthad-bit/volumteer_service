import axios from "axios";
import { ElMessage } from "element-plus";
import { useAuthStore } from "../stores/auth";

const http = axios.create({
  baseURL: "/api/v1",
  timeout: 15000
});

http.interceptors.request.use((config) => {
  const auth = useAuthStore();
  if (auth.token) {
    config.headers.Authorization = `Bearer ${auth.token}`;
  }
  return config;
});

http.interceptors.response.use(
  (response) => {
    const { data } = response;
    // 统一响应体：code=200 视为成功，其余按业务失败处理
    if (data && data.code !== undefined && data.code !== 200) {
      const error = new Error(data.msg || "操作未能完成");
      error.response = response;
      return Promise.reject(error);
    }
    return data;
  },
  (error) => {
    const status = error.response && error.response.status;
    if (status === 401) {
      const auth = useAuthStore();
      auth.logout();
      if (window.location.pathname !== "/login") {
        ElMessage.warning("登录状态已失效，请重新登录");
        window.location.href = "/login";
      }
    } else if (status === 403) {
      ElMessage.error("当前账号无权执行该操作");
    } else if (!error.response) {
      ElMessage.error("无法连接后端服务，请确认后端已启动");
    }
    return Promise.reject(error);
  }
);

/** 从响应中取出 data 字段 */
export function unwrap(res) {
  return res ? res.data : null;
}

export default http;
