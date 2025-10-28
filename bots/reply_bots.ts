// reply_bot.ts
import { Agent, Runner, hostedMcpTool, withTrace } from "@openai/agents";

console.log("✅ Reply bot starting...");

// ---- MCP 工具（Hosted MCP） ----
// 关键点：requireApproval 设为 "never"，避免卡在人工批准。
const mcp = hostedMcpTool({
  serverLabel: "openisle_mcp",
  serverUrl: "https://www.open-isle.com/mcp",
  allowedTools: [
    "search",
    "reply_to_post",
    "reply_to_comment",
    "recent_posts",
    "get_post",
    "list_unread_messages"
  ],
  requireApproval: "never",
});

type WorkflowInput = { input_as_text: string };

// 从环境变量读取你的站点鉴权令牌（可选）
const OPENISLE_TOKEN = process.env.OPENISLE_TOKEN ?? "";

// ---- 定义 Agent ----
const openisleBot = new Agent({
  name: "OpenIsle Bot",
  instructions: [
    "You are a helpful assistant for https://www.open-isle.com.",
    "Finish tasks end-to-end before replying. If multiple MCP tools are needed, call them sequentially until the task is truly done.",
    "When showing the result, reply in Chinese with a concise summary and include any important URLs or IDs.",
    OPENISLE_TOKEN
      ? `If tools require auth, use this token exactly where the tool schema expects it: ${OPENISLE_TOKEN}`
      : "If a tool requires auth, ask me to provide OPENISLE_TOKEN via env.",
  ].join("\n"),
  tools: [mcp],
  model: "gpt-4o",
  modelSettings: {
    temperature: 0.7,
    topP: 1,
    maxTokens: 2048,
    toolChoice: "auto",
    store: true,
  },
});

// ---- 入口函数：跑到拿到 finalOutput 为止，然后输出并退出 ----
export const runWorkflow = async (workflow: WorkflowInput) => {
  // 强烈建议在外部（shell）设置 OPENAI_API_KEY
  if (!process.env.OPENAI_API_KEY) {
    throw new Error("Missing OPENAI_API_KEY");
  }

  const runner = new Runner({
    workflowName: "OpenIsle Bot",
    traceMetadata: {
      __trace_source__: "agent-builder",
      workflow_id: "wf_69003cbd47e08190928745d3c806c0b50d1a01cfae052be8",
    },
    // 如需完全禁用上报可加：tracingDisabled: true
  });

  return await withTrace("OpenIsle Bot run", async () => {
    // Runner.run 会自动循环执行：LLM → 工具 → 直至 finalOutput
    const result = await runner.run(openisleBot, workflow.input_as_text, {
      maxTurns: 16, // 允许更复杂任务多轮调用 MCP
      // stream: true  // 如需边跑边看事件可打开，然后消费流事件
    });

    if (!result.finalOutput) {
      // 若没产出最终结果，通常是启用了人工批准/工具失败/达到 maxTurns
      throw new Error("Agent result is undefined (no final output).");
    }

    const openisleBotResult = { output_text: String(result.finalOutput) };

    console.log("🤖 Agent result:\n" + openisleBotResult.output_text);
    return openisleBotResult;
  });
};

// ---- CLI 运行（示范）----
if (require.main === module) {
  (async () => {
    try {
      const query =
        process.argv.slice(2).join(" ") || "你好，协助看看有什么未读消息，并结合帖子内容/评论内容予以回复";
      console.log("🔍 Running workflow...");
      await runWorkflow({ input_as_text: query });
      process.exit(0);
    } catch (err: any) {
      console.error("❌ Agent failed:", err?.stack || err);
      process.exit(1);
    }
  })();
}
