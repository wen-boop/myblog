package com.gxtb.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gxtb.domain.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/28 16:21
 */
@Mapper
public interface CommentDao extends BaseMapper<Comment> {
}
