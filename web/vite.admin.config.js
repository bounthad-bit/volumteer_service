import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

/**
 * 管理后台独立前端入口（端口 5174）。
 * root 指向 admin/（仅含 index.html 与启动脚本），业务代码复用 ../src，通过别名 @ 引入，
 * 与前台官网（vite.config.js，端口 5173）各自独立开发与构建。
 */
export default defineConfig({
  plugins: [vue()],
  root: fileURLToPath(new URL("./admin", import.meta.url)),
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url))
    }
  },
  server: {
    port: 5174,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: fileURLToPath(new URL("./dist-admin", import.meta.url)),
    emptyOutDir: true
  }
});
