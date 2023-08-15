package com.gxtb.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/8/8 16:28
 */
@Data
public class Obj {
    private int id;
    private String name;
    private String img;
    private String introduction;
    private LocalDateTime createTime;
}
