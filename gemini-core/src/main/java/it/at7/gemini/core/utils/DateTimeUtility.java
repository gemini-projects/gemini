package it.at7.gemini.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.*;
import java.time.format.DateTimeFormatter;

import static it.at7.gemini.core.utils.DateTimeUtility.Formatter.DATE_FORMATTER_INPUT_EXT;
import static java.time.format.DateTimeFormatter.*;

public class DateTimeUtility {
    private static Logger logger = LoggerFactory.getLogger(DateTimeUtility.class);

    public interface Formatter {
        DateTimeFormatter DATE_FORMATTER_INPUT_EXT = DateTimeFormatter.ofPattern("[yyyy-M-d][yyyy/M/d][d/M/yyyy]");
        DateTimeFormatter DATE_FORMATTER_OUTPUT = ISO_DATE;
        DateTimeFormatter TIME_FORMATTER_INPUT = ISO_TIME;
        DateTimeFormatter TIME_FORMATTER_OUTPUT = ISO_TIME;
        DateTimeFormatter DATETIME_FORMATTER_INPUT = ISO_DATE_TIME;
        DateTimeFormatter DATETIME_FORMATTER_OUTPUT = ISO_DATE_TIME;
    }

    public static LocalDateTime isoStringToLocalDateTime(String stValue) {
        try {
            OffsetDateTime offsetTime = OffsetDateTime.parse(stValue, ISO_DATE_TIME);
            return offsetTime.withOffsetSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } catch (DateTimeException e) {
            return LocalDateTime.parse(stValue, ISO_LOCAL_DATE_TIME);
        }
    }

    public static LocalDate isoStringToLocalDate(String stValue) {
        try {
            LocalDateTime localDateTime = isoStringToLocalDateTime(stValue);
            return localDateTime.toLocalDate();
        } catch (DateTimeException e) {
            try {
                return LocalDate.parse(stValue, ISO_DATE);
            } catch (DateTimeException e1) {
                return LocalDate.parse(stValue, DATE_FORMATTER_INPUT_EXT);
            }
        }
    }

    public static LocalTime isoStringToLocalTime(String stValue) {
        try {
            LocalDateTime localDateTime = isoStringToLocalDateTime(stValue);
            return localDateTime.toLocalTime();
        } catch (DateTimeException e) {
            try {
                return OffsetTime.parse(stValue, ISO_TIME).withOffsetSameInstant(ZoneOffset.UTC).toLocalTime();
            } catch (DateTimeException e1) {
                return LocalTime.parse(stValue, ISO_LOCAL_TIME);
            }
        }
    }

}
