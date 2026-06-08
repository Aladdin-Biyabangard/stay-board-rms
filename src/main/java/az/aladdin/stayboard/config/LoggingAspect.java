package az.aladdin.stayboard.config;

import az.aladdin.stayboard.annotation.NoFieldLogging;
import az.aladdin.stayboard.annotation.NoLogging;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private static final String MASKED_VALUE = "***";
    private static final Set<String> SENSITIVE_KEYWORDS = new HashSet<>(Arrays.asList(
            "password",
            "token",
            "secret",
            "authorization",
            "cookie",
            "otp",
            "cvv",
            "pin"
    ));

    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private final ObjectMapper objectMapper;

    @Around("execution(* az.aladdin.stayboard.service..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (joinPoint.getSignature().getDeclaringType().isAnnotationPresent(NoLogging.class)
                || joinPoint.getSignature().getDeclaringType().getMethod(
                        joinPoint.getSignature().getName(),
                        ((MethodSignature) joinPoint.getSignature()).getParameterTypes()
                ).isAnnotationPresent(NoLogging.class)) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String params = buildParameters(signature, args).toString();

        logger.info("ActionLog.{}.START - args={}", methodName, params);

        try {
            Object result = joinPoint.proceed();
            logger.info("ActionLog.{}.SUCCESS", methodName);
            return result;
        } catch (Exception ex) {
            logger.error("ActionLog.{}.FAILED - args={} - Error: {}",
                    methodName,
                    params,
                    ex.getMessage(),
                    ex);
            throw ex;
        }
    }

    private StringBuilder buildParameters(MethodSignature signature, Object[] args) {
        StringBuilder builder = new StringBuilder("{");

        Parameter[] parameters = signature.getMethod().getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];

            builder.append(param.getName())
                    .append(": ")
                    .append(isSensitiveParameterName(param.getName()) ? MASKED_VALUE : getObjectAsString(args[i]));

            if (i < parameters.length - 1) {
                builder.append(", ");
            }
        }

        builder.append("}");
        return builder;
    }

    private String getObjectAsString(Object obj) {
        Object maskedObject = maskObjectIfNeeded(obj);

        try {
            return objectMapper
                    .writeValueAsString(maskedObject)
                    .replace("\"", " ");
        } catch (JsonProcessingException e) {
            return String.valueOf(maskedObject);
        }
    }

    private Object maskObjectIfNeeded(Object obj) {
        if (obj == null) {
            return null;
        }

        Class<?> objectClass = obj.getClass();
        if (isFieldLoggingDisabled(objectClass)) {
            return buildRedactedValue(objectClass);
        }

        if (objectClass.isArray()) {
            int length = Array.getLength(obj);
            List<Object> maskedArray = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                maskedArray.add(maskObjectIfNeeded(Array.get(obj, i)));
            }
            return maskedArray;
        }

        if (obj instanceof Iterable<?> iterable) {
            List<Object> maskedItems = new ArrayList<>();
            for (Object item : iterable) {
                maskedItems.add(maskObjectIfNeeded(item));
            }
            return maskedItems;
        }

        if (obj instanceof Map<?, ?> map) {
            Map<Object, Object> maskedMap = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                Object key = entry.getKey();
                Object value = entry.getValue();
                if (key instanceof String keyString && isSensitiveParameterName(keyString)) {
                    maskedMap.put(key, MASKED_VALUE);
                } else {
                    maskedMap.put(key, maskObjectIfNeeded(value));
                }
            }
            return maskedMap;
        }

        return obj;
    }

    private boolean isFieldLoggingDisabled(Class<?> clazz) {
        return AnnotationUtils.findAnnotation(clazz, NoFieldLogging.class) != null;
    }

    private String buildRedactedValue(Class<?> clazz) {
        return clazz.getSimpleName() + "{fields redacted}";
    }

    private boolean isSensitiveParameterName(String name) {
        String normalizedName = name == null ? "" : name.toLowerCase(Locale.ROOT);
        for (String keyword : SENSITIVE_KEYWORDS) {
            if (normalizedName.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
