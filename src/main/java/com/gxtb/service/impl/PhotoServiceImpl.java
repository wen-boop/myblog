package com.gxtb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxtb.dao.PhotoDao;
import com.gxtb.dao.UserDao;
import com.gxtb.domain.Photo;
import com.gxtb.domain.User;
import com.gxtb.service.PhotoService;
import com.gxtb.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:24
 */
@Service
public class PhotoServiceImpl extends ServiceImpl<PhotoDao, Photo> implements PhotoService {
}
