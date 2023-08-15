package com.gxtb.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxtb.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:28
 */
@Mapper
public interface UserDao extends BaseMapper<User>{
}
