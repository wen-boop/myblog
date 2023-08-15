package com.gxtb.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.gxtb.domain.Photo;
import com.gxtb.domain.Web;
import com.gxtb.service.UserService;
import com.gxtb.service.WebService;
import com.gxtb.utils.CodeUtil;
import com.gxtb.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileWriter;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 23:11
 */
@RestController
@RequestMapping("/web")
public class WebController {
    @Autowired
    private WebService webService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @SaCheckRole(value = {"root","adminl","admins"},mode = SaMode.OR)
    @PostMapping("/postWeb")
    public R addWeb(@RequestBody Web web){
        web.setCreateTime(LocalDateTime.now());
        web.setUpdateTime(LocalDateTime.now());
        web.setUserId(userService.getLocalUserC().getId());
        if (!webService.save(web))
            return R.fail();
        redisTemplate.delete(CodeUtil.WEB_RESOURCE_PRE+"webs");
        return R.success();
    }

    @SaCheckRole(value = {"root","adminl","admins"},mode = SaMode.OR)
    @PostMapping("/modifyWeb")
    public R updateWeb(@RequestBody Web web){
        System.out.println(web);
        web.setUpdateTime(LocalDateTime.now());
        if (!webService.updateById(web))
            return R.fail();
        redisTemplate.delete(CodeUtil.WEB_RESOURCE_PRE+"webs");
        redisTemplate.delete(CodeUtil.WEB_RESOURCE_PRE+"web:"+web.getId());
        return R.success();
    }

    @SaCheckRole(value = {"root","adminl","admins"},mode = SaMode.OR)
    @DeleteMapping("/deleteWeb/{id}")
    public R deleteWeb(@PathVariable int id){
        if (!webService.removeById(id)){
            return R.fail();
        }
        redisTemplate.delete(CodeUtil.WEB_RESOURCE_PRE+"webs");
        redisTemplate.delete(CodeUtil.WEB_RESOURCE_PRE+"web:"+id);
        return R.success();
    }

    @GetMapping("/getWebs")
    public R getWebs(){
        List<Web> webs = (List<Web>) redisTemplate.opsForValue().get(CodeUtil.WEB_RESOURCE_PRE + "webs");
        if (Objects.isNull(webs))
            webs = webService.list();
        redisTemplate.opsForValue().set(CodeUtil.WEB_RESOURCE_PRE+"webs",webs,120, TimeUnit.MINUTES);
        return R.success(webs);

    }
}
