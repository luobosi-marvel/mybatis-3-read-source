/**
 * Copyright 2009-2018 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ibatis.logging;

import java.lang.reflect.Constructor;

/**
 * @author Clinton Begin
 * @author Eduardo Macarron
 */
public final class LogFactory {

    /**
     * Marker to be used by logging implementations that support markers
     */
    public static final String MARKER = "MYBATIS";

    private static Constructor<? extends Log> logConstructor;

    /**
     * 这里只会加载一个日志类
     * 加载的顺序：SLF4J->Apache Commons Logging->Log4J->JDK logging->No Logging
     * 一旦加载到某个日志，就不会继续加载了，下面加载的方法都应该是静态同步的。
     */
    static {
        tryImplementation(LogFactory::useSlf4jLogging);
        tryImplementation(LogFactory::useCommonsLogging);
        tryImplementation(LogFactory::useLog4J2Logging);
        tryImplementation(LogFactory::useLog4JLogging);
        tryImplementation(LogFactory::useJdkLogging);
        tryImplementation(LogFactory::useNoLogging);
    }

    private LogFactory() {
        // disable construction
    }

    /**
     * 根据传入的 log 名称来获取 Log 具体实现的 日志类型
     *
     * @param aClass
     * @return
     */
    public static Log getLog(Class<?> aClass) {
        return getLog(aClass.getName());
    }

    public static Log getLog(String logger) {
        try {
            return logConstructor.newInstance(logger);
        } catch (Throwable t) {
            throw new LogException("Error creating logger for logger " + logger + ".  Cause: " + t, t);
        }
    }

    /**
     * 这里可以使用用户自己实现的 Log 类型, 这里用户可以自定义日志模块加载进来
     *
     * @param clazz
     */
    public static synchronized void useCustomLogging(Class<? extends Log> clazz) {
        setImplementation(clazz);
    }

    public static synchronized void useSlf4jLogging() {
        setImplementation(org.apache.ibatis.logging.slf4j.Slf4jImpl.class);
    }

    public static synchronized void useCommonsLogging() {
        setImplementation(org.apache.ibatis.logging.commons.JakartaCommonsLoggingImpl.class);
    }

    public static synchronized void useLog4JLogging() {
        setImplementation(org.apache.ibatis.logging.log4j.Log4jImpl.class);
    }

    public static synchronized void useLog4J2Logging() {
        setImplementation(org.apache.ibatis.logging.log4j2.Log4j2Impl.class);
    }

    public static synchronized void useJdkLogging() {
        setImplementation(org.apache.ibatis.logging.jdk14.Jdk14LoggingImpl.class);
    }

    public static synchronized void useStdOutLogging() {
        setImplementation(org.apache.ibatis.logging.stdout.StdOutImpl.class);
    }

    public static synchronized void useNoLogging() {
        setImplementation(org.apache.ibatis.logging.nologging.NoLoggingImpl.class);
    }

    private static void tryImplementation(Runnable runnable) {
        if (logConstructor == null) {
            try {
                runnable.run();
            } catch (Throwable t) {
                // ignore
            }
        }
    }

    private static void setImplementation(Class<? extends Log> implClass) {
        try {
            Constructor<? extends Log> candidate = implClass.getConstructor(String.class);
            Log log = candidate.newInstance(LogFactory.class.getName());
            if (log.isDebugEnabled()) {
                log.debug("Logging initialized using '" + implClass + "' adapter.");
            }
            logConstructor = candidate;
        } catch (Throwable t) {
            throw new LogException("Error setting Log implementation.  Cause: " + t, t);
        }
    }

}
