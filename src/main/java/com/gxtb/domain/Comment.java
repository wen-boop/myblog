package com.gxtb.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/28 16:18
 */
@Data
public class Comment {
    private int id;
    private int userId;
    @TableField(exist = false)
    private String nickName;
    @TableField(exist = false)
    private String img;
    private int toUserId;
    private int articleId;
    private int parentId;
    @TableField(exist = false)
    private String toContent;
    private String content;
    private int likes;
    @TableField(exist = false)
    private List<Comment> childrenComments;
    private int confirmed;
    private LocalDateTime createTime;
}
