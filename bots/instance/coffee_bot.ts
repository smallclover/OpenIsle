import { BotFather, WorkflowInput } from "../bot_father";

const WEEKDAY_NAMES = ["日", "一", "二", "三", "四", "五", "六"] as const;

class CoffeeBot extends BotFather {
  constructor() {
    super("Coffee Bot");
  }

  protected override getAdditionalInstructions(): string[] {
    return [
      "You are responsible for 发布每日抽奖早安贴。",
      "创建帖子时，确保标题、奖品信息、开奖时间以及领奖方式完全符合 CLI 查询提供的细节。",
      "正文需亲切友好，简洁明了，鼓励社区成员互动。",
      "开奖说明需明确告知中奖者需私聊站长 @nagisa 领取奖励。",
      "确保只发布一个帖子，避免重复调用 create_post。",
    ];
  }

  protected override getCliQuery(): string {
    const now = new Date(Date.now() + 8 * 60 * 60 * 1000);
    const weekday = WEEKDAY_NAMES[now.getDay()];
    const drawTime = new Date(now);
    drawTime.setHours(23, 0, 0, 0);

    return `
请立即在 https://www.open-isle.com 使用 create_post 发表一篇全新帖子，遵循以下要求：
1. 标题固定为「大家星期${weekday}早安--抽一杯咖啡」。
2. 正文包含：
   - 亲切的早安问候；
   - 明确奖品写作“Coffee”；
   - 帖子类型必须为 LOTTERY；
   - 奖品图片链接：https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/0d6a9b33e9ca4fe5a90540187d3f9ecb.png；
   - 公布开奖时间为 ${drawTime}, 直接传UTC时间给接口，不要考虑时区问题
   - 标注“领奖请私聊站长 @[nagisa]”；
   - 鼓励大家留言互动。
3. 调用 create_post 时 categoryId 固定为 10，tagIds 设为 [36]。
4. 帖子语言使用简体中文。
5. 完成后只输出“已发布咖啡抽奖贴”，不额外生成总结。
`.trim();
  }
}

const coffeeBot = new CoffeeBot();

export const runWorkflow = async (workflow: WorkflowInput) => {
  return coffeeBot.runWorkflow(workflow);
};

if (require.main === module) {
  coffeeBot.runCli();
}

