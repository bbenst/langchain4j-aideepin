package com.moyz.adi.chat.controller;

import com.moyz.adi.common.entity.AdiFile;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.file.FileOperatorContext;
import com.moyz.adi.common.file.LocalFileUtil;
import com.moyz.adi.common.service.FileService;
import com.moyz.adi.common.util.UrlUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.moyz.adi.common.cosntant.AdiConstant.IMAGE_EXTENSIONS;
import static com.moyz.adi.common.enums.ErrorEnum.A_FILE_NOT_EXIST;
import static com.moyz.adi.common.enums.ErrorEnum.B_IMAGE_LOAD_ERROR;
import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
/**
 * 文件与图片资源访问接口控制器。
 */
@Slf4j
@RestController
@Validated
public class FileController {

    /**
     * 浏览器缓存时间设置，一年有效。
     */
    private static final String CACHE_TIME = "public, max-age=31536000";

    /**
     * 文件服务，负责文件存储与读取。
     */
    @Resource
    private FileService fileService;

//    @Operation(summary = "我的图片")
//    @GetMapping(value = "/my-image/{uuid}", produces = MediaType.IMAGE_PNG_VALUE)
//    public void myImage(@Length(min = 32, max = 32) @PathVariable String uuid, HttpServletResponse response) {
//        AdiFile adiFile = fileService.getByUuid(uuid);
//        if (null == adiFile) {
//            throw new BaseException(A_FILE_NOT_EXIST);
//        }
//        responseImage(uuid, adiFile.getExt(), false, response);
//    }

    /**
     * 获取当前用户图片的缩略图。
     *
     * @param uuidWithExt 图片 UUID（含扩展名）
     * @param response 响应对象
     */
    @GetMapping(value = "/my-thumbnail/{uuidWithExt}", produces = MediaType.IMAGE_PNG_VALUE)
    public void thumbnail(@Length(min = 32) @PathVariable String uuidWithExt, HttpServletResponse response) {
        String uuid = UrlUtil.getUuid(uuidWithExt);
        AdiFile adiFile = fileService.getByUuid(uuid);
        if (null == adiFile) {
            throw new BaseException(A_FILE_NOT_EXIST);
        }
        responseImage(uuid, adiFile.getExt(), true, response);
    }

//    /**
//     * 获取图片
//     *
//     * @param uuid     图片uuid
//     * @param response HttpServletResponse
//     */
//    @GetMapping(value = "/image/{uuid}", produces = MediaType.IMAGE_PNG_VALUE)
//    public void image(@Length(min = 32, max = 32) @PathVariable String uuid, HttpServletResponse response) {
//        AdiFile adiFile = fileService.getByUuid(uuid);
//        if (null == adiFile) {
//            throw new BaseException(A_FILE_NOT_EXIST);
//        }
//        responseImage(uuid, adiFile.getExt(), false, response);
//    }

    /**
     * 下载文件或直接输出图片资源。
     *
     * @param uuidWithExt 文件 UUID（含扩展名）
     * @param response 响应对象
     * @return 资源响应
     */
    @GetMapping(value = "/file/{uuidWithExt}")
    public ResponseEntity<org.springframework.core.io.Resource> file(@Length(min = 32) @PathVariable String uuidWithExt, HttpServletResponse response) {
        String uuid = UrlUtil.getUuid(uuidWithExt);
        AdiFile adiFile = fileService.getByUuid(uuid);
        if (null == adiFile) {
            throw new BaseException(A_FILE_NOT_EXIST);
        }
        if (IMAGE_EXTENSIONS.contains(adiFile.getExt().toLowerCase())) {
            responseImage(uuid, adiFile.getExt(), false, response);
            return null;
        }
        response.setHeader(CACHE_CONTROL, CACHE_TIME);
        byte[] bytes = LocalFileUtil.readBytes(adiFile.getPath());
        InputStreamResource inputStreamResource = new InputStreamResource(new ByteArrayInputStream(bytes));

        String fileName = adiFile.getName();
        if (StringUtils.isBlank(fileName)) {
            fileName = adiFile.getUuid() + "." + adiFile.getExt();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment().filename(fileName).build());
        return new ResponseEntity<>(inputStreamResource, headers, HttpStatus.OK);
    }

    /**
     * 输出图片流并设置缓存头。
     *
     * @param uuid 图片 UUID
     * @param ext 图片扩展名
     * @param thumbnail 是否输出缩略图
     * @param response 响应对象
     */
    private void responseImage(String uuid, String ext, boolean thumbnail, HttpServletResponse response) {
        BufferedImage bufferedImage = fileService.readMyImage(uuid, thumbnail);
        // 把图片写回浏览器响应流，避免再次封装
        try {
            // 缓存30天
            response.setHeader(CACHE_CONTROL, CACHE_TIME);
            ImageIO.write(bufferedImage, ext, response.getOutputStream());
        } catch (IOException e) {
            log.error("image error", e);
            throw new BaseException(B_IMAGE_LOAD_ERROR);
        }
    }

    /**
     * 上传文件并返回访问地址。
     *
     * @param file 上传文件
     * @return 文件 UUID 与访问 URL
     */
    @PostMapping(path = "/file/upload", headers = "content-type=multipart/form-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> upload(@RequestPart(value = "file") MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        AdiFile adiFile = fileService.saveFile(file, false);
        result.put("uuid", adiFile.getUuid());
        result.put("url", FileOperatorContext.getFileUrl(adiFile));
        return result;
    }

    /**
     * 上传图片并返回访问地址。
     *
     * @param file 上传图片
     * @return 图片 UUID 与访问 URL
     */
    @PostMapping(path = "/image/upload", headers = "content-type=multipart/form-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, String> imageUpload(@RequestPart(value = "file") MultipartFile file) {
        Map<String, String> result = new HashMap<>();
        AdiFile adiFile = fileService.saveFile(file, true);
        result.put("uuid", adiFile.getUuid());
        result.put("url", FileOperatorContext.getFileUrl(adiFile));
        return result;
    }

    /**
     * 删除指定文件并执行逻辑删除记录。
     *
     * @param uuid 文件 UUID
     * @return 是否删除成功
     */
    @PostMapping("/file/del/{uuid}")
    public boolean del(@PathVariable String uuid) {
        return fileService.removeFileAndSoftDel(uuid);
    }
}
