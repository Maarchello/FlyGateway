package io.onhigh.os.flygateway.util;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public final class ReflectionUtils {

    public static Map<String, Object> getAnnotationAttributesValues(List<String> attributeNames, Class<?> annotation) {
        return Arrays.stream(annotation.getDeclaredFields())
                .filter(f -> attributeNames.contains(f.getName()))
                .collect(Collectors.toMap(Field::getName, f -> getFieldValueSilent(f, annotation)));
    }

    public static Optional<Object> getAnnotationAttributeValue(String attributeName, Class<?> annotation) {
        try {
            return Optional.of(annotation.getDeclaredField(attributeName))
                    .map(field -> getFieldValueSilent(field, annotation));
        } catch (NoSuchFieldException e) {
            log.error("Error during getting declared field, details:\n{}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public static Optional<Field> getDeclaredFieldSilent(String name, Class<?> clazz) {
        try {
            return Optional.of(clazz.getDeclaredField(name));
        } catch (NoSuchFieldException e) {
            log.error("Error during getting declared field, details:\n{}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    public static Optional<Object> getFieldValueSilent(Field field, Object object) {
        try {
            return Optional.of(field.get(object));
        } catch (IllegalAccessException e) {
            log.error("Error during getting declared field value, details:\n{}", e.getMessage(), e);
            return Optional.empty();
        }
    }

}
