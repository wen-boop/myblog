package com.gxtb.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import com.gxtb.domain.User;
import com.gxtb.utils.CodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/15 11:12
 */
@Service
public class StpInterfaceImpl implements StpInterface {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<String> getPermissionList(Object userName, String s) {
        return null;
    }

    @Override
    public List<String> getRoleList(Object userName, String s) {
        User user = (User) redisTemplate.opsForValue().get(CodeUtil.LOGIN_USER_PRE + userName);
        int status = user.getStatus();
        List<String> list = new ArrayList<String>();
        if (status==3)
            list.add("normal");
        if (status==2)
            list.add("admins");
        if (status==1)
            list.add("adminl");
        if (status==0)
            list.add("root");
        return list;
    }
}
