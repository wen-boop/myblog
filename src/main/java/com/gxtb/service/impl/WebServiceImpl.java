package com.gxtb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxtb.dao.UserDao;
import com.gxtb.dao.WebDao;
import com.gxtb.domain.User;
import com.gxtb.domain.Web;
import com.gxtb.service.UserService;
import com.gxtb.service.WebService;
import org.springframework.stereotype.Service;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:24
 */
@Service
public class WebServiceImpl extends ServiceImpl<WebDao, Web> implements WebService {
}
