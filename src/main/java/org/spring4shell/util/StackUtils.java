package org.spring4shell.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public final class StackUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(StackUtils.class);

    private StackUtils() {
    }

    public static void logCallerMethod() {
        final StackWalker walker = StackWalker.getInstance();
        final Optional<StackWalker.StackFrame> callerStackFrame = walker.walk(frames -> frames.skip(1).findFirst());

        final String className = callerStackFrame.map(StackWalker.StackFrame::getClassName).orElse("UNKNOWN");
        final String methodName = callerStackFrame.map(StackWalker.StackFrame::getMethodName).orElse("UNKNOWN");

        LOGGER.info("Method call: {} from {}", methodName, className);
    }

}
