package com.gxtb.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/8/8 18:01
 */
@Data
public class UserObj {
    private int id;
    private int userId;
    private int ObjId;
    private int num;
    private LocalDateTime outTime;
    private int invalid;
    private LocalDateTime createTime;
    @TableField(exist = false)
    private String objName;
    @TableField(exist = false)
    private String objImg;
    @TableField(exist = false)
    private String objIntroduction;
}
