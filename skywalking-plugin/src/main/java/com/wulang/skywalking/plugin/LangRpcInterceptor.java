package com.wulang.skywalking.plugin;

import java.lang.reflect.Method;

import com.wulang.lang.rpc.Context;
import com.wulang.lang.rpc.LangRpcFilter;
import org.apache.skywalking.apm.agent.core.context.ContextCarrier;
import org.apache.skywalking.apm.agent.core.context.tag.Tags;
import org.apache.skywalking.apm.agent.core.context.CarrierItem;
import org.apache.skywalking.apm.agent.core.context.ContextManager;
import org.apache.skywalking.apm.agent.core.context.trace.AbstractSpan;
import org.apache.skywalking.apm.agent.core.context.trace.SpanLayer;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.EnhancedInstance;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.InstanceMethodsAroundInterceptor;
import org.apache.skywalking.apm.agent.core.plugin.interceptor.enhance.MethodInterceptResult;
import org.apache.skywalking.apm.network.trace.component.OfficialComponent;

public class LangRpcInterceptor implements InstanceMethodsAroundInterceptor {
    @Override
    public void beforeMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                             MethodInterceptResult result) throws Throwable {
        Context context = (Context) allArguments[0];

        boolean isConsumer = LangRpcFilter.Clint.equals(context.identity);

        String serviceName = context.message;

        AbstractSpan span;

        String remoteAddress = "127.0.0.1:100001";
        if (isConsumer) {
            final ContextCarrier contextCarrier = new ContextCarrier();
            span = ContextManager.createExitSpan(serviceName, contextCarrier, remoteAddress);
            CarrierItem next = contextCarrier.items();
            while (next.hasNext()) {
                next = next.next();
                context.map.put(next.getHeadKey(), next.getHeadValue());
            }
        } else {
            ContextCarrier contextCarrier = new ContextCarrier();
            CarrierItem next = contextCarrier.items();
            while (next.hasNext()) {
                next = next.next();
                next.setHeadValue(context.map.get(next.getHeadKey()));
            }

            span = ContextManager.createEntrySpan(serviceName, contextCarrier);
        }

        Tags.URL.set(span, remoteAddress);
        span.setComponent(new OfficialComponent(2009, "LangRpc"));
        SpanLayer.asRPCFramework(span);
    }

    @Override
    public Object afterMethod(EnhancedInstance objInst, Method method, Object[] allArguments, Class<?>[] argumentsTypes,
                              Object ret) throws Throwable {
        Context result = (Context) ret;

        ContextManager.stopSpan();
        return ret;
    }

    @Override
    public void handleMethodException(EnhancedInstance objInst, Method method, Object[] allArguments,
                                      Class<?>[] argumentsTypes, Throwable t) {
        dealException(t);
    }

    /**
     * Log the throwable, which occurs in KKrpc RPC service.
     */
    private void dealException(Throwable throwable) {
        AbstractSpan span = ContextManager.activeSpan();
        span.errorOccurred();
        span.log(throwable);
    }
}
