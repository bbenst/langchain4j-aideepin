package com.moyz.adi.chat.controller;

import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.dto.ConfigResp;
import com.moyz.adi.common.dto.ModifyPasswordReq;
import com.moyz.adi.common.dto.UserUpdateReq;
import com.moyz.adi.common.entity.User;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.service.UserService;
import com.talanlabs.avatargenerator.Avatar;
import com.talanlabs.avatargenerator.cat.CatAvatar;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static com.moyz.adi.common.enums.ErrorEnum.B_IMAGE_LOAD_ERROR;

/**
 * 用户相关接口控制器。
 */
@Slf4j
@Tag(name = "用户controller")
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 用户服务，负责用户配置与认证相关操作。
     */
    @Resource
    private UserService userService;

    /**
     * 查询用户信息（当前用于简单日志验证）。
     *
     * @param uuid 用户 UUID
     */
    @Operation(summary = "用户信息")
    @GetMapping("/{uuid}")
    public void info(@Validated @PathVariable String uuid) {
        log.info(uuid);
    }

    /**
     * 获取当前用户配置。
     *
     * @return 配置响应
     */
    @Operation(summary = "配置信息")
    @GetMapping("/config")
    public ConfigResp configInfo() {
        return userService.getConfig();
    }

    /**
     * 更新当前用户配置。
     *
     * @param userUpdateReq 更新请求
     */
    @Operation(summary = "更新信息")
    @PostMapping("/edit")
    public void update(@Validated UserUpdateReq userUpdateReq) {
        userService.updateConfig(userUpdateReq);
    }

    /**
     * 修改当前用户密码。
     *
     * @param modifyPasswordReq 密码修改请求
     * @return 操作结果提示
     */
    @Operation(summary = "修改密码")
    @PostMapping("/password/modify")
    public String modifyPassword(@RequestBody ModifyPasswordReq modifyPasswordReq) {
        userService.modifyPassword(modifyPasswordReq.getOldPassword(), modifyPasswordReq.getNewPassword());
        return "修改成功";
    }

    /**
     * 退出登录。
     */
    @Operation(summary = "退出")
    @PostMapping("/logout")
    public void logout() {
        userService.logout();
    }

    /**
     * 获取当前用户头像。
     *
     * @param response 响应对象
     */
    @Operation(summary = "当前用户头像")
    @GetMapping(value = "/myAvatar", produces = MediaType.IMAGE_JPEG_VALUE)
    public void myAvatar(HttpServletResponse response) {
        User user = ThreadContext.getCurrentUser();
        Avatar avatar = CatAvatar.newAvatarBuilder().build();
        BufferedImage bufferedImage = avatar.create(user.getId());
        // 直接输出头像图片，避免额外封装
        try {
            ImageIO.write(bufferedImage, "png", response.getOutputStream());
        } catch (IOException e) {
            log.error("load my avatar error", e);
            throw new BaseException(B_IMAGE_LOAD_ERROR);
        }
    }

    /**
     * 获取指定用户头像。
     *
     * @param uuid 用户 UUID
     * @param width 头像宽度
     * @param height 头像高度
     * @param response 响应对象
     */
    @Operation(summary = "用户头像")
    @GetMapping(value = "/avatar/{uuid}", produces = MediaType.IMAGE_JPEG_VALUE)
    public void avatar(@Validated @PathVariable String uuid, @RequestParam(defaultValue = "64") @Min(32) @Max(128) Integer width, @RequestParam(defaultValue = "64") @Min(32) @Max(128) Integer height, HttpServletResponse response) {
        User user = userService.getByUuid(uuid);
        long userId = 0;
        if (null != user) {
            userId = user.getId();
        }
        try {
            writeToResponse(userId, width, height, response);
        } catch (IOException e) {
            log.error("load avatar error", e);
            throw new BaseException(B_IMAGE_LOAD_ERROR);
        }
    }

    /**
     * 将头像写入响应流，并设置缓存头。
     *
     * @param userId 用户 ID
     * @param width 头像宽度
     * @param height 头像高度
     * @param response 响应对象
     * @throws IOException 输出流异常
     */
    private synchronized void writeToResponse(Long userId, Integer width, Integer height, HttpServletResponse response) throws IOException {
        // 设置长缓存，头像生成规则稳定，可减少重复生成成本
        response.setHeader("Cache-Control", "max-age=" + 3600 * 24 * 365);
        Avatar avatar = CatAvatar.newAvatarBuilder().size(width, height).build();
        BufferedImage bufferedImage = avatar.create(userId);
        ImageIO.write(bufferedImage, "png", response.getOutputStream());
    }
}
