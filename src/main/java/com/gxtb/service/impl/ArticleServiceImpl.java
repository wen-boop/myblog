package com.gxtb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxtb.dao.ArticleDao;
import com.gxtb.dao.UserDao;
import com.gxtb.domain.Article;
import com.gxtb.domain.User;
import com.gxtb.service.ArticleService;
import com.gxtb.service.UserService;
import com.gxtb.utils.CodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisMode;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:24
 */
@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleDao, Article> implements ArticleService {


}
