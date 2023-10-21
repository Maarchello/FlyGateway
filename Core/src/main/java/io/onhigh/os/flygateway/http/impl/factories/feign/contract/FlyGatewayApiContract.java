package io.onhigh.os.flygateway.http.impl.factories.feign.contract;

import feign.Contract;
import feign.Feign;
import feign.MethodMetadata;
import feign.Util;
import io.onhigh.os.flygateway.FlyEnvironment;
import io.onhigh.os.flygateway.http.api.*;
import io.onhigh.os.flygateway.http.impl.factories.feign.contract.annotationprocessor.PathVariableParameterProcessor;
import io.onhigh.os.flygateway.http.impl.factories.feign.contract.annotationprocessor.RequestHeaderParameterProcessor;
import io.onhigh.os.flygateway.http.impl.factories.feign.contract.annotationprocessor.RequestParamParameterProcessor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

public class FlyGatewayApiContract extends Contract.BaseContract {

    private final Map<Class<? extends Annotation>, AnnotatedParameterProcessor> annotatedArgumentProcessors;
    private final Map<String, Method> processedMethods;
    private FlyEnvironment environment;

    public FlyGatewayApiContract(FlyEnvironment environment) {
        List<AnnotatedParameterProcessor> processors = getDefaultAnnotatedArgumentsProcessors();
        this.environment = environment;
        this.processedMethods = new HashMap<>();
        this.annotatedArgumentProcessors = toAnnotatedArgumentProcessorMap(processors);
    }

    public FlyGatewayApiContract(List<AnnotatedParameterProcessor> annotatedParameterProcessors) {
        this.processedMethods = new HashMap<>();
//        Assert.notNull(annotatedParameterProcessors, "Parameter processors can not be null.");
//        Assert.notNull(conversionService, "ConversionService can not be null.");
        List<AnnotatedParameterProcessor> processors;
        if (!annotatedParameterProcessors.isEmpty()) {
            processors = new ArrayList<>(annotatedParameterProcessors);
        } else {
            processors = getDefaultAnnotatedArgumentsProcessors();
        }

        this.annotatedArgumentProcessors = toAnnotatedArgumentProcessorMap(processors);
    }

    @Override
    protected void processAnnotationOnClass(MethodMetadata data, Class<?> clz) {
        if (clz.getInterfaces().length == 0) {

            Request classAnnotation = clz.getAnnotation(Request.class);
            if (classAnnotation != null && !classAnnotation.value().isBlank()) {
                String pathValue = Util.emptyToNull(classAnnotation.value());
                pathValue = this.resolve(pathValue);
                if (!pathValue.startsWith("/")) {
                    pathValue = "/" + pathValue;
                }

                data.template().uri(pathValue);
            }
        }

    }

    @Override
    public MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {
        this.processedMethods.put(Feign.configKey(targetType, method), method);
        MethodMetadata md = super.parseAndValidateMetadata(targetType, method);
        Request classAnnotation = targetType.getAnnotation(Request.class);
        if (classAnnotation != null) {
            if (!md.template().headers().containsKey("Accept")) {
                this.parseProduces(md, method, classAnnotation.produces());
            }

            if (!md.template().headers().containsKey("Content-Type")) {
                this.parseConsumes(md, method, classAnnotation.consumes());
            }

            this.parseHeaders(md, method, classAnnotation.headers());
        }

        return md;
    }

    @Override
    protected void processAnnotationOnMethod(MethodMetadata data, Annotation methodAnnotation, Method method) {
        if (methodAnnotation.annotationType().isAnnotationPresent(Request.class)) {
            Request requestMapping = methodAnnotation.annotationType().getAnnotation(Request.class);
//            method.getAnnotation(methodAnnotation.annotationType());
//            Request methodMapping =
//            method.getann
            RequestMethod[] methods = requestMapping.method();
            if (methods.length == 0) {
                methods = new RequestMethod[]{RequestMethod.GET};
            }

//            this.checkOne(method, methods, "method");
            RequestMethod requestMethod = methods[0];
            data.template().method(feign.Request.HttpMethod.valueOf(requestMethod.name()));
//            this.checkAtMostOne(method, methodMapping.value(), "value");
//            methodAnnotation.annotationType()

            String requestValue;
            String[] produces;
            String[] consumes;
            String[] headers;
            switch (requestMethod) {

                case GET:
                    GetRequest getRequest = method.getAnnotation(GetRequest.class);
                    requestValue = getRequest.value();
                    produces = getRequest.produces();
                    consumes = getRequest.consumes();
                    headers = getRequest.headers();
                    break;

                case POST:
                    PostRequest postRequest = method.getAnnotation(PostRequest.class);
                    requestValue = postRequest.value();
                    produces = postRequest.produces();
                    consumes = postRequest.consumes();
                    headers = postRequest.headers();
                    break;

                case PUT:
                    PutRequest putRequest = method.getAnnotation(PutRequest.class);
                    requestValue = putRequest.value();
                    produces = putRequest.produces();
                    consumes = putRequest.consumes();
                    headers = putRequest.headers();
                    break;

                case DELETE:
                    DeleteRequest deleteRequest = method.getAnnotation(DeleteRequest.class);
                    requestValue = deleteRequest.value();
                    produces = deleteRequest.produces();
                    consumes = deleteRequest.consumes();
                    headers = deleteRequest.headers();
                    break;

                default:
                    throw new UnsupportedOperationException();

            }
            if (requestValue.length() > 0) {
                String pathValue = Util.emptyToNull(requestValue);
                if (pathValue != null) {
                    pathValue = this.resolve(pathValue);
                    if (!pathValue.startsWith("/") && !data.template().path().endsWith("/")) {
                        pathValue = "/" + pathValue;
                    }

                    data.template().uri(pathValue, true);
                }
            }

            this.parseProduces(data, method, produces);
            this.parseConsumes(data, method, consumes);
            this.parseHeaders(data, method, headers);
            data.indexToExpander(new LinkedHashMap<>());
        }
    }

    private String resolve(String value) {
        return value;
    }

//    private void checkAtMostOne(Method method, Object values, String fieldName) {
//        Util.checkState(values != null && (values.length == 0 || values.length == 1), "Method %s can only contain at most 1 %s field. Found: %s", new Object[]{method.getName(), fieldName, values == null ? null : Arrays.asList(values)});
//    }
//
//    private void checkOne(Method method, Object values, String fieldName) {
//        Util.checkState(values != null && values.length == 1, "Method %s can only contain 1 %s field. Found: %s", new Object[]{method.getName(), fieldName, values == null ? null : Arrays.asList(values)});
//    }

    @Override
    protected boolean processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations, int paramIndex) {
        boolean isHttpAnnotation = false;
        AnnotatedParameterProcessor.AnnotatedParameterContext context = new SimpleAnnotatedParameterContext(data, paramIndex);
        Method method = this.processedMethods.get(data.configKey());

        for (Annotation parameterAnnotation : annotations) {
            AnnotatedParameterProcessor processor = this.annotatedArgumentProcessors.get(parameterAnnotation.annotationType());
            if (processor != null) {
//                Annotation processParameterAnnotation = this.synthesizeWithMethodParameterNameAsFallbackValue(parameterAnnotation, method, paramIndex);
                isHttpAnnotation |= processor.processArgument(context, parameterAnnotation, method);
            }
        }

//        if (isHttpAnnotation && data.indexToExpander().get(paramIndex) == null) {
//            TypeDescriptor typeDescriptor = createTypeDescriptor(method, paramIndex);
//            if (this.conversionService.canConvert(typeDescriptor, STRING_TYPE_DESCRIPTOR)) {
//                Param.Expander expander = this.convertingExpanderFactory.getExpander(typeDescriptor);
//                if (expander != null) {
//                    data.indexToExpander().put(paramIndex, expander);
//                }
//            }
//        }

        return isHttpAnnotation;
    }

    private void parseProduces(MethodMetadata md, Method method, String[] produces) {
        String clientAccepts = produces.length == 0 ? null : Util.emptyToNull(produces[0]);
        if (clientAccepts != null) {
            md.template().header("Accept", clientAccepts);
        }

    }

    private void parseConsumes(MethodMetadata md, Method method, String[] consumes) {
        String clientProduces = consumes.length == 0 ? null : Util.emptyToNull(consumes[0]);
        if (clientProduces != null) {
            md.template().header("Content-Type", clientProduces);
        }

    }

    private void parseHeaders(MethodMetadata md, Method method, String[] headers) {
        if (headers != null) {

            for (String header : headers) {
                int index = header.indexOf(61);
                if (!header.contains("!=") && index >= 0) {
                    md.template().header(this.resolve(header.substring(0, index)), this.resolve(header.substring(index + 1).trim()));
                }
            }
        }

    }

    private Map<Class<? extends Annotation>, AnnotatedParameterProcessor> toAnnotatedArgumentProcessorMap(List<AnnotatedParameterProcessor> processors) {
        Map<Class<? extends Annotation>, AnnotatedParameterProcessor> result = new HashMap<>();

        for (AnnotatedParameterProcessor processor : processors) {
            result.put(processor.getAnnotationType(), processor);
        }

        return result;
    }

    private List<AnnotatedParameterProcessor> getDefaultAnnotatedArgumentsProcessors() {
        List<AnnotatedParameterProcessor> annotatedArgumentResolvers = new ArrayList<>();
        annotatedArgumentResolvers.add(new PathVariableParameterProcessor());
        annotatedArgumentResolvers.add(new RequestParamParameterProcessor());
        annotatedArgumentResolvers.add(new RequestHeaderParameterProcessor());
        return annotatedArgumentResolvers;
    }

//    private Annotation synthesizeWithMethodParameterNameAsFallbackValue(Annotation parameterAnnotation, Method method, int parameterIndex) {
//        Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(parameterAnnotation);
//        Object defaultValue = AnnotationUtils.getDefaultValue(parameterAnnotation);
//        if (defaultValue instanceof String && defaultValue.equals(annotationAttributes.get("value"))) {
//            Type[] parameterTypes = method.getGenericParameterTypes();
//            String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
//            if (this.shouldAddParameterName(parameterIndex, parameterTypes, parameterNames)) {
//                annotationAttributes.put("value", parameterNames[parameterIndex]);
//            }
//        }
//
//        return AnnotationUtils.synthesizeAnnotation(annotationAttributes, parameterAnnotation.annotationType(), (AnnotatedElement)null);
//    }

    private boolean shouldAddParameterName(int parameterIndex, Type[] parameterTypes, String[] parameterNames) {
        return parameterNames != null && parameterNames.length > parameterIndex && parameterTypes != null && parameterTypes.length > parameterIndex;
    }

    private class SimpleAnnotatedParameterContext implements AnnotatedParameterProcessor.AnnotatedParameterContext {
        private final MethodMetadata methodMetadata;
        private final int parameterIndex;

        SimpleAnnotatedParameterContext(MethodMetadata methodMetadata, int parameterIndex) {
            this.methodMetadata = methodMetadata;
            this.parameterIndex = parameterIndex;
        }

        public MethodMetadata getMethodMetadata() {
            return this.methodMetadata;
        }

        public int getParameterIndex() {
            return this.parameterIndex;
        }

        public void setParameterName(String name) {
            io.onhigh.os.flygateway.http.impl.factories.feign.contract.FlyGatewayApiContract.this.nameParam(this.methodMetadata, name, this.parameterIndex);
        }

        public Collection<String> setTemplateParameter(String name, Collection<String> rest) {
            Collection<String> params = Optional.ofNullable(rest).map(ArrayList::new).orElse(new ArrayList<>());
            params.add(String.format("{%s}", name));
            return params;
        }
    }


//    /** @deprecated */
//    @Deprecated
//    public static class ConvertingExpander implements Param.Expander {
//        private final ConversionService conversionService;
//
//        public ConvertingExpander(ConversionService conversionService) {
//            this.conversionService = conversionService;
//        }
//
//        public String expand(Object value) {
//            return this.conversionService.convert(value, String.class);
//        }
//    }

}
