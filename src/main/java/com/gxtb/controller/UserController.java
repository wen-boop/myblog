package com.gxtb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxtb.domain.User;
import com.gxtb.domain.UserDto;
import com.gxtb.service.UserService;
import com.gxtb.utils.CodeUtil;
import com.gxtb.utils.CommonUtils;
import com.gxtb.utils.MailUtils;
import com.gxtb.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 23:11
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MailUtils mailUtils;

    @PostMapping("/login")
    public R login(@RequestBody User user) {
        if (Objects.isNull(user) || Objects.isNull(user.getUserName()) || Objects.isNull(user.getPassword()))
            return R.fail("请输入用户名及密码");
        User byUserName = userService.getByUserName(user.getUserName());
        if (Objects.isNull(byUserName)||!user.getUserName().equals(byUserName.getUserName()) || !user.getPassword().equals(byUserName.getPassword())) {
            return R.fail("用户名或密码错误");
        }
        if (StpUtil.isDisable(user.getUserName()))
            return R.fail("账号已被封禁,距离解封还有"+StpUtil.getDisableTime(user.getUserName())+"秒");
        StpUtil.login(user.getUserName());
        redisTemplate.opsForValue().set(CodeUtil.LOGIN_USER_PRE+byUserName.getUserName(),byUserName,120,TimeUnit.MINUTES);
        return R.success("登陆成功");
    }

    @GetMapping("/isLogin")
    public R isLogin() {
        if (StpUtil.isLogin())
            return R.success(200);
        return R.success(201);
    }

    @SaCheckRole("root")
    @PostMapping("/changeStatus/{changedStatus}")
    public R changeStatus(@RequestBody User user,@PathVariable int changedStatus){
        if (user.getStatus()==0)
            return R.fail("root用户不可被修改");
        user.setStatus(changedStatus);
        user.setUpdateTime(LocalDateTime.now());
        userService.updateById(user);
        redisTemplate.delete(CodeUtil.USER_RESOURCE_PRE+"users");
        return R.success();
    }

    @SaCheckRole(value = {"root","adminl","admins"},mode = SaMode.OR)
    @PostMapping("/forbid/{num}")
    public R forbid(@RequestBody User user,@PathVariable int num){
        StpUtil.kickout(user.getUserName());
        StpUtil.disable(user.getUserName(),num*60*60);
        redisTemplate.delete(CodeUtil.USER_RESOURCE_PRE+"forbidden");
        return R.success();
    }

    @SaCheckRole(value = {"root","adminl","admins"},mode = SaMode.OR)
    @PostMapping("/release")
    public R release(@RequestBody User user){
        if (!StpUtil.isDisable(user.getUserName()))
            return R.fail("该用户未被封禁");
        StpUtil.untieDisable(user.getUserName());
        redisTemplate.delete(CodeUtil.USER_RESOURCE_PRE+"forbidden");
        return R.success();
    }

    @SaCheckRole(value = {"root","adminl","admins"},mode = SaMode.OR)
    @GetMapping("/getUsers")
    public R getUsers(){
        List<User> users = (List<User>) redisTemplate.opsForValue().get(CodeUtil.USER_RESOURCE_PRE + "users");
        if (Objects.isNull(users)){
            users=userService.list();
        }
        Map<Integer,Integer> forbidden= (Map<Integer, Integer>) redisTemplate.opsForValue().get(CodeUtil.USER_RESOURCE_PRE+"forbidden");
        if (Objects.isNull(forbidden)){
            forbidden=new HashMap<>();
            for (User user : users) {
                forbidden.put(user.getId(),StpUtil.isDisable(user.getUserName())?1:0);
            }
        }
        List<Object> objs=new ArrayList<>();
        objs.add(users);
        objs.add(forbidden);
        redisTemplate.opsForValue().set(CodeUtil.USER_RESOURCE_PRE+"users",users,120,TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(CodeUtil.USER_RESOURCE_PRE+"forbidden",forbidden,120,TimeUnit.MINUTES);
        return R.success(objs);
    }

    private R checkUser(User user){
        if (Objects.isNull(user))
            return R.fail();
        if (user.getUserName()==null||user.getUserName().length()<6)
            return R.fail("用户名需大于6位数");
        if (user.getPassword()==null||user.getPassword().length()<6)
            return R.fail("密码为空或过于简单，密码需大于6位");
        if (user.getEmail()==null)
            return R.fail("邮箱为空");
        if (null!=userService.getByUserName(user.getUserName()))
            return R.fail("用户名被占用了，换一个吧");
        if (null==user.getPassword()||user.getPassword()=="")
            return R.fail("麻烦设置一下密码");
        return R.success();
    }

    @PostMapping("/getCode")
    public R code(@RequestBody User user){
        R r = checkUser(user);
        if (r.getStatus()!=200)
            return r;
        String key="regist:code:"+user.getUserName()+user.getEmail();
        String code = CommonUtils.getRandomCode(6);
//        if (null!=redisTemplate.opsForValue().get(key))
//            return R.fail("发送过了，没收到一分钟后再试吧");
        redisTemplate.opsForValue().set(key,code,5, TimeUnit.MINUTES);
        mailUtils.sendTextMailMessage(user.getEmail(), "注册验证码（五分钟内有效）",code);
        return R.success();
    }

    @PostMapping("/regist")
    public R noCode(){
        return R.fail("请输入验证码");
    }

    @PostMapping("/regist/{code}")
    public R regist(@RequestBody User user,@PathVariable String code){
        R r = checkUser(user);
        if (r.getStatus()!=200)
            return r;
        String key= "regist:code:" +user.getUserName()+user.getEmail();
        String realCode = (String) redisTemplate.opsForValue().get(key);
        if (code==null||code==""||realCode==null||!realCode.equals(code))
            return R.fail("验证码错误");
        redisTemplate.delete(key);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setStatus(3);
        userService.save(user);
        redisTemplate.delete(CodeUtil.USER_RESOURCE_PRE+"users");
        redisTemplate.delete(CodeUtil.USER_RESOURCE_PRE+"forbidden");
        return R.success("注册成功");
    }

    @GetMapping("/status")
    public R getStatus(){
        return R.success(StpUtil.getRoleList());
    }

    @GetMapping("/userDetail")
    public R getUserDetail() {
        User user = userService.getLocalUserC();
        if (user==null)
            return R.fail("未登录");
        return R.success(user);
    }

    @PostMapping("/logout")
    public R logout() {
        if (!StpUtil.isLogin())
            return R.fail("未登录");
        String userName = (String) StpUtil.getLoginId();
        StpUtil.logout();
        redisTemplate.delete(CodeUtil.LOGIN_USER_PRE+userName);
        return R.success();
    }
}
