package com.gxtb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxtb.dao.ObjDao;
import com.gxtb.dao.PhotoDao;
import com.gxtb.domain.Obj;
import com.gxtb.domain.Photo;
import com.gxtb.service.ObjService;
import com.gxtb.service.PhotoService;
import com.gxtb.utils.CodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:24
 */
@Service
public class ObjServiceImpl extends ServiceImpl<ObjDao, Obj> implements ObjService {
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Obj getByIdC(int id) {
        Obj obj = (Obj) redisTemplate.opsForValue().get(CodeUtil.OBJ_PRE + id);
        if (obj==null)
            obj = getById(id);
        redisTemplate.opsForValue().set(CodeUtil.OBJ_PRE + id,obj,120, TimeUnit.MINUTES);
        return obj;
    }

}
