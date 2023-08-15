package com.gxtb.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckOr;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gxtb.domain.Article;
import com.gxtb.domain.Comment;
import com.gxtb.domain.User;
import com.gxtb.service.ArticleService;
import com.gxtb.service.CommentService;
import com.gxtb.service.UserService;
import com.gxtb.utils.CodeUtil;
import com.gxtb.utils.R;
import net.sf.jsqlparser.parser.feature.Feature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static net.sf.jsqlparser.parser.feature.Feature.comment;
import static net.sf.jsqlparser.parser.feature.Feature.use;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 23:11
 */
@RestController
@RequestMapping("/article")
public class ArticleController {
    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private CommentService commentService;

    private String array2String(List<String> strs){
        if (strs.size()==0)
            return null;
        StringBuilder sb=new StringBuilder(strs.get(0));
        for (int i = 1; i < strs.size(); i++) {
            sb.append(","+ strs.get(i));
        }
        return sb.toString();
    }

    @SaCheckRole(value = {"root","adminl"},mode = SaMode.OR)
    @PostMapping("/postArticle")
    public R addArticle(@RequestBody Article article){
        article.setCreateTime(LocalDateTime.now());
        article.setUpdateTime(LocalDateTime.now());
        article.setUserId(userService.getLocalUserC().getId());
        article.setArtiImgUrls(array2String(article.getArtiImgUrlList()));
        articleService.save(article);
        redisTemplate.delete(CodeUtil.ARTICLE_RESOURCE_PRE+"articles");
        return R.success();
    }

    @SaCheckRole(value = {"root","adminl"},mode = SaMode.OR)
    @PostMapping("/modifyArticle")
    public R updateArticle(@RequestBody Article article){
        article.setArtiImgUrls(array2String(article.getArtiImgUrlList()));
        article.setUpdateTime(LocalDateTime.now());
        if (!articleService.updateById(article))
            return R.fail();
        redisTemplate.delete(CodeUtil.ARTICLE_RESOURCE_PRE+"articles");
        redisTemplate.delete(CodeUtil.ARTICLE_RESOURCE_PRE+article.getId());
        return R.success();
    }

    @SaCheckRole(value = {"root","adminl"},mode = SaMode.OR)
    @DeleteMapping("/deleteArticle/{id}")
    public R deleteArticle(@PathVariable int id){
        if (!articleService.removeById(id)){
            return R.fail();
        }
        redisTemplate.delete(CodeUtil.ARTICLE_RESOURCE_PRE+"articles");
        redisTemplate.delete(CodeUtil.ARTICLE_RESOURCE_PRE+id);
        return R.success();
    }

    @GetMapping("/getArticles")
    public R getArticles(){
        List<Article> articles= (List<Article>) redisTemplate.opsForValue().get(CodeUtil.ARTICLE_RESOURCE_PRE+"articles");
        if (articles==null) {
            LambdaQueryWrapper<Article> wrapper=new LambdaQueryWrapper<>();
            wrapper.orderByDesc(Article::getCreateTime);
            articles = articleService.list(wrapper);
            for (Article article: articles) {
                String urls = article.getArtiImgUrls();
                if (urls==null) {
                    article.setArtiImgUrlList(new ArrayList<>());
                    continue;
                }
                List<String> urlList = Arrays.asList(urls.split(","));
                article.setArtiImgUrlList(urlList);
            }
        }
        redisTemplate.opsForValue().set(CodeUtil.ARTICLE_RESOURCE_PRE+"articles",articles,120, TimeUnit.MINUTES);
        for (Article article : articles) {
            int userId = article.getUserId();
            User user = (User) redisTemplate.opsForValue().get(CodeUtil.USER_RESOURCE_PRE + "userDetail:" + userId);
            if (Objects.isNull(user))
                user=userService.getById(userId);
            redisTemplate.opsForValue().set(CodeUtil.USER_RESOURCE_PRE+"userDetail:"+userId,user,120,TimeUnit.MINUTES);
            String nickName = user.getNickName();
            article.setNickName(nickName);
        }
        return R.success(articles);
    }

    @GetMapping("/getArticleById/{id}")
    public R getArticleById(@PathVariable("id") int id){
        Article article = (Article) redisTemplate.opsForValue().get(CodeUtil.ARTICLE_RESOURCE_PRE + id);
        if (article==null) {
            article = articleService.getById(id);
            String urls = article.getArtiImgUrls();
            if (urls==null)
                article.setArtiImgUrlList(new ArrayList<>());
            else {
                List<String> urlList = Arrays.asList(urls.split(","));
                article.setArtiImgUrlList(urlList);
            }
        }
        redisTemplate.opsForValue().set(CodeUtil.ARTICLE_RESOURCE_PRE + id,article,120,TimeUnit.MINUTES);
        int userId = article.getUserId();
        User user = (User) redisTemplate.opsForValue().get(CodeUtil.USER_RESOURCE_PRE + "userDetail:" + userId);
        if (Objects.isNull(user))
            user=userService.getById(userId);
        redisTemplate.opsForValue().set(CodeUtil.USER_RESOURCE_PRE+"userDetail:"+userId,user,120,TimeUnit.MINUTES);
        String nickName = user.getNickName();
        article.setNickName(nickName);
        return R.success(article);
    }

    @GetMapping("/getComments/{id}")
    public R getComments(@PathVariable int id){
        List<Comment> comments = commentService.getByArticleIdC(id);
        for (Comment comment : comments) {
            List<Comment> children = commentService.getChildrenCommentByIdC(comment.getId());
            comment.setChildrenComments(children);
        }
        return R.success(comments);
    }

    @SaCheckLogin
    @PostMapping("/postComment")
    public R postComment(@RequestBody Comment comment){
        if (comment.getContent()==null||comment.getContent().length()==0)
            return R.fail("内容不能为空");
        comment.setLikes(0);
        comment.setUserId(userService.getLocalUserC().getId());
        comment.setCreateTime(LocalDateTime.now());
        boolean save = commentService.save(comment);
        if (!save)
            return R.fail("错误");
        redisTemplate.delete(CodeUtil.COMMENTS_PRE+comment.getArticleId());
        redisTemplate.delete(CodeUtil.RECEIVES_PRE+comment.getToUserId());
        return R.success();
    }

    @SaCheckLogin
    @PostMapping("/postReceive")
    public R postReceive(@RequestBody Comment receive){
        String content = receive.getContent();
        if (content==null||content.length()==0)
            return R.fail("内容不能为空");
        if (content.startsWith("@<")&&content.endsWith(">：")){
            return R.fail("内容不能为空");
        }
        receive.setLikes(0);
        receive.setUserId(userService.getLocalUserC().getId());
        receive.setCreateTime(LocalDateTime.now());
        boolean save = commentService.save(receive);
        if (!save)
            return R.fail("错误");
        redisTemplate.delete(CodeUtil.CHILDREN_COMMENTS_PRE+receive.getParentId());
        redisTemplate.delete(CodeUtil.RECEIVES_PRE+receive.getToUserId());
        return R.success();
    }

    @SaCheckLogin
    @PostMapping("/deleteComOrRec")
    public R deleteComOrRec(@RequestBody Comment comment){
        boolean b = commentService.removeCommentC(comment);
        if (!b)
            return R.fail("删除失败");
        return R.success();
    }

    @SaCheckLogin
    @GetMapping("/getReceives")
    public R getReceives(){
        String userName = StpUtil.getLoginIdAsString();
        User user = userService.getByUserName(userName);
        List<Comment> receives=commentService.getReceivesC(user.getId());
        return R.success(receives);
    }

    @SaCheckLogin
    @PostMapping("/comfirm")
    public R confirm(){
        String userName = StpUtil.getLoginIdAsString();
        User user = userService.getByUserName(userName);
        List<Comment> receives = commentService.confirmC(user.getId());
        return R.success(receives);
    }

/*    @SaCheckLogin
    @PostMapping("/likeComOrRec")
    public R likeComOrRec(){

    }*/
}
