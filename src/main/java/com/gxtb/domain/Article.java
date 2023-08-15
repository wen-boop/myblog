package com.gxtb.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:12
 */
@Data
public class Article {
    private int id;
    private int userId;
    @TableField(exist = false)
    private String nickName;
    private String artiTitle;
    private String artiImgUrls;
    @TableField(exist = false)
    private List<String> artiImgUrlList;
    private String artiDetail;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
