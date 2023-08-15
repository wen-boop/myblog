package com.gxtb.controller;

import cn.dev33.satoken.annotation.SaCheckOr;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxtb.domain.Article;
import com.gxtb.domain.Photo;
import com.gxtb.domain.Web;
import com.gxtb.service.ArticleService;
import com.gxtb.service.PhotoService;
import com.gxtb.service.UserService;
import com.gxtb.utils.CodeUtil;
import com.gxtb.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 23:11
 */
@RestController
@RequestMapping("/photo")
public class PhotoController {
    @Autowired
    private PhotoService photoService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @SaCheckRole("root")
    @PostMapping("/postPhoto")
    public R addPhoto(@RequestBody Photo photo){
        photo.setCreateTime(LocalDateTime.now());
        photo.setUpdateTime(LocalDateTime.now());
        photo.setUserId(userService.getLocalUserC().getId());
        if (!photoService.save(photo))
            return R.fail();
        redisTemplate.delete(CodeUtil.PHOTO_RESOURCE_PRE+"photos");
        return R.success();
    }

    @SaCheckRole("root")
    @PostMapping("/modifyPhoto")
    public R updatePhoto(@RequestBody Photo photo){
        photo.setUpdateTime(LocalDateTime.now());
        if (!photoService.updateById(photo))
            return R.fail();
        redisTemplate.delete(CodeUtil.PHOTO_RESOURCE_PRE+"photos");
        redisTemplate.delete(CodeUtil.PHOTO_RESOURCE_PRE+"photo:"+photo.getId());
        return R.success();
    }

    @SaCheckRole("root")
    @DeleteMapping("/deletePhoto/{id}")
    public R deletePhoto(@PathVariable int id){
        if (!photoService.removeById(id)){
            return R.fail();
        }
        redisTemplate.delete(CodeUtil.PHOTO_RESOURCE_PRE+"photos");
        redisTemplate.delete(CodeUtil.PHOTO_RESOURCE_PRE+"photo:"+id);
        return R.success();
    }

    @GetMapping("/getPhotos")
    public R getPhotos(){
        List<Photo> photos = (List<Photo>) redisTemplate.opsForValue().get(CodeUtil.PHOTO_RESOURCE_PRE + "photos");
        if (Objects.isNull(photos)) {
            LambdaQueryWrapper<Photo> wrapper=new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Photo::getCreateTime);
            photos = photoService.list(wrapper);
        }
        redisTemplate.opsForValue().set(CodeUtil.PHOTO_RESOURCE_PRE+"photos",photos,120, TimeUnit.MINUTES);
        return R.success(photos);
    }

}
