package cn.bupt.edu.zfq.reggietakeout.controller;

import cn.bupt.edu.zfq.reggietakeout.common.R;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RestController
public class ImageController {
    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     *
     * @param file 参数名字不能随便取, 必须和前端保持一致
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) {
        var suffix = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf('.')); // 截取文件后缀
        var randomUUID = UUID.randomUUID().toString(); // 生成UUID
        var fileName = randomUUID + suffix; // 拼接文, 最后名称, 结果为:  UUID + 后缀

        // 保证存储的位置有这个文件夹
        var dir = new File(basePath);
        if (!dir.exists())
            dir.mkdirs(); // 目标存储位置不存在, 就创建一个文件夹

        try {
            file.transferTo(new File(basePath + fileName)); // 转存文件到: 指定位置
        } catch (IOException e) {
            e.printStackTrace();
        }
        return R.success(fileName); // 把文件的名字上传回去, 方便后续回显读取路径
    }

    /**
     * 文件下载
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) {
        var bytes = new byte[1024];
        var length = 0;
        response.setContentType("image/jpeg");
        try (var fileInputStream = new FileInputStream(basePath + name);
             var outputStream = response.getOutputStream()) {
            while ((length = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, length);
                outputStream.flush();
            }
        } catch (FileNotFoundException e) {
            log.error("没有在 {} 找到名为 {} 的文件", basePath, name);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
