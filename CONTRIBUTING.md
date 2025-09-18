- [前置工作](#前置工作)
- [启动后端服务](#启动后端服务)
  - [本地 IDEA](#本地-idea)
    - [配置环境变量](#配置环境变量)
    - [配置 IDEA 参数](#配置-idea-参数)
    - [配置 MySQL](#配置-mysql)
    - [配置 Redis](#配置-redis)
    - [配置 RabbitMQ](#配置-rabbitmq)
  - [Docker 环境](#docker-环境)
    - [配置环境变量](#配置环境变量-1)
    - [构建并启动镜像](#构建并启动镜像)
- [启动前端服务](#启动前端服务)
  - [配置环境变量](#配置环境变量-2)
  - [安装依赖和运行](#安装依赖和运行)
- [其他配置](#其他配置)
  - [配置第三方登录以GitHub为例](#配置第三方登录以GitHub为例)
  - [配置Resend邮箱服务](#配置Resend邮箱服务)
- [API文档](#api文档)
  - [OpenAPI文档](#openapi文档)
  - [部署时间线以及文档时效性](#部署时间线以及文档时效性)
  - [OpenAPI文档使用](#OpenAPI文档使用)
  - [OpenAPI文档应用场景](#OpenAPI文档应用场景)

## 前置工作

先克隆仓库：

```shell
git clone https://github.com/nagisa77/OpenIsle.git
cd OpenIsle
```

- 后端开发环境
  - JDK 17+
- 前端开发环境
  - Node.JS 20+

## 启动后端服务

启动后端服务有多种方式，选择一种即可。

> [!IMPORTANT]
> 仅想修改前端的朋友可不用部署后端服务。转到 [启动前端服务](#启动前端服务) 章节。

### 本地 IDEA

```shell
cd backend/
```

IDEA 打开 `backend/` 文件夹。

#### 配置环境变量

1. 生成环境变量文件

   ```shell
   cp open-isle.env.example open-isle.env
   ```

   `open-isle.env.example` 是环境变量模板，`open-isle.env` 才是真正读取的内容

2. 修改环境变量，留下需要的，比如你要开发 Google 登录业务，就需要谷歌相关的变量，数据库是一定要的

   ![环境变量](assets/contributing/backend_img_7.png)

3. 应用环境文件，选择刚刚的 `open-isle.env`

可以在 `open-isle.env` 按需填写个性化的配置，该文件不会被 Git 追踪。比如你想把服务跑在 `8082`（默认为 `8080`），那么直接改 `open-isle.env` 即可：

```ini
SERVER_PORT=8082
```

另一种方式是修改 `.properities` 文件（但不建议），位于 `src/main/application.properties`，该配置同样来源于 `open-isle.env`，但修改 `.properties` 文件会被 Git 追踪。

![配置数据库](assets/contributing/backend_img_5.png)

#### 配置 IDEA 参数

- 设置 JDK 版本为 java 17

- 设置 VM Option，最好运行在其他端口，非 `8080`，这里设置 `8081`
  若上面在环境变量中设置了端口，那这里就不需要再额外设置

  ```shell
  -Dserver.port=8081
  ```

![配置1](assets/contributing/backend_img_3.png)

![配置2](assets/contributing/backend_img_2.png)

#### 配置 MySQL

> [!TIP]
> 如果不知道怎么配置数据库可以参考 [Docker 环境](#docker-环境) 章节

1. 本机配置 MySQL 服务（网上很多教程，忽略）
   - 可以用 Laragon，自带 MySQL 包括 Nodejs，版本建议 `6.x`，`7` 以后需要 Lisence
   - [下载地址](https://github.com/leokhoa/laragon/releases)

2. 填写环境变量

   ![环境变量](assets/contributing/backend_img_6.png)

   ```ini
   MYSQL_URL=jdbc:mysql://<数据库地址>:<端口>/<数据库名>?useUnicode=yes&characterEncoding=UTF-8&useInformationSchema=true&useSSL=false&serverTimezone=UTC
   MYSQL_USER=<数据库用户名>
   MYSQL_PASSWORD=<数据库密码>
   ```

3. 执行 [`db/init/init_script.sql`](backend/src/main/resources/db/init/init_script.sql) 脚本，导入基本的数据
   管理员：**admin/123456**
   普通用户1：**user1/123456**
   普通用户2：**user2/123456**

   ![初始化脚本](assets/contributing/resources_img.png)

#### 配置 Redis

后端的登录态缓存、访问频控等都依赖 Redis，请确保本地有可用的 Redis 实例。

1. **启动 Redis 服务**（已有服务可跳过）

   ```bash
   docker run --name openisle-redis -p 6379:6379 -d redis:7-alpine
   ```

   该命令会在本机暴露 `6379` 端口。若你已有其他端口的 Redis，可以根据实际情况调整映射关系。

2. **在 `backend/open-isle.env` 中填写连接信息**

   ```ini
   REDIS_HOST=127.0.0.1
   REDIS_PORT=6379
   # 可选：若需要切换逻辑库，可新增此变量，默认使用 0 号库
   REDIS_DATABASE=0
   ```

   `application.properties` 中的默认值为 `localhost:6379`、数据库 `0`，如果你的环境恰好一致，也可以不额外填写；显式声明可以避免 IDE/运行时读取到意外配置。

3. **验证连接**

   ```bash
   redis-cli -h 127.0.0.1 -p 6379 ping
   ```

   启动后端后，日志中会出现 `Redis connection established ...`（来自 `RedisConnectionLogger`），说明已成功连通。

#### 配置 RabbitMQ

消息通知和 WebSocket 推送链路依赖 RabbitMQ。后端会自动声明交换机与队列，确保本地 RabbitMQ 可用即可。

1. **启动 RabbitMQ 服务**（推荐包含管理界面）

   ```bash
   docker run --name openisle-rabbitmq \
     -e RABBITMQ_DEFAULT_USER=openisle \
     -e RABBITMQ_DEFAULT_PASS=openisle \
     -p 5672:5672 -p 15672:15672 \
     -d rabbitmq:3.13-management
   ```

   管理界面位于 http://127.0.0.1:15672 ，可用于查看队列、交换机等资源。

2. **同步填写后端与 WebSocket 服务的环境变量**

   ```ini
   # backend/open-isle.env
   RABBITMQ_HOST=127.0.0.1
   RABBITMQ_PORT=5672
   RABBITMQ_USERNAME=openisle
   RABBITMQ_PASSWORD=openisle

   # 如果需要启动 websocket_service，也需要在 websocket_service.env 中保持一致
   ```

   如果沿用 RabbitMQ 默认的 `guest/guest`，可以不显式设置，Spring Boot 会回退到 `application.properties` 中的默认值 (`localhost:5672`、`guest/guest`、虚拟主机 `/`)。

3. **确认自动声明的资源**

   - 交换机：`openisle-exchange`
   - 旧版兼容队列：`notifications-queue`
   - 分片队列：`notifications-queue-0` ~ `notifications-queue-f`（共 16 个，对应路由键 `notifications.shard.0` ~ `notifications.shard.f`）
   - 队列持久化默认开启，来自 `rabbitmq.queue.durable=true`，如需仅在本地短暂测试，可在 `application.properties` 中调整该配置。

   启动后端时可在日志中看到 `=== 开始主动声明 RabbitMQ 组件 ===` 与后续的声明结果，也可以在管理界面中查看是否创建成功。

完成 Redis 与 RabbitMQ 配置后，即可继续启动后端服务。

![运行画面](assets/contributing/backend_img_4.png)

### Docker 环境

#### 配置环境变量

```shell
cd docker/
```

主要配置两个 `.env` 文件

- `backend/open-isle.env`：后端环境变量，配置同上，见 [配置环境变量](#配置环境变量)。
- `docker/.env`：Docker Compose 环境变量，主要配置 MySQL 相关
  ```shell
  cp .env.example .env
  ```

> [!TIP]
> 使用单独的 `.env` 文件是为了兼容线上环境或已启用 MySQL 服务的情况，如果只是想快速体验或者启动统一的环境，则推荐使用本方式。

在指定 `docker/.env` 后，`backend/open-isle.env` 中以下配置会被覆盖，这样就确保使用了同一份配置。

```ini
MYSQL_URL=
MYSQL_USER=
MYSQL_PASSWORD=
```

#### 构建并启动镜像

```shell
docker compose up -d
```

如果想了解启动过程发生了什么可以查看日志

```shell
docker compose logs
```

## 启动前端服务

> [!IMPORTANT]
> **⚠️ 环境要求：Node.js 版本最低 20.0.0（因为 Nuxt 框架要求）**

```shell
cd frontend_nuxt/
```

### 配置环境变量

前端可以依赖本机部署的后端，也可以直接调用线上的后端接口。

- 利用预发环境：**（⚠️ 强烈推荐只开发前端的朋友使用该环境）**

  ```shell
  cp .env.staging.example .env
  ```

- 利用生产环境

  ```shell
  cp .env.production.example .env
  ```

- 利用本地环境

  ```shell
  cp .env.dev.example .env
  ```

若依赖本机部署的后端，需要修改 `.env` 中的 `NUXT_PUBLIC_API_BASE_URL` 值与后端服务端口一致

### 安装依赖和运行

前端安装依赖并启动服务。

```shell
# 安装依赖
npm install --verbose

# 运行前端服务
npm run dev
```

如此一来，浏览器访问 http://127.0.0.1:3000 即可访问前端页面。

## 其他配置

### 配置第三方登录以GitHub为例

- 修改 `application.properties` 配置

  ![后端配置](assets/contributing/backend_img.png)

- 修改 `.env` 配置

  ![前端](assets/contributing/fontend_img.png)

- 配置第三方登录回调地址

  ![github配置](assets/contributing/github_img.png)

  ![github配置2](assets/contributing/github_img_2.png)

### 配置Resend邮箱服务

https://resend.com/emails 创建账号并登录

- `Domains` -> `Add Domain`
  ![image-20250906150459400](assets/contributing/image-20250906150459400.png)

- 填写域名
  ![image-20250906150541817](assets/contributing/image-20250906150541817.png)

- 等待一段时间后解析成功，创建 key
  `API Keys` -> `Create API Key`，输入名称，设置 `Permission` 为 `Sending access`
  **Key 只能查看一次，务必保存下来**
  ![image-20250906150811572](assets/contributing/image-20250906150811572.png)
  ![image-20250906150924975](assets/contributing/image-20250906150924975.png)
  ![image-20250906150944130](assets/contributing/image-20250906150944130.png)
- 修改 `.env` 配置中的 `RESEND_API_KEY` 和 `RESEND_FROM_EMAIL`
  `RESEND_FROM_EMAIL`： **noreply@域名**
  `RESEND_API_KEY`：**刚刚复制的 Key**
  ![image-20250906151218330](assets/contributing/image-20250906151218330.png)

## API文档

### OpenAPI文档
https://docs.open-isle.com

### 部署时间线以及文档时效性

我已经将API Docs的部署融合进本站CI & CD中，目前如下

- 每次合入main之后，都会构建预发环境 http://staging.open-isle.com/ ,现在文档是紧随其后进行部署，也就是说代码合入main之后，如果是新增后台接口，就可以立即通过OpenAPI文档页面进行查看和调试，但是如果想通过OpenAPI调试需要选择预发环境的
- 每日凌晨三点会构建并重新部署正式环境，届时当日合入main的新后台API也可以通过OpenAPI文档页面调试

![CleanShot 2025-09-10 at 12 .04.48@2x.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/168303009f4047ca828344957e911ff1.png)

👆如图是合入main之后构建预发+docs的情形，总大约耗时4分钟左右

### OpenAPI文档使用

- 预发环境/正式环境切换，可以通过如下位置切换API环境

![CleanShot 2025-09-10 at 12 .08.00@2x.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/f9fb7a0f020d4a0e94159d7820783224.png)

- API分两种，一种是需要鉴权（需登录后的token），另一种是直接访问，可以直接访问的GET请求，直接点击Send即可调试，如下👇，比如本站的推荐流rss: /api/rss: https://docs.open-isle.com/openapi/feed

![CleanShot 2025-09-10 at 12 .09.48@2x.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/2afb42e0c96340559dd42854905ca5fc.png)

- 需要登陆的API，比如关注，取消关注，发帖等，则需要提供token，目前在“API与调试”可获取自身token，可点击link看看👉 https://www.open-isle.com/about?tab=api

![CleanShot 2025-09-10 at 12 .11.07@2x.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/74033f1b9cc14f2fab3cbe3b7fe306d8.png)

copy完token之后，粘贴到Bear之后, 即可发送调试， 如下👇，大家亦可自行尝试：https://docs.open-isle.com/openapi/me

![CleanShot 2025-09-10 at 12 .13.00@2x.png](https://openisle-1307107697.cos.accelerate.myqcloud.com/dynamic_assert/63913fe2e70541a486651e35c723765e.png)

#### OpenAPI文档应用场景

- 方便大部分前端调试的需求，如果有只想做前端/客户端的同学参与本项目，该平台会大大提高效率
- 自动化：有自动化发帖/自动化操作的需求，亦可通过该平台实现或调试
- API文档: https://docs.open-isle.com/openapi
