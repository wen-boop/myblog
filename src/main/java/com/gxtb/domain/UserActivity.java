package com.gxtb.domain;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/8/12 10:35
 */
@Data
public class UserActivity {
    private int id;
    private int userId;
    private int activityId;
    private LocalDateTime createTime;
}
