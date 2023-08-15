package com.gxtb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxtb.config.RabbitMqConfig;
import com.gxtb.dao.ActivityDao;
import com.gxtb.dao.UserActivityDao;
import com.gxtb.domain.Activity;
import com.gxtb.domain.Obj;
import com.gxtb.domain.User;
import com.gxtb.domain.UserActivity;
import com.gxtb.service.ActivityService;
import com.gxtb.service.ObjService;
import com.gxtb.service.UserActivityService;
import com.gxtb.service.UserService;
import com.gxtb.utils.CodeUtil;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
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
public class UserActivityServiceImpl extends ServiceImpl<UserActivityDao, UserActivity> implements UserActivityService {
    @Autowired
    private RedisTemplate redisTemplate;

}
