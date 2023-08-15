package com.gxtb.controller;

import ch.qos.logback.core.util.FileUtil;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.gxtb.utils.CodeUtil;
import com.gxtb.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: GXTB
 * @Description: TODO
 * @Date: 2023/7/19 16:48
 */
@RestController
@RequestMapping("/upload")
public class UploadController {

    @SaCheckLogin
    @RequestMapping("/uploadImg")
    public R uploadImg(@RequestParam("file") MultipartFile image){
        try {
            // 获取原始文件名称
            String originalFilename = image.getOriginalFilename();
            // 生成新文件名
            String fileName = createNewFileName(originalFilename);
            // 保存文件
            image.transferTo(new File(CodeUtil.IMG_PATH, fileName));
            // 返回结果
            return R.success(CodeUtil.IMG_PATH_TO_GET+fileName);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
    }

    @SaCheckLogin
    @PostMapping("/deleteImg")
    public R deleteBlogImg(@RequestBody Map name) {
        if (name.get("name")=="")
            return R.success();
        if (name==null||name.get("name")==null)
            return R.fail("无法获取文件");
        String fileName = (String) name.get("name");
        String replaced = fileName.replace(CodeUtil.IMG_PATH_TO_GET, CodeUtil.IMG_PATH+"\\");
        File file = new File(replaced);
        if (!file.exists()||file.isDirectory()) {
            return R.fail("错误的文件名称");
        }
        if (file.delete())
            return R.success();
        return R.fail();
    }

    private String createNewFileName(String originalFilename) {
        // 获取后缀
        String suffix = StringUtils.substringAfter(originalFilename, ".");
        // 生成目录
        String name = UUID.randomUUID().toString();
        // 判断目录是否存在
        File dir = new File(CodeUtil.IMG_PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 生成文件名
        return name+"."+suffix;
    }
}
