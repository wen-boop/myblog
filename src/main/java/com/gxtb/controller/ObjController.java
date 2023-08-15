package com.gxtb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxtb.domain.Article;
import com.gxtb.domain.Obj;
import com.gxtb.domain.User;
import com.gxtb.domain.UserObj;
import com.gxtb.service.ObjService;
import com.gxtb.service.UserObjService;
import com.gxtb.service.UserService;
import com.gxtb.utils.CodeUtil;
import com.gxtb.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/8/8 16:36
 */
@RestController
@RequestMapping("/obj")
public class ObjController {
    @Autowired
    private ObjService objService;

    @Autowired
    private UserObjService userObjService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/getObjs")
    public R getAllObjs(){
        List<Obj> objs= (List<Obj>) redisTemplate.opsForValue().get(CodeUtil.OBJ_PRE+"objs");
        if (objs==null)
            objs = objService.list();
        redisTemplate.opsForValue().set(CodeUtil.OBJ_PRE+"objs",objs,120, TimeUnit.MINUTES);
        return R.success(objs);
    }

    @SaCheckLogin
    @GetMapping("/getMyObjs")
    public R getMyObjs(){
        String userName = StpUtil.getLoginIdAsString();
        User user = userService.getByUserName(userName);
        List<UserObj> objs= (List<UserObj>) redisTemplate.opsForValue().get(CodeUtil.OBJ_PRE+"user:"+user.getId());
        if (objs==null) {
            LambdaQueryWrapper<UserObj> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(UserObj::getUserId,user.getId());
            wrapper.orderByDesc(UserObj::getCreateTime);
            objs = userObjService.list();
            for (UserObj uobj : objs) {
                Obj obj = objService.getByIdC(uobj.getObjId());
                uobj.setObjImg(obj.getImg());
                uobj.setObjName(obj.getName());
                uobj.setObjIntroduction(obj.getIntroduction());
            }
        }
        redisTemplate.opsForValue().set(CodeUtil.OBJ_PRE+"user:"+user.getId(),objs,120, TimeUnit.MINUTES);
        return R.success(objs);
    }

    @GetMapping("/getObjById/{id}")
    public R getObjById(@PathVariable int id){
        Obj obj=objService.getByIdC(id);
        return R.success(obj);
    }

    @SaCheckRole("root")
    @PostMapping("/addObj")
    public R addObj(@RequestBody Obj obj){
        obj.setCreateTime(LocalDateTime.now());
        if (obj.getName()==null||obj.getName().replace(" ","")=="")
            return R.fail("请正确填写数据");
        boolean save = objService.save(obj);
        if (!save)
            return R.fail("失败，可能名称重复");
        redisTemplate.delete(CodeUtil.OBJ_PRE+"objs");
        return R.success();
    }

}
