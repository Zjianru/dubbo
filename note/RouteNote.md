# dubbo 服务路由
服务目录在刷新 Invoker 列表的过程中，会通过 Router 进行服务路由，筛选出符合路由规则的服务提供者

## 关于服务路由
服务路由包含一条路由规则，路由规则决定了服务消费者的调用目标，即规定了服务消费者可调用哪些服务提供者。Dubbo 目前提供了三种服务路由实现，分别为条件路由 ConditionRouter、脚本路由 ScriptRouter 和标签路由 TagRouter

## 源码分析
条件路由规则由两个条件组成，分别用于对服务消费者和提供者进行匹配。比如有这样一条规则：

host = 10.20.153.10 => host = 10.20.153.11

该条规则表示 IP 为 10.20.153.10 的服务消费者只可调用 IP 为 10.20.153.11 机器上的服务，不可调用其他机器上的服务。条件路由规则的格式如下：

[服务消费者匹配条件] => [服务提供者匹配条件]

如果服务消费者匹配条件为空，表示不对服务消费者进行限制。如果服务提供者匹配条件为空，表示对某些服务消费者禁用服务。官方文档中对条件路由进行了比较详细的介绍，大家可以参考下，这里就不过多说明了。

条件路由实现类 ConditionRouter 在进行工作前，需要先对用户配置的路由规则进行解析，得到一系列的条件。然后再根据这些条件对服务进行路由。本章将分两节进行说明，2.1节介绍表达式解析过程。2.2 节介绍服务路由的过程

### 条件表达式解析
条件路由规则是一条字符串，对于 Dubbo 来说，它并不能直接理解字符串的意思，需要将其解析成内部格式才行。条件表达式的解析过程始于 ConditionRouter 的构造方法

条件路由 package --> `package org.apache.dubbo.rpc.cluster.router.condition`
标签路由 package --> `package org.apache.dubbo.rpc.cluster.router.tag`
脚本路由 package --> `package org.apache.dubbo.rpc.cluster.router.script`

关于条件是否匹配的处理 可参考条件路由的处理 `org.apache.dubbo.rpc.cluster.router.condition.ConditionStateRouter#doRoute()`
方法签名记录：`protected BitList<Invoker<T>> doRoute(BitList<Invoker<T>> invokers, URL url, Invocation invocation, boolean needToPrintMessage, Holder<RouterSnapshotNode<T>> nodeHolder, Holder<String> messageHolder)`
