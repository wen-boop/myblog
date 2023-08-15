package com.gxtb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxtb.domain.Activity;
import com.gxtb.domain.Obj;
import com.gxtb.domain.User;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:24
 */

public interface ActivityService extends IService<Activity> {
    List<Activity> getActivitiesC();

    Activity getByIdC(int id);

    String tryActivity(int id);

    @Transactional
    void postMq(User user, Activity activity);
}
