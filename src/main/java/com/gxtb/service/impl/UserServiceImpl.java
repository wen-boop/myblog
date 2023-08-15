package com.gxtb.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxtb.dao.UserDao;
import com.gxtb.domain.User;
import com.gxtb.service.UserService;
import com.gxtb.utils.CodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:24
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public User getByUserName(String userName){
        LambdaQueryWrapper<User> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserName,userName);
        User one = userDao.selectOne(wrapper);
        return one;
    }

    @Override
    public User getLocalUserC(){
        if (!StpUtil.isLogin())
            return null;
        String userName = StpUtil.getLoginIdAsString();
        User user = (User) redisTemplate.opsForValue().get(CodeUtil.LOGIN_USER_PRE + userName);
        if (user==null)
            user=getByUserName(userName);
        redisTemplate.opsForValue().set(CodeUtil.LOGIN_USER_PRE+userName,user,120, TimeUnit.MINUTES);
        return user;
    }

}
