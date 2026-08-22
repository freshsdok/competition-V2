import { defineConfig, loadEnv } from "vite";
import path from "path";
import createVitePlugins from "./vite/plugins";
import ReactivityTransform from "@vue-macros/reactivity-transform/vite";
import { lazyImport, VxeResolver } from 'vite-plugin-lazy-import'

const gatewayUrl = "http://127.0.0.1:9889";
const imPlatformUrl = "http://127.0.0.1:8888";
const imServerUrl = "http://127.0.0.1:8887";
const imSocketUrl = "ws://127.0.0.1:8878";

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd());
  const { VITE_APP_ENV } = env;
  return {
    // 部署生产环境和开发环境下的URL。
    // 默认情况下，vite 会假设您的应用是被部署在一个域名的根路径上
    // 例如 https://www.ruoyi.vip/。如果应用被部署在一个子路径上，您就需要用这个选项指定这个子路径。例如，如果您的应用被部署在 https://www.ruoyi.vip/admin/，则设置 baseUrl 为 /admin/。
    base: VITE_APP_ENV === "production" ? "/" : "/",
    plugins: [
      ReactivityTransform({
        include: [/\.[jt]sx?$/, /\.vue$/],
        exclude: [/node_modules/, /vform\/.*\.umd\.js$/]
      }),
      ...createVitePlugins(env, command === "build"),
      lazyImport({
        resolvers: [
          VxeResolver({
            libraryName: 'vxe-table'
          })
        ]
      })
    ],
    optimizeDeps: {
      include: [
        'vue',
        'vue-router',
        'pinia',
        'axios',
        '@vueuse/core',
        'echarts',
        'vue-i18n',
        '@vueup/vue-quill',
        '@/../lib/vform/designer.umd.js', '@/../lib/vmform/designer.umd.js'
      ]
    },
    resolve: {
      // https://cn.vitejs.dev/config/#resolve-alias
      alias: {
        // 设置路径
        "~": path.resolve(__dirname, "./"),
        // 设置别名
        "@": path.resolve(__dirname, "./src"),
      },
      // https://cn.vitejs.dev/config/#resolve-extensions
      extensions: [".mjs", ".js", ".ts", ".jsx", ".tsx", ".json", ".vue"],
    },
    // 打包配置
    build: {
      // https://vite.dev/config/build-options.html
      sourcemap: command === "build" ? false : "inline",
      outDir: "dist",
      assetsDir: "assets",
      chunkSizeWarningLimit: 2000,
      rollupOptions: {
        output: {
          chunkFileNames: "static/js/[name]-[hash].js",
          entryFileNames: "static/js/[name]-[hash].js",
          assetFileNames: "static/[ext]/[name]-[hash].[ext]",
        },
      },
      commonjsOptions: {
          include: /node_modules|lib/  //这里记得把lib目录加进来，否则生产打包会报错！！
      }
    },
    // vite 相关配置
    server: {
      port: 8081,
      host: true,
      open: true,
      proxy: {
        // https://cn.vitejs.dev/config/#server-proxy
        // "/dev-api": {
        //   target: 'http://192.168.1.202:8897',//202
        //     //  target: 'http://8.130.171.65:8897',//正式环境！！！
        //   // target: "http://8.130.155.88:8877", //测试
        //   changeOrigin: true,
        //   rewrite: (p) => p.replace(/^\/dev-api/, "/prod-api"),
        // },
        "/dev-api": {
          target: gatewayUrl,
          // target: 'http://192.168.1.135:9998',//萧
          // target: "http://192.168.1.132:9889", //军舰 
          // target: 'http://192.168.1.137:9998',//陈柏宇
          // target: 'http://192.168.1.155:9998',//张超
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api/, ""),
        },
        "/im-platform": {
          target: imPlatformUrl,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/im-platform/, ""),
        },
        "/im-server": {
          target: imServerUrl,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/im-server/, ""),
        },
        "/im-ws": {
          target: imSocketUrl,
          changeOrigin: true,
          ws: true,
          rewrite: (p) => p.replace(/^\/im-ws/, ""),
        },
        // springdoc proxy
        "^/v3/api-docs/(.*)": {
          target: gatewayUrl,
          changeOrigin: true,
        },
      },
    },
    css: {
      postcss: {
        plugins: [
          {
            postcssPlugin: "internal:charset-removal",
            AtRule: {
              charset: (atRule) => {
                if (atRule.name === "charset") {
                  atRule.remove();
                }
              },
            },
          },
        ],
      },
    },
  };
});
