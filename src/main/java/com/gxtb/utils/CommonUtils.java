package com.gxtb.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.gxtb.domain.Article;
import com.gxtb.domain.User;
import com.gxtb.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.xml.ws.Service;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/17 10:39
 */
public class CommonUtils {

    private static String table="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String getRandomCode(int len){
        StringBuilder sb=new StringBuilder();
        Random random=new Random();
        for (int i = 0; i < len; i++) {
            sb.append(table.charAt(random.nextInt(table.length())));
        }
        return sb.toString();
    }

}
