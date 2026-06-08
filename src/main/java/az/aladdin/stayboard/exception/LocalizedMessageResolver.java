package az.aladdin.stayboard.exception;

import az.aladdin.stayboard.audit.AuditMessageSupport;
import az.aladdin.stayboard.config.LocaleConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocalizedMessageResolver {

    private static final String ENTITY_PREFIX = "entity.";
    private static final String FIELD_PREFIX = "field.";
    private static final String NOTE_PREFIX = "note.";
    private static final String AUDIT_FIELD_PREFIX = "audit.field.";

    private final MessageSource messageSource;

    public String resolve(String messageKey, Object[] args, Locale locale) {
        Object[] resolvedArgs = resolveArgs(args, locale);
        for (Locale candidate : fallbackLocales(locale)) {
            try {
                return messageSource.getMessage(messageKey, resolvedArgs, candidate);
            } catch (NoSuchMessageException ignored) {
                // try next locale
            }
        }
        return messageKey;
    }

    private Iterable<Locale> fallbackLocales(Locale locale) {
        if (locale == null || Locale.ROOT.equals(locale)) {
            return LocaleConfig.SUPPORTED_LOCALES;
        }
        if (Locale.ENGLISH.getLanguage().equals(locale.getLanguage())) {
            return List.of(locale, Locale.ENGLISH);
        }
        return List.of(locale, Locale.ENGLISH);
    }

    private Object[] resolveArgs(Object[] args, Locale locale) {
        if (args == null || args.length == 0) {
            return args;
        }
        Object[] out = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof String key && isResolvableKey(key)) {
                out[i] = resolveLabel(key, locale);
            } else if (arg instanceof LocalDate date) {
                out[i] = formatLocalDate(date, locale);
            } else if (arg instanceof LocalDateTime dateTime) {
                out[i] = formatLocalDateTime(dateTime, locale);
            } else {
                out[i] = AuditMessageSupport.formatArg(arg);
            }
        }
        return out;
    }

    private boolean isResolvableKey(String key) {
        return key.startsWith(ENTITY_PREFIX)
                || key.startsWith(FIELD_PREFIX)
                || key.startsWith(NOTE_PREFIX)
                || key.startsWith(AUDIT_FIELD_PREFIX);
    }

    private String resolveLabel(String key, Locale locale) {
        for (Locale candidate : fallbackLocales(locale)) {
            try {
                return messageSource.getMessage(key, null, candidate);
            } catch (NoSuchMessageException ignored) {
                // try next locale
            }
        }
        return key;
    }

    private String formatLocalDate(LocalDate date, Locale locale) {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                .withLocale(locale != null ? locale : Locale.ENGLISH)
                .format(date);
    }

    private String formatLocalDateTime(LocalDateTime dateTime, Locale locale) {
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withLocale(locale != null ? locale : Locale.ENGLISH)
                .format(dateTime);
    }
}
