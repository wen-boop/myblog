package com.gxtb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxtb.domain.Comment;

import java.util.List;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/28 16:20
 */
public interface CommentService extends IService<Comment> {

    List<Comment> getByArticleIdC(int id);

    List<Comment> getChildrenCommentByIdC(int id);

    boolean removeCommentC(Comment comment);

    List<Comment> getReceivesC(int id);

    List<Comment> confirmC(int id);
}
