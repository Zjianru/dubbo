/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.rpc;

import org.apache.dubbo.common.extension.ExtensionScope;
import org.apache.dubbo.common.extension.SPI;

/**
 * Extension for intercepting the invocation for both service provider and consumer, furthermore, most of functions in
 * dubbo are implemented base on the same mechanism. Since every time when remote method is invoked, the filter
 * extensions will be executed too, the corresponding penalty should be considered before
 * <p>
 * 用于拦截服务提供者和使用者的调用的扩展，此外，dubbo中的大多数功能都是基于相同的机制实现的。由于每次调用远程方法时，也将执行过滤器扩展，因此在添加更多过滤器之前应考虑相应的惩罚。
 * <pre>
 *  They way filter work from sequence point of view is
 *  从顺序的角度来看，他们的过滤工作方式是
 *
 *    <b>
 *    ...code before filter ...
 *          invoker.invoke(invocation) //filter work in a filter implementation class
 *    ...code after filter ...
 *    </b>
 *    Caching is implemented in dubbo using filter approach. If cache is configured for invocation then before
 *    remote call configured caching type's (e.g. Thread Local, JCache etc) implementation invoke method gets called.
 * <p>
 *    在 dubbo 中使用过滤器方法实现缓存。如果缓存被配置为调用，那么在远程调用之前配置的缓存类型 (例如 Thread Local，JCache 等) 实现调用方法会被调用。
 * </pre>
 * <p>
 * Starting from 3.0, Filter on consumer side has been refactored. There are two different kinds of Filters working at
 * different stages of an RPC request. 1. Filter. Works at the instance level, each Filter is bond to one specific
 * Provider instance(invoker). 2. ClusterFilter. Newly introduced in 3.0, intercepts request before Loadbalancer picks
 * one specific Filter(Invoker).
 * <p>
 * 从3.0开始，消费者端的过滤器已经被重构。有两种不同类型的过滤器在RPC请求的不同阶段工作。
 * <p>
 * 1.过滤器。在实例级别工作，每个过滤器绑定到一个特定的提供者实例 (调用程序)
 * <p>
 * 2. ClusterFilter.在3.0中新引入的，在Loadbalancer选择一个特定的过滤器 (调用程序) 之前拦截请求
 * <p>
 * Filter Chain in 3.x
 * <p>
 * -> Filter -> Invoker
 * <p>
 * Proxy -> ClusterFilter -> ClusterInvoker -> Filter -> Invoker
 * <p>
 * -> Filter -> Invoker
 * <p>
 * <p>
 * Filter Chain in 2.x
 * <p>
 * Filter -> Invoker
 * <p>
 * Proxy -> ClusterInvoker -> Filter -> Invoker
 * <p>
 * Filter -> Invoker
 * <p>
 * <p>
 * Filter. (SPI, Singleton, ThreadSafe)
 *
 * @see org.apache.dubbo.rpc.filter.GenericFilter
 * @see org.apache.dubbo.rpc.filter.EchoFilter
 * @see org.apache.dubbo.rpc.filter.TokenFilter
 * @see org.apache.dubbo.rpc.filter.TpsLimitFilter
 */
@SPI(scope = ExtensionScope.MODULE)
public interface Filter extends BaseFilter {}
