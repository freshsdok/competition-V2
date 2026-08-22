import { defineConfig, loadEnv } from "vite";
import vue from "@vitejs/plugin-vue";
import path from "path";
import AutoImport from "unplugin-auto-import/vite";
import Components from "unplugin-vue-components/vite";
import ReactivityTransform from "@vue-macros/reactivity-transform/vite";
import { lazyImport, VxeResolver } from "vite-plugin-lazy-import";

const gatewayUrl = "http://127.0.0.1:9889";
const imPlatformUrl = "http://127.0.0.1:8888";
const imServerUrl = "http://127.0.0.1:8887";
const imSocketUrl = "ws://127.0.0.1:8878";

// https://vite.dev/config/
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd());
  const { VITE_APP_ENV } = env;
  return {
    plugins: [
      vue(),
      AutoImport({
        imports: ["vue"], // 自动导入 Vue 3 的 API
        dts: "src/auto-imports.d.ts", // 生成类型声明
      }),
      ReactivityTransform({
        // 👇 关键：排除外部资源目录
        exclude: [
          /node_modules/,
          /public/,
          /lib/, // ← 你放 vform 的目录
          /\.umd\.js$/, // ← 排除所有 .umd.js 文件
        ],
      }),
      Components({
        // 自动注册组件
        dirs: ["src/components/DS_C"],
        extensions: ["vue"],
        include: [/\.(vue|md)$/],
        exclude: [
          /[\\/]node_modules[\\/]/,
          /[\\/].git[\\/]/,
          /[\\/].nuxt[\\/]/,
        ],
        dts: "src/components.d.ts", // 生成组件类型声明文件
        resolvers: [],
      }),
      lazyImport({
        resolvers: [
          VxeResolver({
            libraryName: "vxe-table",
          }),
        ],
      }),
    ],
    // 预编译
    optimizeDeps: {
      include: [
        "vue",
        "vue-router",
        "pinia",
        "axios",
        "@vueuse/core",
        "echarts",
        "vue-i18n",
        "@vueup/vue-quill",
        "element-plus",
        "jquery",
        "@/../lib/vform/designer.umd.js",
        "@/../lib/vmform/designer.umd.js",
      ],
    },
    build: {
      outDir: "pc", // 指定输出目录为pc
      assetsDir: "assets",
      commonjsOptions: {
        include: /node_modules|lib/, //这里记得把lib目录加进来，否则生产打包会报错！！
      },
    },
    resolve: {
      alias: {
        // 设置路径
        "~": path.resolve(__dirname, "./"),
        // 设置别名
        "@": path.resolve(__dirname, "./src"),
      },
      // https://cn.vitejs.dev/config/#resolve-extensions
      extensions: [".mjs", ".js", ".ts", ".jsx", ".tsx", ".json", ".vue"],
    },
    server: {
      port: 8082,
      host: true,
      open: true,
      proxy: {
        '/dev-api': {
          // target: 'http://8.130.171.65:8897',//正式环境！！！！
          //  target: "https://wxapp.ksup.cn", //测试
          //  target: "http://8.130.155.88:7788", //测试
          target: gatewayUrl,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api/, '')
        },
        '/im-platform': {
          target: imPlatformUrl,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/im-platform/, '')
        },
        '/im-server': {
          target: imServerUrl,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/im-server/, '')
        },
        '/im-ws': {
          target: imSocketUrl,
          changeOrigin: true,
          ws: true,
          rewrite: (p) => p.replace(/^\/im-ws/, '')
        },
        // "/dev-api": {
        //   //  target: 'http://192.168.1.135:9998',//萧
        //   target: 'http://192.168.1.108:9998',//东浩
        //   // target: "http://192.168.1.132:9889", //军舰
        //   // target: 'http://192.168.1.155:9998',//张超
        //   // target: 'http://192.168.1.137:9998',//陈柏宇
        //   changeOrigin: true,
        //   rewrite: (p) => p.replace(/^\/dev-api/, ""),
        // },
      },
    },
    css: {
      preprocessorOptions: {
        scss: {
          javascriptEnabled: true,
          additionalData: (content, loaderContext) => {
            if (loaderContext.endsWith("styles/variables.scss")) {
              return content;
            }
            return `@use "./src/styles/variables.scss" as *; ${content}`;
          },
        },
      },
    },
    test: {
      globals: true,
      environment: 'happy-dom',
      include: ['**/*.{test,spec}.{js,ts,jsx,tsx}'],
    },
  };
});
