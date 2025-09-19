import { generateFiles } from "fumadocs-openapi";
import { openapi } from "@/lib/openapi";

void generateFiles({
  input: openapi,
  output: "./content/docs/openapi/(generated)",

  // 1) 每个接口（method+path）生成一个独立 MDX 页面
  per: "operation",

  // 2) 生成的文件不再深藏在 tag 目录里，避免“点进去才看到”
  //    如果你更喜欢按路由分组，也可以用 'route'
  groupBy: "none",

  // 3) 生成一个索引页，列出所有接口卡片（标题里会含 方法 与 路径）
  index: {
    url: {
      // 你的文档真实访问前缀（按你的站点路由调整）
      baseUrl: "/docs/openapi/(generated)",
      // 生成文件所在磁盘目录（与 output 一致）
      contentDir: "./content/docs/openapi/(generated)",
    },
    items: [
      {
        // 索引页文件名，访问就是 /openapi/(generated)/
        path: "index.mdx",
        description: "All API endpoints",
      },
    ],
  },

  // 让接口描述体也写入 MDX，增强站内搜索可见性
  includeDescription: true,

  // 为索引卡片导航注入必须的 imports（官方建议）
  imports: [
    { names: ["source"], from: "@/lib/source" },
    { names: ["getPageTreePeers"], from: "fumadocs-core/server" },
  ],

  // 在文件头加上“自动生成”注释
  addGeneratedComment: true,
});
