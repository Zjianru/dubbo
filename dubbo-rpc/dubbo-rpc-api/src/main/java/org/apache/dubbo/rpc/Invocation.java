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

import org.apache.dubbo.common.Experimental;
import org.apache.dubbo.rpc.model.ModuleModel;
import org.apache.dubbo.rpc.model.ScopeModelUtil;
import org.apache.dubbo.rpc.model.ServiceModel;

import java.beans.Transient;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Invocation. (API, Prototype, NonThreadSafe) 持有调用过程中的变量，比如方法名，参数等
 *
 * @serial Don't change the class name and package name.
 * @see org.apache.dubbo.rpc.Invoker#invoke(Invocation)
 * @see org.apache.dubbo.rpc.RpcInvocation
 */
public interface Invocation {

    String getTargetServiceUniqueName();

    String getProtocolServiceKey();

    /**
     * get method name.
     *
     * @return method name.
     * @serial
     */
    String getMethodName();

    /**
     * get the interface name
     *
     * @return
     */
    String getServiceName();

    /**
     * get parameter types.
     *
     * @return parameter types.
     * @serial
     */
    Class<?>[] getParameterTypes();

    /**
     * get parameter's signature, string representation of parameter types.获取参数的签名，参数类型的字符串表示形式。
     *
     * @return parameter's signature
     */
    default String[] getCompatibleParamSignatures() {
        return Stream.of(getParameterTypes())
                .map(Class::getName)
                .toArray(String[]::new);
    }

    /**
     * get arguments.
     *
     * @return arguments.
     * @serial
     */
    Object[] getArguments();

    /**
     * get attachments.
     *
     * @return attachments.
     * @serial
     */
    Map<String, String> getAttachments();

    @Experimental("Experiment api for supporting Object transmission")
    Map<String, Object> getObjectAttachments();

    @Experimental("Experiment api for supporting Object transmission")
    Map<String, Object> copyObjectAttachments();

    @Experimental("Experiment api for supporting Object transmission")
    void foreachAttachment(Consumer<Map.Entry<String, Object>> consumer);

    void setAttachment(String key, String value);

    @Experimental("Experiment api for supporting Object transmission")
    void setAttachment(String key, Object value);

    @Experimental("Experiment api for supporting Object transmission")
    void setObjectAttachment(String key, Object value);

    void setAttachmentIfAbsent(String key, String value);

    @Experimental("Experiment api for supporting Object transmission")
    void setAttachmentIfAbsent(String key, Object value);

    @Experimental("Experiment api for supporting Object transmission")
    void setObjectAttachmentIfAbsent(String key, Object value);

    /**
     * get attachment by key.
     *
     * @return attachment value.
     * @serial
     */
    String getAttachment(String key);

    @Experimental("Experiment api for supporting Object transmission")
    Object getObjectAttachment(String key);

    @Experimental("Experiment api for supporting Object transmission")
    default Object getObjectAttachmentWithoutConvert(String key) {
        return getObjectAttachment(key);
    }

    /**
     * get attachment by key with default value.
     *
     * @return attachment value.
     * @serial
     */
    String getAttachment(String key, String defaultValue);

    @Experimental("Experiment api for supporting Object transmission")
    Object getObjectAttachment(String key, Object defaultValue);

    /**
     * get the invoker in current context.
     *
     * @return invoker.
     * @transient
     */
    @Transient
    Invoker<?> getInvoker();

    void setServiceModel(ServiceModel serviceModel);

    ServiceModel getServiceModel();

    default ModuleModel getModuleModel() {
        return ScopeModelUtil.getModuleModel(getServiceModel() == null ? null : getServiceModel().getModuleModel());
    }

    Object put(Object key, Object value);

    Object get(Object key);

    Map<Object, Object> getAttributes();

    /**
     * To add invoked invokers into invocation. Can be used in ClusterFilter or Filter for tracing or debugging purpose.
     * Currently, only support in consumer side. 将被调用的调用程序添加到调用中。可在ClusterFilter或Filter中用于跟踪或调试目的 目前，仅在消费者端支持。
     *
     * @param invoker invoked invokers
     */
    void addInvokedInvoker(Invoker<?> invoker);

    /**
     * Get all invoked invokers in current invocation. NOTICE: A curtain invoker could be invoked for twice or more if
     * retries. 获取当前调用中的所有 invoker。 注意: 如果重试，窗口 invoker 可能会调用多次
     *
     * @return invokers
     */
    List<Invoker<?>> getInvokedInvokers();
}
