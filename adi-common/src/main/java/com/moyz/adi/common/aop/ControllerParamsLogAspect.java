package com.moyz.adi.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 控制器请求参数日志切面。
 *
 * @author moyz
 * date:2021-07-15 03:16:59
 */
@Aspect
@Component
public class ControllerParamsLogAspect {

    /**
     * 日志记录器。
     */
    private static final Logger logger = LoggerFactory.getLogger(ControllerParamsLogAspect.class);

    /**
     * 控制器方法切点。
     */
    @Pointcut("execution(public * com.adi.*.controller..*.*(..))")
    public void controllerMethods() {
    }

    /**
     * 方法执行前记录请求参数。
     *
     * @param joinPoint 连接点
     */
    @Before("controllerMethods()")
    public void before(JoinPoint joinPoint) {
        ParamsLogAspect.paramsLog(joinPoint, logger);
    }

}
