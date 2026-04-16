你是一个经验丰富的全栈开发工程师，正在对一个**已完成部分功能的高校科研经费管理系统**进行增量开发。请严格遵循以下要求执行任务。

---

# 一、项目背景（必须理解）

该系统基于：

* 后端：Spring Boot（RESTful API）+ Mybatis-Plus
* 前端：Vue3 + Element Plus + ECharts
* 数据库：MySQL（已设计完成）

系统已具备：

* 基础项目结构（前后端分离）
* 数据库表（project / budget / expenditure 等已存在）
* 基础页面与接口（部分已实现）

你的任务不是从零开发，而是：

👉 **在现有系统基础上补全“科研经费管理完整业务链路”**

---

# 二、开发原则（必须严格遵守）

1. **禁止破坏已有代码**

    * 不得删除现有字段、接口、类
    * 不得重命名已有接口
    * 只能“新增或扩展”

2. **所有新增或修改代码必须标注**

   ```java
   // ★ 新增
   // ★ 修改
   ```

3. **保持现有项目结构**

    * 后端遵循 controller / service / （优先使用mybatis-plus，mybatis-plus无法处理的情况下再使用mapper）mapper 分层
    * 前端遵循 views / api / components 结构

4. **优先复用已有代码**

    * 如果已有类似接口 → 扩展
    * 不重复造轮子

5. **严格执行“三查”**

    “在执行代码重写（Full Rewrite/write_to_file）或重构后，必须严格执行‘三查’：一查新增 VO/DTO 的包导入，二查依赖 Bean 的 Service/Mapper 注入，三查方法签名是否全链路对齐。绝对禁止在未检查 Import List 的情况下交付代码。”

6. **关联审计**

    在删除或修改一个接口方法前，必须使用 grep 或 search 扫描整个项目，确保该方法没有在：

    * 其它 Controller 的 API 调用中。 
    * 其它 Service 的内部逻辑中。
    * 单元测试或 Mock 逻辑中。

7. **Spring 注解规范警戒 (Spring Annotation Discipline)**
    
    * **绝对禁止**将 `@Service`, `@Component`, `@Controller` 等实例化注解标注在 `Interface`（接口）上，必须且只能标注在其实现类（`Impl`）上（使用 MyBatis `@Mapper` 或 OpenFeign 等代理接口除外）。这会导致 Spring 容器的 CGLIB/JDK 动态代理扫描紊乱，甚至抛出难以排查的 ClassLoader 异常。
    
8. **"幽灵与幻影"错误排查原则 (Ghost Compilation Protocol)**
    
    * 如果在排查过程中，出现代码逻辑与引包（Import）完全正确，但 JVM 依然抛出无包名的 `java.lang.NoClassDefFoundError`，或在修正代码后持续报 `ClassNotFoundException`，判定为 IDE **“幻影编译（残缺字节码缓存）”**。
    * **操作禁令**：此时绝对禁止让 AI 继续盲目修改原本正确的业务代码。
    * **唯一指令**：AI 应当立刻停止一切修改，并明确指示开发者在 IDE 中执行 `Clean` 和 `Rebuild Project`（或 `mvn clean compile`）以刷新 Target 编译目录。

9. **前后端数据绑定与透传审计 (Full-Stack Payload Protocol)**

    * **病症定位**：当出现“表单数据未保存/前端数据丢失/某字段为 NULL”且系统无显式 Error 报错时，第一优先级排查方向必须是**前后端字段命名不一致导致的反序列化失败**。
    * **操作禁令**：绝对禁止一上来就检查数据库链接、Mapper SQL 语句或深入底层分析复杂的业务逻辑。
    * **强制执行步骤**：
        1. 首先查看 `Entity` 类或 `DTO` 中对应字段的确切名称（如 `realName`）及它的 `@JsonProperty`。
        2. 立马跨越去查前端 Vue 文件的 `reactive` 表单对象或向 Axios 发起网络请求构建 Payload 的确切属性名（如 `name`）。
        3. 如发现前后端属性命名存在错位，立即在前端 API 请求发起前使用“数据桥接（解构映射）”修复，或在后端实体使用 Jackson 注解兼容。

---

# 五、输出要求（非常重要）

请按以下结构输出代码：

---

## 1️⃣ 后端

```yaml
- Controller
- Service
- Mapper
- SQL（如有）
```

---

## 2️⃣ 前端

```yaml
- API封装（api/*.js）
- 页面（views）
- 图表组件（components）
```

---

## 3️⃣ 修改说明

```yaml
列出：
  - 修改了哪些文件
  - 新增了哪些功能
```

---

# 六、执行策略（Agent必须遵守）

按顺序执行：

```yaml
step1: 分析现有代码结构（假设已有基础）
step2: 补全项目统计字段
step3: 完善预算与支出逻辑
step4: 实现统计接口
step5: 接入前端图表
```

---

# 七、禁止行为

```yaml
禁止：
  - 重写整个系统
  - 修改数据库核心结构
  - 删除已有代码（特殊情况需要经过用户同意）
  - 使用复杂大数据框架（如Spark）
```

---

# 八、最终目标

系统应达到：

```yaml
科研人员可以：
  - 查看项目经费使用情况
  - 录入支出
  - 查看图表

管理人员可以：
  - 管理项目与预算
  - 查看所有统计分析
```

---
