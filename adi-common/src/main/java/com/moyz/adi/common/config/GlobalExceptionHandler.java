package com.moyz.adi.common.config;

import com.moyz.adi.common.base.BaseResponse;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理参数校验异常。
     *
     * @param exception 参数校验异常
     * @return 统一响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    private BaseResponse handleMethodArgumentNotValidException(
            final MethodArgumentNotValidException exception) {
        Map<Object, Object> error = wrapperError(exception.getBindingResult());
        log.error("参数校验异常:{}", error);
        return new BaseResponse(ErrorEnum.A_PARAMS_ERROR.getCode(), ErrorEnum.A_PARAMS_ERROR.getInfo(), error);
    }

    /**
     * 处理业务异常。
     *
     * @param exception 业务异常
     * @return 统一响应
     */
    @ExceptionHandler(BaseException.class)
    private BaseResponse handleBaseException(final BaseException exception) {
        log.error("拦截业务异常:{}", exception);
        return new BaseResponse(exception.getCode(), exception.getInfo(), exception.getData());
    }

    /**
     * 处理未捕获异常。
     *
     * @param exception 未捕获异常
     * @return 统一响应
     */
    @ExceptionHandler(Exception.class)
    private BaseResponse handleException(final Exception exception) {
        log.error("拦截全局异常:", exception);
        return new BaseResponse(ErrorEnum.B_GLOBAL_ERROR.getCode(), ErrorEnum.B_GLOBAL_ERROR.getInfo(), exception.getMessage());
    }

    /**
     * 将校验错误封装为字段与错误信息映射。
     *
     * @param result 校验结果
     * @return 错误映射
     */
    private Map<Object, Object> wrapperError(BindingResult result) {
        Map<Object, Object> errorMap = new HashMap<>(5);
        result.getFieldErrors().forEach(x -> errorMap.put(x.getField(), x.getDefaultMessage()));
        return errorMap;
    }
}
