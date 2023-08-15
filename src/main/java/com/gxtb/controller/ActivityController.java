package com.gxtb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxtb.domain.Activity;
import com.gxtb.service.ActivityService;
import com.gxtb.utils.CodeUtil;
import com.gxtb.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/8/8 16:36
 */
@RestController
@RequestMapping("/activity")
public class ActivityController {
    @Autowired
    private ActivityService activityService;

    @Autowired
    private RedisTemplate redisTemplate;

    @GetMapping("/getActivities")
    public R getActivities(){
        List<Activity> activities=activityService.getActivitiesC();
        return R.success(activities);
    }

    @SaCheckRole("root")
    @PostMapping("/postActivity")
    public R postActivity(@RequestBody Activity activity){
        activity.setCreateTime(LocalDateTime.now());
        activity.setLeftNum(activity.getNum());
        activity.setDel(0);
        System.out.println(activity.getNum());
        if (activity.getName()==null||activity.getName()==""||activity.getNum()<=0||activity.getObjNum()<=0)
            return R.fail("请正确填写数据");
        if (!activityService.save(activity))
            return R.fail("请正确填写数据");
        redisTemplate.delete(CodeUtil.ACTIVITY_PRE+"activities");
        return R.success();
    }

    @SaCheckRole("root")
    @PostMapping("/deleteActivity")
    public R deleteActivity(@RequestBody Activity activity){
        activity.setDel(1);
        if (!activityService.updateById(activity)){
            return R.fail();
        }
        redisTemplate.delete(CodeUtil.ACTIVITY_PRE+"activities");
        redisTemplate.delete(CodeUtil.ACTIVITY_PRE+activity.getId());
        redisTemplate.delete(CodeUtil.ACTIVITY_PRE+"left:"+activity.getId());
        redisTemplate.delete(CodeUtil.ACTIVITY_PRE+"attended:"+activity.getId());
        return R.success();
    }

    @SaCheckLogin
    @PostMapping("/try/{id}")
    public R tryActivity(@PathVariable int id){
        String s=activityService.tryActivity(id);
        return R.success(s);
    }
}
