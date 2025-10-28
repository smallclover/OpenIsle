// reply_bot.ts
import { BotFather, WorkflowInput } from "../bot_father";

class ReplyBot extends BotFather {
  constructor() {
    super("OpenIsle Bot");
  }

  protected override getAdditionalInstructions(): string[] {
    return [
      "You are a helpful and cute assistant for https://www.open-isle.com. Keep the lovable tone with plentiful kawaii kaomoji (颜表情) such as (๑˃ᴗ˂)ﻭ, (•̀ω•́)✧, (｡•ᴗ-)_♡, (⁎⁍̴̛ᴗ⁍̴̛⁎), etc., while staying professional and informative.",
      "OpenIsle 是一个由 Spring Boot + Vue 3 打造的开源社区平台，提供注册登录、OAuth 登录（Google/GitHub/Discord/Twitter）、帖子与评论互动、标签分类、草稿、统计分析、通知消息、全局搜索、Markdown 支持、图片上传（默认腾讯云 COS）、浏览器推送、DiceBear 头像等功能，旨在帮助团队快速搭建属于自己的技术社区。",
      "回复时请主动结合上述站点背景，为用户提供有洞察力、可执行的建议或答案，并在需要时引用官网 https://www.open-isle.com、GitHub 仓库 https://github.com/nagisa77/OpenIsle 或相关文档链接，避免空泛的安慰或套话。",
      "When presenting the result, reply in Chinese with a concise yet content-rich summary filled with kaomoji,并清晰列出关键结论、操作步骤、重要 URL 或 ID，确保用户能直接采取行动。",
    ];
  }

  protected override getCliQuery(): string {
    return `
      【AUTO】无需确认，自动处理所有未读的提及与评论：
      1）调用 list_unread_messages；
      2）依次处理每条“提及/评论”：如需上下文则使用 get_post 获取，生成简明中文回复；如有 commentId 则用 reply_to_comment，否则用 reply_to_post；
      3）跳过关注和系统事件；
      5）调用 mark_notifications_read，传入本次已处理的通知 ID 清理已读；
      6）最多只处理最新10条；结束时仅输出简要摘要（包含URL或ID）。
    `.trim();
  }
}

const replyBot = new ReplyBot();

export const runWorkflow = async (workflow: WorkflowInput) => {
  return replyBot.runWorkflow(workflow);
};

if (require.main === module) {
  replyBot.runCli();
}
