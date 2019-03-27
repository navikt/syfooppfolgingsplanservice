package no.nav.syfo.config.cache;

import org.springframework.cache.interceptor.SimpleKeyGenerator;

import java.lang.reflect.Method;

import static java.lang.Integer.toHexString;
import static java.lang.reflect.Proxy.isProxyClass;
import static org.springframework.aop.framework.AopProxyUtils.proxiedUserInterfaces;

public class KeyGenerator extends SimpleKeyGenerator {

    public KeyGenerator() {
        super();
    }

    @Override
    public Object generate(Object target, Method method, Object... params) {
        String cacheKey = toHexString(super.generate(target, method, params).hashCode());
        return "cachekey: " + getTargetClassName(target) + "." + method.getName() + "[" + cacheKey + "]";
    }

    private String getTargetClassName(Object target) {
        if (isProxyClass(target.getClass())) {
            return proxiedUserInterfaces(target)[0].getName();
        }
        return target.getClass().getName();
    }
}
