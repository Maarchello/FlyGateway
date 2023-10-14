package io.onhigh.os.flygateway;

import io.onhigh.os.flygateway.http.HttpGateway;
import io.onhigh.os.flygateway.http.HttpGatewayAnnotationProcessor;
import io.onhigh.os.flygateway.mq.MQGateway;
import io.onhigh.os.flygateway.mq.MQGatewayAnnotationProcessor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Marat Kadzhaev
 * @since 17 февр. 2022
 */
@Slf4j
@Setter
public class EnableApiGatewaysRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

    private static final List<Class<? extends Annotation>> DEFAULT_GATEWAYS_TYPES_FILTER = List.of(
            HttpGateway.class,
            MQGateway.class
    );

    private Map<GatewayType, GatewayAnnotationProcessor> DEFAULT_GATEWAY_REGISTRARS;

    private Environment environment;
    private ResourceLoader resourceLoader;


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        DEFAULT_GATEWAY_REGISTRARS = Map.of(
                GatewayType.HTTP, new HttpGatewayAnnotationProcessor(environment),
                GatewayType.MQ, new MQGatewayAnnotationProcessor()
        );
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        ClassPathScanningCandidateComponentProvider scanner = getScanner();
        scanner.setResourceLoader(this.resourceLoader);

        Map<String, Object> enableApiAttributes = metadata.getAnnotationAttributes(EnableApiGateways.class.getCanonicalName());
        Class<? extends Annotation>[] includeGateways = (Class<? extends Annotation>[]) enableApiAttributes.get("include");

        Stream.concat(DEFAULT_GATEWAYS_TYPES_FILTER.stream(), Arrays.stream(includeGateways))
                .map(AnnotationTypeFilter::new)
                .forEach(scanner::addIncludeFilter);

        Set<String> basePackages = getBasePackages(metadata, enableApiAttributes, includeGateways);
        for (String basePackage : basePackages) {

            Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
            for (BeanDefinition candidateComponent : candidateComponents) {
                if (candidateComponent instanceof AnnotatedBeanDefinition) {
                    // verify annotated class is an interface
                    // todo: убрать эту проверку, если классы все таки понадобятся
                    AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
                    AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
                    Assert.isTrue(annotationMetadata.isInterface(), "ApiGateway can only be an interface");

                    Class<? extends Annotation> gatewayAnnotation = Stream.concat(Arrays.stream(includeGateways), DEFAULT_GATEWAYS_TYPES_FILTER.stream())
                            .filter(gateway -> annotationMetadata.isAnnotated(gateway.getCanonicalName()))
                            .findFirst()
                            .orElse(null);

                    if (gatewayAnnotation == null) {
                        continue;
                    }

                    registerApiGateways(registry, annotationMetadata, annotationMetadata.getAnnotationAttributes(gatewayAnnotation.getCanonicalName()));
                }
            }
        }
    }

    private void registerApiGateways(BeanDefinitionRegistry registry, AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {

        boolean isHttpGateway = annotationMetadata.getAnnotations().isPresent(HttpGateway.class);
        boolean isMQGateway = annotationMetadata.getAnnotations().isPresent(MQGateway.class);

        if (isHttpGateway && isMQGateway) {
            throw new GatewayConfigurerException("The @HttpGateway and @MQGateway annotations cannot be used together.");
        }

        if (isHttpGateway) {
            DEFAULT_GATEWAY_REGISTRARS.get(GatewayType.HTTP).process(registry, annotationMetadata, attributes);
        } else if (isMQGateway) {
            DEFAULT_GATEWAY_REGISTRARS.get(GatewayType.MQ).process(registry, annotationMetadata, attributes);
        }

    }


    protected ClassPathScanningCandidateComponentProvider getScanner() {
        return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                boolean isCandidate = false;
                if (beanDefinition.getMetadata().isIndependent()) {
                    if (!beanDefinition.getMetadata().isAnnotation()) {
                        isCandidate = true;
                    }
                }
                return isCandidate;
            }
        };
    }

    protected Set<String> getBasePackages(AnnotationMetadata metadata, Map<String, Object> attributes, Class<? extends Annotation>[] includeGateways) {
        Set<String> basePackages = new HashSet<>();
        for (String pkg : (String[]) attributes.get("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }

        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(metadata.getClassName()));
        }

        for (Class<? extends Annotation> includeGateway : includeGateways) {
            try {
                Method registrarMethod = includeGateway.getDeclaredMethod("registrar");
                GatewayRegistrar gatewayRegistrar = getGatewayRegistrar((Class<? extends GatewayRegistrar>) registrarMethod.getDefaultValue());
                basePackages.addAll(gatewayRegistrar.getBasePackages());
            } catch (NoSuchMethodException e) {
                throw new GatewayConfigurerException("'registrar' attribute is required for " + includeGateway);
            } catch (Exception e) {
                throw new GatewayConfigurerException(e.getMessage());
            }
        }

        return basePackages;
    }

    private GatewayRegistrar getGatewayRegistrar(Class<? extends GatewayRegistrar> registrar) {
        Constructor<?> declaredConstructor = registrar.getDeclaredConstructors()[0];
        GatewayRegistrar gatewayRegistrar;
        try {
            if (declaredConstructor.getParameterCount() > 0) {
                gatewayRegistrar = registrar.getDeclaredConstructor(Environment.class).newInstance(environment);
            } else {
                gatewayRegistrar = registrar.getDeclaredConstructor().newInstance();
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new GatewayConfigurerException(e.getMessage());
        }

        return gatewayRegistrar;
    }

}
