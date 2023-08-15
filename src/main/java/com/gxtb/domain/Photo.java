package com.gxtb.domain;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:12
 */
@Data
public class Photo {
    private int id;
    private int userId;
    private String photoName;
    private String photoImgUrl;
    private String photoDescription;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
