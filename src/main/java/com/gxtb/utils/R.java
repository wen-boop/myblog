package com.gxtb.utils;

import lombok.Data;

import java.util.Objects;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 23:30
 */
@Data
public class R {
    private int status;
    private Object data;
    private String msg;

    public static R success(int status,String msg){
        R r=new R();
        r.status=status;
        r.msg=msg;
        return r;
    }
    public static R success(int status){
        R r=new R();
        r.status=status;
        return r;
    }
    public static R success(Object data,String msg){
        R r=new R();
        r.status=200;
        r.data=data;
        r.msg=msg;
        return r;
    }
    public static R success(Object data){
        R r=new R();
        r.status=200;
        r.data=data;
        r.msg="成功";
        return r;
    }
    public static R success(){
        R r=new R();
        r.status=200;
        r.msg="成功";
        return r;
    }
    public static R success(String msg){
        R r=new R();
        r.status=200;
        r.msg=msg;
        return r;
    }
    public static R fail(int status,String msg){
        R r=new R();
        r.status=status;
        r.msg="错误";
        return r;
    }
    public static R fail(String msg){
        R r=new R();
        r.status=500;
        r.msg=msg;
        return r;
    }
    public static R fail(){
        R r=new R();
        r.status=500;
        r.msg="错误";
        return r;
    }
}
