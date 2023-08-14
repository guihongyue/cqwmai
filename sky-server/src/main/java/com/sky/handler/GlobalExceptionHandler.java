package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的业务异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result exceptionHandler(BaseException ex){
        log.error("异常信息：{}", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /*   捕获账号与存在异常   */
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        // Duplicate entry 'lis' for key 'idx_username'  把'lis'取出，
        String message = ex.getMessage();
        //如果含有Duplicate entry 这进行下面摘要操作
        if(message.contains("Duplicate entry")){
            String[] split = message.split(" ");        //用空格进行切割
            String username = split[2];                 //获取第三个内容
            String msg = username + MessageConstant.ACCOUNT_LOCKED;// MessageConstant.ACCOUNT_LOCKED 是优先写好的参数
            return Result.error(msg);
        }else{
            return Result.error("未知错误");
        }
    }

}
