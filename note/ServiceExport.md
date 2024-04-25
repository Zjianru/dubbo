# 服务导出

Dubbo 服务导出过程始于 Spring 容器发布刷新事件

Dubbo 在接收到事件后，会立即执行服务导出逻辑。整个逻辑大致可分为三个部分
1. 前置工作，主要用于检查参数，组装 URL
2. 导出服务，包含导出服务到本地 (JVM)，和导出服务到远程两个过程
3. 向注册中心注册服务，用于服务发现

## 源码分析

#### 服务暴露

> 官方文档管这个叫服务导出,个人感觉服务暴露更适合


服务暴露的入口方法是 `org.apache.dubbo.config.ServiceConfigBase.export()`方法

实际的实现则是在`org.apache.dubbo.config.ServiceConfig.export(RegisterTypeEnum registerType)`方法

在 Dubbo 中，URL 的作用十分重要。Dubbo 使用 URL 作为配置载体，所有的拓展点都是通过 URL 获取配置。这一点，官方文档中有所说明。

> 采用 URL 作为配置信息的统一格式，所有扩展点都通过传递 URL 携带配置信息

#### 服务注册

服务注册的接口为 `register(URL)`，这个方法定义在 `FailbackRegistry` 抽象类中

整个过程可简单总结为：先创建注册中心实例，之后再通过注册中心实例注册服务
