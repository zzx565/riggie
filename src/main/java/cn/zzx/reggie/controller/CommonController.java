package cn.zzx.reggie.controller;

import cn.zzx.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;


// 文件上传下载
@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        // file是一个临时文件，需要指定保存路径，否则本次请求完成后临时文件就会删除
        log.info(file.toString());

        // 创建目录对象
        File dir = new File(basePath);
        if(!dir.exists()){
            // 目录不存在，需要创建
            dir.mkdirs();
        }

        // 原始文件名
        String originalFilename = file.getOriginalFilename();
        String suffix= originalFilename.substring(originalFilename.lastIndexOf("."));

        // 使用UUID重新生成文件名，防止文件名重复造成覆盖
        String filename = UUID.randomUUID().toString() + suffix;
        // 将临时文件转存到指定位置
        file.transferTo(new File(basePath + filename));
        return R.success(filename);
    }

    /**
     * 文件下载
     * @param name
     * @param response
     * @throws Exception
     */
    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws Exception {
        // 输入流，通过输入流读取文件
        FileInputStream fileInputStream =  new FileInputStream(new File(basePath+name));
        // 输出流，通过输出流将文件写到浏览器中
        ServletOutputStream outputStream = response.getOutputStream();

        response.setContentType("image/jpeg");

        int len = 0;
        byte[] bytes = new byte[1024];
        while ((len = fileInputStream.read(bytes))!=-1){
            outputStream.write(bytes,0,len);
            outputStream.flush();
        }

        // 关闭资源
        fileInputStream.close();
        outputStream.close();

    }

}
