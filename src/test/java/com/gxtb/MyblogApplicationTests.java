package com.gxtb;

import com.gxtb.domain.User;
import com.gxtb.service.UserService;
import lombok.extern.log4j.Log4j;
import org.apache.ibatis.logging.Log;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class MyblogApplicationTests {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    void contextLoads() {
        redisTemplate.delete("users");
    }

}
