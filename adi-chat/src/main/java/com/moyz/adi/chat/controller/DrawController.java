package com.moyz.adi.chat.controller;

import com.moyz.adi.common.dto.*;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.service.DrawService;
import com.moyz.adi.common.service.FileService;
import com.moyz.adi.common.util.UrlUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import static com.moyz.adi.common.enums.ErrorEnum.*;

/**
 * 绘图任务与图片访问相关接口控制器。
 */
@Slf4j
@RestController
@RequestMapping("/draw")
@Validated
public class DrawController {

    /**
     * 绘图任务服务，负责创建、编辑与查询绘图任务。
     */
    @Resource
    private DrawService drawService;

    /**
     * 文件服务，用于读取生成图片。
     */
    @Resource
    private FileService fileService;

    /**
     * 通过提示词创建绘图任务。
     *
     * @param generateImageReq 生成请求
     * @return 绘图任务 UUID
     */
    @PostMapping("/generation")
    public Map<String, String> generation(@RequestBody @Validated GenerateImageReq generateImageReq) {
        String uuid = drawService.createByPrompt(generateImageReq);
        return Map.of("uuid", uuid);
    }

    /**
     * 重新触发指定绘图任务的生成。
     *
     * @param uuid 绘图任务 UUID
     */
    @PostMapping("/regenerate/{uuid}")
    public void regenerate(@PathVariable @Length(min = 32, max = 32) String uuid) {
        drawService.regenerate(uuid);
    }

    /**
     * 基于原图进行编辑生成。
     *
     * @param editImageReq 编辑请求
     * @return 新任务 UUID
     */
    @Operation(summary = "Edit image")
    @PostMapping("/edit")
    public Map<String, String> edit(@RequestBody EditImageReq editImageReq) {
        String uuid = drawService.editByOriginalImage(editImageReq);
        return Map.of("uuid", uuid);
    }

    /**
     * 生成图像变体。
     *
     * @param variationImageReq 变体请求
     * @return 新任务 UUID
     */
    @Operation(summary = "Image variation")
    @PostMapping("/variation")
    public Map<String, String> variation(@RequestBody VariationImageReq variationImageReq) {
        String uuid = drawService.variationImage(variationImageReq);
        return Map.of("uuid", uuid);
    }

    /**
     * 获取当前用户的绘图任务列表。
     *
     * @param maxId 游标 ID
     * @param pageSize 每页数量
     * @return 绘图任务列表
     */
    @GetMapping("/list")
    public DrawListResp list(@RequestParam Long maxId, @RequestParam int pageSize) {
        return drawService.listByCurrentUser(maxId, pageSize);
    }


    /**
     * 获取绘图任务详情。
     *
     * @param uuid 绘图任务 UUID
     * @return 绘图任务详情
     */
    @GetMapping("/detail/{uuid}")
    public DrawDto getOne(@PathVariable String uuid) {
        DrawDto drawDto = drawService.getPublicOrMine(uuid);
        if (null == drawDto) {
            throw new BaseException(A_DRAW_NOT_FOUND);
        }
        return drawDto;
    }

    /**
     * 查询更新的公开绘图任务。
     *
     * @param uuid 当前任务 UUID
     * @return 更新的公开任务
     */
    @GetMapping("/detail/newer-public/{uuid}")
    public DrawDto prevPublic(@PathVariable String uuid) {
        return drawService.newerPublicOne(uuid);
    }

    /**
     * 查询更早的公开绘图任务。
     *
     * @param uuid 当前任务 UUID
     * @return 更早的公开任务
     */
    @GetMapping("/detail/older-public/{uuid}")
    public DrawDto nextPublic(@PathVariable String uuid) {
        return drawService.olderPublicOne(uuid);
    }

    /**
     * 查询更新的收藏绘图任务。
     *
     * @param uuid 当前任务 UUID
     * @return 更新的收藏任务
     */
    @GetMapping("/detail/newer-starred/{uuid}")
    public DrawDto prevStarred(@PathVariable String uuid) {
        return drawService.newerStarredOne(uuid);
    }

    /**
     * 查询更早的收藏绘图任务。
     *
     * @param uuid 当前任务 UUID
     * @return 更早的收藏任务
     */
    @GetMapping("/detail/older-starred/{uuid}")
    public DrawDto nextStarred(@PathVariable String uuid) {
        return drawService.olderStarredOne(uuid);
    }

    /**
     * 查询更新的当前用户绘图任务。
     *
     * @param uuid 当前任务 UUID
     * @return 更新的用户任务
     */
    @GetMapping("/detail/newer-mine/{uuid}")
    public DrawDto prevMine(@PathVariable String uuid) {
        return drawService.newerMine(uuid);
    }

    /**
     * 查询更早的当前用户绘图任务。
     *
     * @param uuid 当前任务 UUID
     * @return 更早的用户任务
     */
    @GetMapping("/detail/older-mine/{uuid}")
    public DrawDto nextMine(@PathVariable String uuid) {
        return drawService.olderMine(uuid);
    }

    /**
     * 删除绘图任务的所有内容（提示词及生成图片）。
     *
     * @param uuid 绘图任务 UUID
     * @return 是否删除成功
     */
    @PostMapping("/del/{uuid}")
    public boolean del(@PathVariable String uuid) {
        return drawService.del(uuid);
    }

    /**
     * 删除绘图任务中的单张图片。
     *
     * @param uuid     绘图任务的uuid
     * @param fileUuid 待删除图片uuid
     * @return 是否删除成功
     */
    @PostMapping("/file/del/{fileUuid}")
    public boolean fileDel(@RequestParam @NotBlank String uuid, @PathVariable String fileUuid) {
        return drawService.delGeneratedFile(uuid, fileUuid);
    }

    /**
     * 设置绘图任务是否公开，并可选追加水印。
     *
     * @param uuid 绘图任务 UUID
     * @param isPublic 是否公开
     * @param withWatermark 是否添加水印
     * @return 更新后的绘图任务信息
     */
    @Operation(summary = "将绘图任务设置为公开或私有")
    @PostMapping("/set-public/{uuid}")
    public DrawDto setPublic(@PathVariable @NotBlank String uuid, @RequestParam(defaultValue = "false") Boolean isPublic, @RequestParam(required = false) Boolean withWatermark) {
        return drawService.setDrawPublic(uuid, isPublic, withWatermark);
    }

    /**
     * 查询公开的绘图任务列表。
     *
     * @param maxId 游标 ID
     * @param pageSize 每页数量
     * @return 公开绘图任务列表
     */
    @Operation(summary = "公开的绘图任务列表")
    @GetMapping("/public/list")
    public DrawListResp publicList(@RequestParam Long maxId, @RequestParam int pageSize) {
        return drawService.listPublic(maxId, pageSize);
    }

    /**
     * 输出公开图片文件（可能带水印）。
     *
     * @param drawUuid 绘图任务 UUID
     * @param imageUuidWithExt 图片 UUID（含扩展名）
     * @param response 响应对象
     */
    @Operation(summary = "公开的图片,可能带水印（根据水印设置决定）")
    @GetMapping(value = "/public/image/{drawUuid}/{imageUuidWithExt}", produces = MediaType.IMAGE_PNG_VALUE)
    public void publicImage(@Length(min = 32) @PathVariable String drawUuid, @Length(min = 32, max = 32) @PathVariable String imageUuidWithExt, HttpServletResponse response) {
        DrawDto drawDto = drawService.getPublicOrMine(drawUuid);
        if (null == drawDto) {
            throw new BaseException(A_AI_IMAGE_NO_AUTH);
        }
        String imageUuid = UrlUtil.getUuid(imageUuidWithExt);
        BufferedImage bufferedImage = fileService.readImage(imageUuid, false);
        // 把图片写回浏览器响应流，保持直接输出二进制内容
        try {
            ImageIO.write(bufferedImage, "png", response.getOutputStream());
        } catch (IOException e) {
            log.error("publicImage error", e);
            throw new BaseException(B_IMAGE_LOAD_ERROR);
        }
    }

    /**
     * 输出公开缩略图（可能带水印）。
     *
     * @param drawUuid 绘图任务 UUID
     * @param imageUuidWithExt 图片 UUID（含扩展名）
     * @param response 响应对象
     */
    @Operation(summary = "公开的缩略图,可能带水印（根据水印设置决定）")
    @GetMapping(value = "/public/thumbnail/{drawUuid}/{imageUuidWithExt}", produces = MediaType.IMAGE_PNG_VALUE)
    public void publicThumbnail(@Length(min = 32) @PathVariable String drawUuid, @Length(min = 32) @PathVariable String imageUuidWithExt, HttpServletResponse response) {
        DrawDto drawDto = drawService.getPublicOrMine(drawUuid);
        if (null == drawDto) {
            throw new BaseException(A_AI_IMAGE_NO_AUTH);
        }
        String imageUuid = UrlUtil.getUuid(imageUuidWithExt);
        BufferedImage bufferedImage = fileService.readImage(imageUuid, true);
        try {
            ImageIO.write(bufferedImage, "png", response.getOutputStream());
        } catch (IOException e) {
            log.error("publicThumbnail error", e);
            throw new BaseException(B_IMAGE_LOAD_ERROR);
        }
    }
}
