package com.gxtb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxtb.domain.User;
import org.springframework.stereotype.Service;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:24
 */

public interface UserService extends IService<User> {
    User getByUserName(String userName);
    User getLocalUserC();
}
