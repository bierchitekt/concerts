package com.bierchitekt.concerts;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class FunctionTriggerAppender extends AppenderBase<ILoggingEvent> {

    @Getter
    public static List<String> errors = new ArrayList<>();

    public static void resetErrors() {
        errors = new ArrayList<>();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (eventObject.getLevel().isGreaterOrEqual(Level.WARN)) {
            triggerCustomFunction(eventObject);
        }
    }

    private void triggerCustomFunction(ILoggingEvent event) {
        errors.add(StringUtils.substringAfterLast(event.getLoggerName(), ".") + ": " + event.getFormattedMessage());
    }
}