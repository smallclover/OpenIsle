// reply_bot.ts
import { Agent, Runner, hostedMcpTool, withTrace } from "@openai/agents";

console.log("✅ Reply bot starting...");

const allowedMcpTools = [
  "search",
  "reply_to_post",
  "reply_to_comment",
  "recent_posts",
  "get_post",
  "list_unread_messages",
  "mark_notifications_read",
];

console.log("🛠️ Configured Hosted MCP tools:", allowedMcpTools.join(", "));

// ---- MCP 工具（Hosted MCP） ----
// 关键点：requireApproval 设为 "never"，避免卡在人工批准。
const mcp = hostedMcpTool({
  serverLabel: "openisle_mcp",
  serverUrl: "https://www.open-isle.com/mcp",
  allowedTools: allowedMcpTools,
  requireApproval: "never",
});

type WorkflowInput = { input_as_text: string };

// 从环境变量读取你的站点鉴权令牌（可选）
const OPENISLE_TOKEN = process.env.OPENISLE_TOKEN ?? "";

console.log(
  OPENISLE_TOKEN
    ? "🔑 OPENISLE_TOKEN detected in environment."
    : "🔓 OPENISLE_TOKEN not set; agent will request it if required."
);

// ---- 定义 Agent ----
const openisleBot = new Agent({
  name: "OpenIsle Bot",
  instructions: [
    "You are a helpful and cute assistant for https://www.open-isle.com. Please use plenty of kawaii kaomoji (颜表情), such as (๑˃ᴗ˂)ﻭ, (•̀ω•́)✧, (｡•ᴗ-)_♡, (⁎⁍̴̛ᴗ⁍̴̛⁎), etc., in your replies to create a friendly, adorable vibe.",
    "Finish tasks end-to-end before replying. If multiple MCP tools are needed, call them sequentially until the task is truly done.",
    "When presenting the result, reply in Chinese with a concise, cute summary filled with kaomoji and include any important URLs or IDs.",
    OPENISLE_TOKEN
      ? `If tools require auth, use this token exactly where the tool schema expects it: ${OPENISLE_TOKEN}`
      : "If a tool requires auth, ask me to provide OPENISLE_TOKEN via env.",
    "After finishing replies, call mark_notifications_read with all processed notification IDs to keep the inbox clean.",
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
    const preview = workflow.input_as_text.trim();
    console.log(
      "📝 Received workflow input (preview):",
      preview.length > 200 ? `${preview.slice(0, 200)}…` : preview
    );

    // Runner.run 会自动循环执行：LLM → 工具 → 直至 finalOutput
    console.log("🚦 Starting agent run with maxTurns=16...");
    const result = await runner.run(openisleBot, workflow.input_as_text, {
      maxTurns: 16, // 允许更复杂任务多轮调用 MCP
      // stream: true  // 如需边跑边看事件可打开，然后消费流事件
    });

    console.log("📬 Agent run completed. Result keys:", Object.keys(result));

    if (!result.finalOutput) {
      // 若没产出最终结果，通常是启用了人工批准/工具失败/达到 maxTurns
      throw new Error("Agent result is undefined (no final output).");
    }

    const openisleBotResult = { output_text: String(result.finalOutput) };

    console.log(
      "🤖 Agent result (length=%d):\n%s",
      openisleBotResult.output_text.length,
      openisleBotResult.output_text
    );
    return openisleBotResult;
  });
};

// ---- CLI 运行（示范）----
if (require.main === module) {
  (async () => {
    try {
      const query = `
        【AUTO】无需确认，自动处理所有未读的提及与评论：
        1）调用 list_unread_messages；
        2）依次处理每条“提及/评论”：如需上下文则使用 get_post 获取，生成简明中文回复；如有 commentId 则用 reply_to_comment，否则用 reply_to_post；
        3）跳过关注和系统事件；
        4）保证幂等性：如该贴最后一条是你自己发的回复，则跳过；
        5）调用 mark_notifications_read，传入本次已处理的通知 ID 清理已读；
        6）最多只处理最新10条；结束时仅输出简要摘要（包含URL或ID）。
      `;
        
      console.log("🔍 Running workflow...");
      await runWorkflow({ input_as_text: query });
      process.exit(0);
    } catch (err: any) {
      console.error("❌ Agent failed:", err?.stack || err);
      process.exit(1);
    }
  })();
}
