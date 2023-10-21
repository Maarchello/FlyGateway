//package io.onhigh.os.flygateway.http;
//
//import org.springframework.core.convert.TypeDescriptor;
//import org.springframework.core.convert.converter.GenericConverter;
//import org.springframework.format.annotation.DateTimeFormat;
//
//import java.time.OffsetDateTime;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
///**
// * Makes possible usage of parameter of type {@link Date} in gateways.
// * <br>Parameters annotated by {@link org.springframework.format.annotation.DateTimeFormat} with field {@code iso} set
// * formatted with
// * <br>- {@link org.springframework.format.annotation.DateTimeFormat.ISO#DATE_TIME}: {@link #SPRING_ISO_DATE_TIME_PATTERN} pattern
// * <br>- {@link org.springframework.format.annotation.DateTimeFormat.ISO#DATE}: {@link DateTimeFormatter#ISO_DATE} formatter
// * <br>In all other cases parameters formatted with {@link DateTimeFormatter#ISO_DATE_TIME} formatter
// */
//public class DateToStringGenericConverter implements GenericConverter {
//
//    /**
//     * Corresponds to org.springframework.format.datetime.DateFormatter.ISO_PATTERNS[ISO.DATE_TIME]
//     */
//    private static final String SPRING_ISO_DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
//
//    @Override
//    public Set<ConvertiblePair> getConvertibleTypes() {
//        return Collections.singleton(new ConvertiblePair(Date.class, String.class));
//    }
//
//    @Override
//    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
//        if (null == source) return null;
//
//        OffsetDateTime odt = OffsetDateTime.ofInstant(((Date) source).toInstant(), ZoneId.systemDefault());
//        DateTimeFormat.ISO iso = Optional.ofNullable(sourceType.getAnnotation(DateTimeFormat.class))
//                .map(DateTimeFormat::iso)
//                .orElse(DateTimeFormat.ISO.NONE);
//
//        DateTimeFormatter formatter;
//        if (iso == DateTimeFormat.ISO.DATE_TIME) {
//            // Usage of java.time.format.DateTimeFormatter.ISO_DATE_TIME leads to string w/o "fraction-of-second"
//            //+ that in controller spring can't deserialize using its own "ISO_DATE_TIME" pattern
//            formatter = DateTimeFormatter.ofPattern(SPRING_ISO_DATE_TIME_PATTERN, Locale.getDefault());
//        } else if (iso == DateTimeFormat.ISO.DATE) {
//            formatter = DateTimeFormatter.ISO_DATE;
//        } else {
//            formatter = DateTimeFormatter.ISO_DATE_TIME;
//        }
//
//        return formatter.format(odt);
//    }
//}
