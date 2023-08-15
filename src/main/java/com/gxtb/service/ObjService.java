package com.gxtb.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gxtb.domain.Obj;
import com.gxtb.domain.Photo;

import java.util.List;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/10 22:24
 */

public interface ObjService extends IService<Obj> {
    Obj getByIdC(int id);

}
