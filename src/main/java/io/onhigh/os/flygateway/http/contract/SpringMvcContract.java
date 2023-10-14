package io.onhigh.os.flygateway.http.contract;

import feign.*;
import io.onhigh.os.flygateway.http.DateToStringGenericConverter;
import io.onhigh.os.flygateway.http.contract.impl.PathVariableParameterProcessor;
import io.onhigh.os.flygateway.http.contract.impl.RequestHeaderParameterProcessor;
import io.onhigh.os.flygateway.http.contract.impl.RequestParamParameterProcessor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;

public class SpringMvcContract extends Contract.BaseContract implements ResourceLoaderAware {

    private static final TypeDescriptor STRING_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(String.class);
    private static final TypeDescriptor ITERABLE_TYPE_DESCRIPTOR = TypeDescriptor.valueOf(Iterable.class);
    private static final ParameterNameDiscoverer PARAMETER_NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private final Map<Class<? extends Annotation>, AnnotatedParameterProcessor> annotatedArgumentProcessors;
    private final Map<String, Method> processedMethods;
    private final ConversionService conversionService;
    private final ConvertingExpanderFactory convertingExpanderFactory;
    private ResourceLoader resourceLoader;

    public SpringMvcContract() {
        List<AnnotatedParameterProcessor> processors = getDefaultAnnotatedArgumentsProcessors();
        this.processedMethods = new HashMap<>();
        this.resourceLoader = new DefaultResourceLoader();
        this.annotatedArgumentProcessors = toAnnotatedArgumentProcessorMap(processors);
        this.conversionService = getConversionService();
        this.convertingExpanderFactory = new ConvertingExpanderFactory(conversionService);
    }

    public SpringMvcContract(List<AnnotatedParameterProcessor> annotatedParameterProcessors) {
        this(annotatedParameterProcessors, getConversionService());
    }

    public SpringMvcContract(List<AnnotatedParameterProcessor> annotatedParameterProcessors, ConversionService conversionService) {
        this.processedMethods = new HashMap<>();
        this.resourceLoader = new DefaultResourceLoader();
        Assert.notNull(annotatedParameterProcessors, "Parameter processors can not be null.");
        Assert.notNull(conversionService, "ConversionService can not be null.");
        List<AnnotatedParameterProcessor> processors;
        if (!annotatedParameterProcessors.isEmpty()) {
            processors = new ArrayList<>(annotatedParameterProcessors);
        } else {
            processors = getDefaultAnnotatedArgumentsProcessors();
        }

        this.annotatedArgumentProcessors = toAnnotatedArgumentProcessorMap(processors);
        this.conversionService = conversionService;
        this.convertingExpanderFactory = new ConvertingExpanderFactory(conversionService);
    }

    private static ConversionService getConversionService() {
        ConfigurableConversionService conversionService = new DefaultConversionService();
        conversionService.addConverter(new DateToStringGenericConverter());
        return conversionService;
    }

    private static TypeDescriptor createTypeDescriptor(Method method, int paramIndex) {
        Parameter parameter = method.getParameters()[paramIndex];
        MethodParameter methodParameter = MethodParameter.forParameter(parameter);
        TypeDescriptor typeDescriptor = new TypeDescriptor(methodParameter);
        if (typeDescriptor.isAssignableTo(ITERABLE_TYPE_DESCRIPTOR)) {
            TypeDescriptor elementTypeDescriptor = getElementTypeDescriptor(typeDescriptor);
            Util.checkState(elementTypeDescriptor != null, "Could not resolve element type of Iterable type %s. Not declared?", new Object[]{typeDescriptor});
            typeDescriptor = elementTypeDescriptor;
        }

        return typeDescriptor;
    }

    private static TypeDescriptor getElementTypeDescriptor(TypeDescriptor typeDescriptor) {
        TypeDescriptor elementTypeDescriptor = typeDescriptor.getElementTypeDescriptor();
        if (elementTypeDescriptor == null && Iterable.class.isAssignableFrom(typeDescriptor.getType())) {
            ResolvableType type = typeDescriptor.getResolvableType().as(Iterable.class).getGeneric(0);
            return type.resolve() == null ? null : new TypeDescriptor(type, null, typeDescriptor.getAnnotations());
        } else {
            return elementTypeDescriptor;
        }
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    protected void processAnnotationOnClass(MethodMetadata data, Class<?> clz) {
        if (clz.getInterfaces().length == 0) {
            RequestMapping classAnnotation =  AnnotatedElementUtils.findMergedAnnotation(clz, RequestMapping.class);
            if (classAnnotation != null && classAnnotation.value().length > 0) {
                String pathValue = Util.emptyToNull(classAnnotation.value()[0]);
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
        RequestMapping classAnnotation = AnnotatedElementUtils.findMergedAnnotation(targetType, RequestMapping.class);
        if (classAnnotation != null) {
            if (!md.template().headers().containsKey("Accept")) {
                this.parseProduces(md, method, classAnnotation);
            }

            if (!md.template().headers().containsKey("Content-Type")) {
                this.parseConsumes(md, method, classAnnotation);
            }

            this.parseHeaders(md, method, classAnnotation);
        }

        return md;
    }

    @Override
    protected void processAnnotationOnMethod(MethodMetadata data, Annotation methodAnnotation, Method method) {
        if (methodAnnotation instanceof RequestMapping || methodAnnotation.annotationType().isAnnotationPresent(RequestMapping.class)) {
            RequestMapping methodMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            RequestMethod[] methods = methodMapping.method();
            if (methods.length == 0) {
                methods = new RequestMethod[]{RequestMethod.GET};
            }

            this.checkOne(method, methods, "method");
            data.template().method(Request.HttpMethod.valueOf(methods[0].name()));
            this.checkAtMostOne(method, methodMapping.value(), "value");
            if (methodMapping.value().length > 0) {
                String pathValue = Util.emptyToNull(methodMapping.value()[0]);
                if (pathValue != null) {
                    pathValue = this.resolve(pathValue);
                    if (!pathValue.startsWith("/") && !data.template().path().endsWith("/")) {
                        pathValue = "/" + pathValue;
                    }

                    data.template().uri(pathValue, true);
                }
            }

            this.parseProduces(data, method, methodMapping);
            this.parseConsumes(data, method, methodMapping);
            this.parseHeaders(data, method, methodMapping);
            data.indexToExpander(new LinkedHashMap<>());
        }
    }

    private String resolve(String value) {
        return StringUtils.hasText(value) && this.resourceLoader instanceof ConfigurableApplicationContext ? ((ConfigurableApplicationContext)this.resourceLoader).getEnvironment().resolvePlaceholders(value) : value;
    }

    private void checkAtMostOne(Method method, Object[] values, String fieldName) {
        Util.checkState(values != null && (values.length == 0 || values.length == 1), "Method %s can only contain at most 1 %s field. Found: %s", new Object[]{method.getName(), fieldName, values == null ? null : Arrays.asList(values)});
    }

    private void checkOne(Method method, Object[] values, String fieldName) {
        Util.checkState(values != null && values.length == 1, "Method %s can only contain 1 %s field. Found: %s", new Object[]{method.getName(), fieldName, values == null ? null : Arrays.asList(values)});
    }

    @Override
    protected boolean processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations, int paramIndex) {
        boolean isHttpAnnotation = false;
        AnnotatedParameterProcessor.AnnotatedParameterContext context = new SimpleAnnotatedParameterContext(data, paramIndex);
        Method method = this.processedMethods.get(data.configKey());

        for (Annotation parameterAnnotation : annotations) {
            AnnotatedParameterProcessor processor = this.annotatedArgumentProcessors.get(parameterAnnotation.annotationType());
            if (processor != null) {
                Annotation processParameterAnnotation = this.synthesizeWithMethodParameterNameAsFallbackValue(parameterAnnotation, method, paramIndex);
                isHttpAnnotation |= processor.processArgument(context, processParameterAnnotation, method);
            }
        }

        if (isHttpAnnotation && data.indexToExpander().get(paramIndex) == null) {
            TypeDescriptor typeDescriptor = createTypeDescriptor(method, paramIndex);
            if (this.conversionService.canConvert(typeDescriptor, STRING_TYPE_DESCRIPTOR)) {
                Param.Expander expander = this.convertingExpanderFactory.getExpander(typeDescriptor);
                if (expander != null) {
                    data.indexToExpander().put(paramIndex, expander);
                }
            }
        }

        return isHttpAnnotation;
    }

    private void parseProduces(MethodMetadata md, Method method, RequestMapping annotation) {
        String[] serverProduces = annotation.produces();
        String clientAccepts = serverProduces.length == 0 ? null : Util.emptyToNull(serverProduces[0]);
        if (clientAccepts != null) {
            md.template().header("Accept", clientAccepts);
        }

    }

    private void parseConsumes(MethodMetadata md, Method method, RequestMapping annotation) {
        String[] serverConsumes = annotation.consumes();
        String clientProduces = serverConsumes.length == 0 ? null : Util.emptyToNull(serverConsumes[0]);
        if (clientProduces != null) {
            md.template().header("Content-Type", clientProduces);
        }

    }

    private void parseHeaders(MethodMetadata md, Method method, RequestMapping annotation) {
        if (annotation.headers() != null && annotation.headers().length > 0) {
            String[] headers = annotation.headers();

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

    private Annotation synthesizeWithMethodParameterNameAsFallbackValue(Annotation parameterAnnotation, Method method, int parameterIndex) {
        Map<String, Object> annotationAttributes = AnnotationUtils.getAnnotationAttributes(parameterAnnotation);
        Object defaultValue = AnnotationUtils.getDefaultValue(parameterAnnotation);
        if (defaultValue instanceof String && defaultValue.equals(annotationAttributes.get("value"))) {
            Type[] parameterTypes = method.getGenericParameterTypes();
            String[] parameterNames = PARAMETER_NAME_DISCOVERER.getParameterNames(method);
            if (this.shouldAddParameterName(parameterIndex, parameterTypes, parameterNames)) {
                annotationAttributes.put("value", parameterNames[parameterIndex]);
            }
        }

        return AnnotationUtils.synthesizeAnnotation(annotationAttributes, parameterAnnotation.annotationType(), (AnnotatedElement)null);
    }

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
            SpringMvcContract.this.nameParam(this.methodMetadata, name, this.parameterIndex);
        }

        public Collection<String> setTemplateParameter(String name, Collection<String> rest) {
            Collection<String> params = Optional.ofNullable(rest).map(ArrayList::new).orElse(new ArrayList<>());
            params.add(String.format("{%s}", name));
            return params;
        }
    }

    private static class ConvertingExpanderFactory {
        private final ConversionService conversionService;

        ConvertingExpanderFactory(ConversionService conversionService) {
            this.conversionService = conversionService;
        }

        Param.Expander getExpander(TypeDescriptor typeDescriptor) {
            return (value) -> {
                Object converted = this.conversionService.convert(value, typeDescriptor, SpringMvcContract.STRING_TYPE_DESCRIPTOR);
                return (String)converted;
            };
        }
    }

    /** @deprecated */
    @Deprecated
    public static class ConvertingExpander implements Param.Expander {
        private final ConversionService conversionService;

        public ConvertingExpander(ConversionService conversionService) {
            this.conversionService = conversionService;
        }

        public String expand(Object value) {
            return this.conversionService.convert(value, String.class);
        }
    }

}
