package io.onhigh.os.flygateway.http.contract;

import feign.MethodMetadata;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;

public interface AnnotatedParameterProcessor {

    Class<? extends Annotation> getAnnotationType();

    boolean processArgument(AnnotatedParameterContext context, Annotation annotation, Method method);

    interface AnnotatedParameterContext {
        MethodMetadata getMethodMetadata();

        int getParameterIndex();

        void setParameterName(String name);

        Collection<String> setTemplateParameter(String name, Collection<String> rest);
    }
}
