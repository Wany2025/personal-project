# 校园二手交易平台 - 后端

基于Spring Boot 3.2.0开发的校园二手交易平台后端系统，为大学生提供便捷的闲置物品交易服务。

## 🚀 项目特性

- **用户管理**: 学生注册登录、个人信息管理、信用积分系统
- **商品管理**: 商品发布、编辑、上下架、浏览详情、搜索筛选
- **分类管理**: 预置6大商品分类（教材书籍、数码电子、生活用品等）
- **JWT认证**: 安全的用户身份验证和授权机制
- **统一响应**: 标准化的API响应格式
- **异常处理**: 全局异常捕获和处理
- **数据验证**: 完整的参数校验机制

## 📋 技术栈
- **框架**: Spring Boot 3.2.0
- **数据库**: MySQL 8.0
- **ORM**: MyBatis-Plus 3.5.5
- **认证**: JWT (jsonwebtoken)
- **工具库**: Hutool 5.8.23, Lombok
- **构建工具**: Maven 3.6+
- **JDK版本**: Java 17

## 🏗️ 项目结构

```
src/main/java/com/xiaou/
├── CampusMarketApplication.java      # 启动类
├── config/                          # 配置类
│   ├── CorsConfig.java             # 跨域配置
│   ├── JwtInterceptor.java         # JWT拦截器
│   ├── MybatisPlusConfig.java      # MyBatis-Plus配置
│   └── WebConfig.java              # Web配置
├── controller/                     # 控制器层
│   ├── UserController.java         # 用户接口
│   ├── CategoryController.java     # 分类接口
│   └── ProductController.java      # 商品接口
├── service/                        # 服务层
│   ├── impl/                       # 服务实现
│   ├── UserService.java            # 用户服务
│   ├── CategoryService.java        # 分类服务
│   └── ProductService.java         # 商品服务
├── mapper/                         # 数据访问层
│   ├── UserMapper.java             # 用户Mapper
│   ├── CategoryMapper.java         # 分类Mapper
│   └── ProductMapper.java          # 商品Mapper
├── entity/                         # 实体类
│   ├── User.java                   # 用户实体
│   ├── Category.java               # 分类实体
│   ├── Product.java                # 商品实体
│   ├── OrderInfo.java              # 订单实体
│   ├── ChatMessage.java            # 聊天消息实体
│   ├── Favorite.java               # 收藏实体
│   └── SystemConfig.java           # 系统配置实体
├── dto/                           # 数据传输对象
├── vo/                            # 视图对象
├── common/                        # 通用类
│   ├── Result.java                # 统一响应结果
│   └── PageResult.java            # 分页响应结果
├── exception/                     # 异常处理
│   ├── BusinessException.java     # 业务异常
│   └── GlobalExceptionHandler.java # 全局异常处理器
└── utils/                         # 工具类
    └── JwtUtil.java               # JWT工具类
```

## 🗄️ 数据库设计

项目包含7张核心表：

1. **user** - 用户表（存储用户基本信息、信用分等）
2. **category** - 商品分类表（预置6大分类）
3. **product** - 商品表（商品信息、状态、统计数据）
4. **order_info** - 订单表（交易订单、评价信息）
5. **chat_message** - 聊天消息表（买卖双方沟通、议价）
6. **favorite** - 收藏表（用户收藏的商品）
7. **system_config** - 系统配置表（系统参数配置）

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd 021-backend
   ```

2. **数据库配置**
   ```sql
   # 创建数据库
   CREATE DATABASE campus_market CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

   # 导入表结构和初始数据
   source src/main/resources/sql/init.sql
   ```

3. **修改配置**
   编辑 `src/main/resources/application.yml` 中的数据库连接信息：
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://localhost:3306/campus_market?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
       username: your_username
       password: your_password
   ```

4. **启动项目**
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. **访问接口**
   项目启动后，API访问地址：`http://localhost:8080/api`

## 📚 API文档

### 用户模块 `/user`

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/user/register` | 用户注册 |
| POST | `/user/login` | 用户登录 |
| GET | `/user/info` | 获取用户信息 |
| PUT | `/user/update` | 更新用户信息 |
| GET | `/user/profile/{id}` | 查看用户资料 |

### 分类模块 `/category`

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/category/list` | 获取分类列表 |

### 商品模块 `/product`

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/product/publish` | 发布商品 |
| PUT | `/product/update` | 更新商品 |
| DELETE | `/product/{id}` | 删除商品 |
| PUT | `/product/{id}/shelf` | 商品上下架 |
| GET | `/product/list` | 商品列表（支持搜索、筛选、排序） |
| GET | `/product/{id}` | 商品详情 |
| GET | `/product/my` | 我的发布 |

## 🔧 核心功能特性

### 用户认证
- JWT Token认证机制
- 登录状态自动续期
- 拦截器统一鉴权

### 商品管理
- 支持多图片上传
- 商品状态管理（在售/已售/下架）
- 浏览量统计
- 收藏数统计

### 搜索筛选
- 关键词搜索（标题、描述）
- 分类筛选
- 成色筛选
- 价格排序
- 时间排序
- 热门排序

### 数据验证
- 完整的参数校验
- 业务规则验证
- 异常统一处理

## 📝 待完成功能

- [ ] 订单管理模块
- [ ] 聊天功能模块
- [ ] 收藏功能模块
- [ ] 图片上传功能
- [ ] 信用分评价系统
- [ ] 管理后台接口

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 项目Issue: [GitHub Issues](https://github.com/your-username/021-backend/issues)
- 邮箱: your-email@example.com

---

**注意**: 这是一个校园二手交易平台的后端实现，适合作为毕业设计项目使用。如需在生产环境使用，请确保完善安全机制和性能优化。