package io.onhigh.os.flygateway.http.impl.factories.feign.contract.annotationprocessor;

import feign.MethodMetadata;
import io.onhigh.os.flygateway.http.api.RequestParam;
import io.onhigh.os.flygateway.http.impl.factories.feign.contract.AnnotatedParameterProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import static feign.Util.checkState;
import static feign.Util.emptyToNull;

public class RequestParamParameterProcessor implements AnnotatedParameterProcessor {
    private static final Class<RequestParam> ANNOTATION = RequestParam.class;

    public RequestParamParameterProcessor() {
    }

    public Class<? extends Annotation> getAnnotationType() {
        return ANNOTATION;
    }

    public boolean processArgument(AnnotatedParameterContext context, Annotation annotation, Method method) {
        int parameterIndex = context.getParameterIndex();
        Class<?> parameterType = method.getParameterTypes()[parameterIndex];
        MethodMetadata data = context.getMethodMetadata();

        if (Map.class.isAssignableFrom(parameterType)) {
            checkState(data.queryMapIndex() == null, "Query map can only be present once.");
            data.queryMapIndex(parameterIndex);

            return true;
        }

        RequestParam requestParam = ANNOTATION.cast(annotation);
        String name = requestParam.value();
        checkState(emptyToNull(name) != null,
                "RequestParam.value() was empty on parameter %s", parameterIndex);
        context.setParameterName(name);

        Collection<String> query = context.setTemplateParameter(name, data.template().queries().get(name));
        data.template().query(name, query);
        return true;
    }
}