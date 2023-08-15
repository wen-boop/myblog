package com.gxtb.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/8/8 16:31
 */
@Data
public class Activity implements Serializable {
    private int id;
    private String name;
    private String img;
    private String introduction;
    private int num;
    private int leftNum;
    private int objId;
    @TableField(exist = false)
    private String objName;
    @TableField(exist = false)
    private String objImg;
    @TableField(exist = false)
    private String objIntroduction;
    @TableField(exist = false)
    private int attended;
    private int objNum;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int del;
    private LocalDateTime createTime;
}
