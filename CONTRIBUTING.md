- [前置工作](#前置工作)
- [启动后端服务](#启动后端服务)
  - [本地 IDEA](#本地-idea)
    - [配置环境变量](#配置环境变量)
    - [配置 IDEA 参数](#配置-idea-参数)
    - [配置 MySQL](#配置-mysql)
  - [Docker 环境](#docker-环境)
    - [配置环境变量](#配置环境变量-1)
    - [构建并启动镜像](#构建并启动镜像)
- [启动前端服务](#启动前端服务)
  - [配置环境变量](#配置环境变量-2)
  - [安装依赖和运行](#安装依赖和运行)
- [其他配置](#其他配置)

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

填写环境变量 `.env` 中的 Redis 相关配置并启动 Redis

```ini
REDIS_HOST=<Redis 地址>
REDIS_PORT=<Redis 端口>
```

处理完环境问题直接跑起来就能通了

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

### 配置第三方登录，这里以 GitHub 为例：

- 修改 `application.properties` 配置

  ![后端配置](assets/contributing/backend_img.png)

- 修改 `.env` 配置

  ![前端](assets/contributing/fontend_img.png)

- 配置第三方登录回调地址

  ![github配置](assets/contributing/github_img.png)

  ![github配置2](assets/contributing/github_img_2.png)

### 配置 Resend 邮箱服务

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

## 开源共建和API文档

- API文档: https://docs.open-isle.com/openapi
