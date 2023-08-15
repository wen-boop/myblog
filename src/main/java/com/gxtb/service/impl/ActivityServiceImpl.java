package com.gxtb.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gxtb.config.RabbitMqConfig;
import com.gxtb.dao.ActivityDao;
import com.gxtb.domain.*;
import com.gxtb.service.*;
import com.gxtb.utils.CodeUtil;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:24
 */
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityDao, Activity> implements ActivityService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private ObjService objService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private UserObjService userObjService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Override
    public List<Activity> getActivitiesC() {
        List<Activity> activities = (List<Activity>) redisTemplate.opsForValue().get(CodeUtil.ACTIVITY_PRE + "activities");
        if (activities == null) {
            LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Activity::getDel,0);
            wrapper.orderByDesc(Activity::getEndTime);
            activities = list(wrapper);
            for (Activity activity : activities) {
                redisTemplate.opsForValue().setIfAbsent(CodeUtil.ACTIVITY_PRE + "left:" + activity.getId(), activity.getLeftNum());
                int objId = activity.getObjId();
                if (objId == 0)
                    continue;
                Obj obj = (Obj) redisTemplate.opsForValue().get(CodeUtil.OBJ_PRE + objId);
                if (Objects.isNull(obj))
                    obj = objService.getById(objId);
                redisTemplate.opsForValue().set(CodeUtil.OBJ_PRE + objId, obj, 120, TimeUnit.MINUTES);
                activity.setObjImg(obj.getImg());
                activity.setObjIntroduction(obj.getIntroduction());
                activity.setObjName(obj.getName());
            }
        }
        redisTemplate.opsForValue().set(CodeUtil.ACTIVITY_PRE+"activities",activities);
        User user = userService.getLocalUserC();
        for (Activity activity : activities) {
            activity.setLeftNum((int) redisTemplate.opsForValue().get(CodeUtil.ACTIVITY_PRE + "left:" + activity.getId()));
            activity.setAttended(redisTemplate.opsForSet().isMember(CodeUtil.ACTIVITY_PRE+"attended:"+activity.getId(),user.getId())?1:0);
        }
        return activities;
    }

    @Override
    public Activity getByIdC(int id) {
        Activity activity = (Activity) redisTemplate.opsForValue().get(CodeUtil.ACTIVITY_PRE + id);
        if (activity == null) {
            activity = getById(id);
        }
        redisTemplate.opsForValue().set(CodeUtil.ACTIVITY_PRE + id, activity, 120, TimeUnit.MINUTES);
        return activity;
    }

    @Override
    public String tryActivity(int id) {
        Activity activity = getByIdC(id);
        if (activity.getStartTime().isAfter(LocalDateTime.now()))
            return "活动未开始";
        if (activity.getEndTime().isBefore(LocalDateTime.now()))
            return "活动已结束";
        Lock lock = new ReentrantLock();
        User user = userService.getLocalUserC();
        try {
            lock.lock();
            if (redisTemplate.opsForSet().isMember(CodeUtil.ACTIVITY_PRE + "attended:" + activity.getId(), user.getId()))
                return "已参与";
            int left = (int) redisTemplate.opsForValue().get(CodeUtil.ACTIVITY_PRE + "left:" + activity.getId());
            if (left <= 0)
                return "已满";
            redisTemplate.opsForValue().set(CodeUtil.ACTIVITY_PRE + "left:" + activity.getId(), left - 1, Duration.between(activity.getStartTime(), activity.getEndTime()).toMinutes(), TimeUnit.MINUTES);
            activity.setLeftNum(left-1);
            redisTemplate.opsForSet().add(CodeUtil.ACTIVITY_PRE + "attended:" + activity.getId(), user.getId());
        } finally {
            lock.unlock();
        }
        postMq(user, activity);
        return "成功参与";
    }

    @Override
    public void postMq(User user, Activity activity) {
        Map<String, Object> map = new HashMap<>();
        map.put("user", JSON.toJSONString(user));
        map.put("activity", JSON.toJSONString(activity));
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, "activity", JSON.toJSONString(map));
    }

    @RabbitListener(queues = {"attendActivity"},ackMode = "MANUAL")
    @Transactional
    public void saveMessage(Message message, @Headers Map<String, Object> headers, Channel channel) throws Exception {
        String body=new String(message.getBody());
        Map map = JSON.parseObject(body, HashMap.class);
        User user = JSON.parseObject((String) map.get("user"),User.class);
        Activity activity = JSON.parseObject((String) map.get("activity"),Activity.class);
        updateById(activity);

        UserActivity userActivity=new UserActivity();
        userActivity.setActivityId(activity.getId());
        userActivity.setUserId(user.getId());
        userActivity.setCreateTime(LocalDateTime.now());

        LambdaQueryWrapper<UserObj> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(UserObj::getObjId,activity.getObjId());
        wrapper.eq(UserObj::getUserId,user.getId());
        UserObj one = userObjService.getOne(wrapper);
        if (one==null) {
            UserObj userObj = new UserObj();
            userObj.setUserId(user.getId());
            userObj.setObjId(activity.getObjId());
            userObj.setNum(activity.getObjNum());
            userObj.setInvalid(0);
            userObj.setCreateTime(LocalDateTime.now());
            userObjService.save(userObj);
        }
        else {
            one.setNum(one.getNum()+activity.getObjNum());
            userObjService.updateById(one);
        }
        redisTemplate.delete(CodeUtil.OBJ_PRE+"user:"+user.getId());
        channel.basicAck((Long) headers.get("amqp_deliveryTag"),false);
    }
}
