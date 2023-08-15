package com.gxtb.controller;

import com.gxtb.utils.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/18 9:38
 */
@RestControllerAdvice
public class MyExceptionHandler {
    @ExceptionHandler(cn.dev33.satoken.exception.NotLoginException.class)
    public R notLogin(){
        return R.fail("未登录");
    }

    @ExceptionHandler(cn.dev33.satoken.exception.NotRoleException.class)
    public R noPermission(){
        return R.fail("无权限");
    }
}
