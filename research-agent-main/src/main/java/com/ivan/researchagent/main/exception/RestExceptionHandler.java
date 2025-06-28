package com.ivan.researchagent.main.exception;

import com.ivan.researchagent.common.exception.BizException;
import com.ivan.researchagent.common.exception.ErrorEnum;
import com.ivan.researchagent.common.model.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Copyright (c) 2024 research-agent.
 * All Rights Reserved.
 *
 * @version 1.0
 * @description:
 * @author: ivan
 * @since: 2025/6/28/周六
 **/
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    /**
     * 将validator的校验json返回，进行简化
     *
     * @param request http请求
     * @param ex      异常
     * @return 包含异常的响应体
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(HttpServletRequest request, MethodArgumentNotValidException ex) {

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(HttpStatus.BAD_REQUEST.value());
        errorResponse.setTimestamp(new Date());
        errorResponse.setException(ex.getClass().getName());
        errorResponse.setPath(request.getServletPath());

        BindingResult result = ex.getBindingResult();
        List<ObjectError> objectErrorList = result.getAllErrors();
        ObjectError objectError = objectErrorList.get(0);

        errorResponse.setMessage(objectError.getDefaultMessage());

        ResponseEntity<ErrorResponse> responseEntity = ResponseEntity.ok().body(errorResponse);

        log.warn("ExceptionHandler::handle ex : ", ex);

        return responseEntity;
    }


    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(HttpServletRequest request, ConstraintViolationException ex) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(HttpStatus.BAD_REQUEST.value());
        errorResponse.setTimestamp(new Date());
        errorResponse.setException(ex.getClass().getName());
        errorResponse.setPath(request.getServletPath());

        Set<ConstraintViolation<?>> errors = ex.getConstraintViolations();
        Object error = errors.toArray()[0];
//        if (error instanceof ConstraintViolationImpl) {
//            errorResponse.setMessage(((ConstraintViolationImpl) error).getMessage());
//        }
        errorResponse.setMessage(error.toString());

        ResponseEntity<ErrorResponse> responseEntity = ResponseEntity.ok().body(errorResponse);

        log.warn("ExceptionHandler::handle ex : ", ex);

        return responseEntity;
    }


    /**
     * 将系统抛出对异常按照简化对hibernate格式进行处理
     *
     * @param request http请求
     * @param ex      异常
     * @return 包含异常的响应体
     */
    @ResponseBody
    @ExceptionHandler(value = BizException.class)
    @ResponseStatus(value = HttpStatus.OK)
    public ErrorResponse handleBizExceptionHandler(HttpServletRequest request, BizException ex) {

        if (AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class) != null) {
            throw ex;
        }

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(ex.getErrorInfo().getCode());
        errorResponse.setMessage(ex.getMessage());
        errorResponse.setTimestamp(new Date());
        errorResponse.setException(ex.getClass().getName());
        errorResponse.setPath(request.getServletPath());

        log.warn("ExceptionHandler::handle ex : ", ex);

        return errorResponse;
    }

    /**
     * 全局异常捕获
     *
     * @param request http请求
     * @param ex      异常
     * @return 包含异常的响应体
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(value = HttpStatus.OK)
    public ResponseEntity<ErrorResponse> globalExceptionHandler(HttpServletRequest request, Exception ex) throws Exception {

        if (AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class) != null) {
            throw ex;
        }

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setCode(ErrorEnum.SNEAK_AWAY.getCode());
        errorResponse.setMessage(ErrorEnum.SNEAK_AWAY.getMessage());
        errorResponse.setTimestamp(new Date());
        errorResponse.setException(ex.getClass().getName());
        errorResponse.setPath(request.getServletPath());

        ResponseEntity<ErrorResponse> responseEntity = ResponseEntity.ok().body(errorResponse);

        log.warn("ExceptionHandler::handle ex : ", ex);

        return responseEntity;
    }

}
