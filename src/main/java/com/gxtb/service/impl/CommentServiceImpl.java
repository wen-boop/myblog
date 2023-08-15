package com.gxtb.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gxtb.dao.CommentDao;
import com.gxtb.domain.Article;
import com.gxtb.domain.Comment;
import com.gxtb.domain.User;
import com.gxtb.service.ArticleService;
import com.gxtb.service.CommentService;
import com.gxtb.service.UserService;
import com.gxtb.utils.CodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/28 16:22
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentDao, Comment> implements CommentService {

    @Autowired
    private UserService userService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Comment> getByArticleIdC(int id) {
        List<Comment> comments = (List<Comment>) redisTemplate.opsForValue().get(CodeUtil.COMMENTS_PRE + id);
        if (Objects.isNull(comments)) {
            LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getArticleId, id);
            wrapper.eq(Comment::getParentId,0);
            wrapper.orderByAsc(Comment::getId);
            comments = list(wrapper);
        }
        redisTemplate.opsForValue().set(CodeUtil.COMMENTS_PRE+id,comments,120, TimeUnit.MINUTES);
        for (Comment comment : comments) {
            int userId = comment.getUserId();
            User user = (User) redisTemplate.opsForValue().get(CodeUtil.USER_RESOURCE_PRE + "userDetail:" + userId);
            if (Objects.isNull(user))
                user=userService.getById(userId);
            redisTemplate.opsForValue().set(CodeUtil.USER_RESOURCE_PRE+"userDetail:"+userId,user,120,TimeUnit.MINUTES);
            String nickName = user.getNickName();
            comment.setNickName(nickName);
            comment.setImg(user.getImg());
        }
        return comments;
    }

    @Override
    public List<Comment> getChildrenCommentByIdC(int id){
        List<Comment> children = (List<Comment>) redisTemplate.opsForValue().get(CodeUtil.CHILDREN_COMMENTS_PRE + id);
        if (Objects.isNull(children)){
            LambdaQueryWrapper<Comment> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getParentId,id);
            wrapper.orderByAsc(Comment::getId);
            children=list(wrapper);
        }
        redisTemplate.opsForValue().set(CodeUtil.CHILDREN_COMMENTS_PRE+id,children,120, TimeUnit.MINUTES);
        for (Comment comment : children) {
            int userId = comment.getUserId();
            User user = (User) redisTemplate.opsForValue().get(CodeUtil.USER_RESOURCE_PRE + "userDetail:" + userId);
            if (Objects.isNull(user))
                user=userService.getById(userId);
            redisTemplate.opsForValue().set(CodeUtil.USER_RESOURCE_PRE+"userDetail:"+userId,user,120,TimeUnit.MINUTES);
            String nickName = user.getNickName();
            comment.setNickName(nickName);
            comment.setImg(user.getImg());
        }
        return children;
    }

    @Override
    @Transactional
    public boolean removeCommentC(Comment comment) {
        try {
            removeById(comment.getId());
            if (comment.getParentId()==0){
                LambdaQueryWrapper<Comment> wrapper=new LambdaQueryWrapper<>();
                wrapper.eq(Comment::getParentId,comment.getId());
                remove(wrapper);
            }
        }catch (Exception e){
            return false;
        }
        if (comment.getParentId()==0)
            redisTemplate.delete(CodeUtil.COMMENTS_PRE+comment.getArticleId());
        redisTemplate.delete(CodeUtil.CHILDREN_COMMENTS_PRE+comment.getId());
        return true;
    }

    @Override
    public List<Comment> getReceivesC(int id) {
        List<Comment> receives = (List<Comment>) redisTemplate.opsForValue().get(CodeUtil.RECEIVES_PRE + id);
        if (receives==null){
            LambdaQueryWrapper<Comment> wrapper=new LambdaQueryWrapper<>();
            wrapper.eq(Comment::getToUserId,id);
            wrapper.orderByDesc(Comment::getCreateTime);
            receives=list(wrapper);
        }
        for (Comment receive : receives) {
            if (receive.getParentId()==0) {
                Article article = articleService.getById(receive.getArticleId());
                if (article!=null)
                    receive.setToContent(article.getArtiTitle());
                else
                    receive.setToContent("内容已删除");
            }
            else{
                Comment comment = getById(receive.getParentId());
                if (comment!=null)
                    receive.setToContent(comment.getContent());
                else
                    receive.setToContent("内容已删除");
        }
        }
        redisTemplate.opsForValue().set(CodeUtil.RECEIVES_PRE+id,receives,120, TimeUnit.MINUTES);
        for (Comment comment : receives) {
            int userId = comment.getUserId();
            User user = (User) redisTemplate.opsForValue().get(CodeUtil.USER_RESOURCE_PRE + "userDetail:" + userId);
            if (Objects.isNull(user))
                user=userService.getById(userId);
            redisTemplate.opsForValue().set(CodeUtil.USER_RESOURCE_PRE+"userDetail:"+userId,user,120,TimeUnit.MINUTES);
            String nickName = user.getNickName();
            comment.setNickName(nickName);
            comment.setImg(user.getImg());
        }
        return receives;
    }

    @Transactional
    @Override
    public List<Comment> confirmC(int id) {
        List<Comment> receives = getReceivesC(id);
        if (receives.get(0).getConfirmed()==1)
            return receives;
        for (Comment receive : receives) {
            if (receive.getConfirmed()==1)
                break;
            receive.setConfirmed(1);
        }
        updateBatchById(receives);
        redisTemplate.delete(CodeUtil.RECEIVES_PRE+id);
        redisTemplate.opsForValue().set(CodeUtil.RECEIVES_PRE+id,receives,120,TimeUnit.MINUTES);
        return receives;
    }
}
