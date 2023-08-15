package com.gxtb.domain;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:12
 */
@Data
public class User implements Serializable {
    private int id;
    private String nickName;
    private String userName;
    private String password;
    private String email;
    private String img;
    //0:root;1:adminl;2:admins;3:normal
    private int status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
