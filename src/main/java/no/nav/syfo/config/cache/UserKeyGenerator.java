package no.nav.syfo.config.cache;

import java.lang.reflect.Method;

import static java.lang.Integer.toHexString;
import static no.nav.brukerdialog.security.context.SubjectHandler.getSubjectHandler;

public class UserKeyGenerator extends KeyGenerator {

    public UserKeyGenerator() {
        super();
    }

    @Override
    public Object generate(Object target, Method method, Object... params) {
        String cacheKey = toHexString(super.generate(target, method, params).hashCode());
        return "user: " + getUser() + "[" + cacheKey + "]";
    }

    private String getUser() {
        return getSubjectHandler().getUid();
    }

}

