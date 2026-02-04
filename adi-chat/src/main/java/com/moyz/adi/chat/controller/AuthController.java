package com.moyz.adi.chat.controller;

import com.moyz.adi.common.config.AdiProperties;
import com.moyz.adi.common.dto.LoginReq;
import com.moyz.adi.common.dto.LoginResp;
import com.moyz.adi.common.dto.RegisterReq;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.searchengine.SearchEngineServiceContext;
import com.moyz.adi.common.service.UserService;
import com.moyz.adi.common.vo.SearchEngineInfo;
import com.ramostear.captcha.HappyCaptcha;
import com.ramostear.captcha.support.CaptchaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;

import static com.moyz.adi.common.enums.ErrorEnum.B_ACTIVE_USER_ERROR;
import static com.moyz.adi.common.enums.ErrorEnum.B_RESET_PASSWORD_ERROR;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;


/**
 * 权限与认证相关的接口控制器。
 */
@Slf4j
@Tag(name = "权限controller", description = "权限controller")
@Validated
@RestController
@RequestMapping("auth")
public class AuthController {

    /**
     * 应用配置属性。
     */
    @Resource
    private AdiProperties adiProperties;

    /**
     * 用户领域服务。
     */
    @Resource
    private UserService userService;

    /**
     * 注册账号并发送激活链接。
     *
     * @param registerReq 注册请求信息
     * @return 提示文案
     */
    @Operation(summary = "注册")
    @PostMapping(value = "/register", produces = MediaType.TEXT_PLAIN_VALUE)
    public String register(@RequestBody RegisterReq registerReq) {
        userService.register(registerReq.getEmail(), registerReq.getPassword(), registerReq.getCaptchaId(), registerReq.getCaptchaCode());
        return "激活链接已经发送到邮箱，请登录邮箱进行激活";
    }

    /**
     * 生成注册验证码图片并缓存验证码值。
     *
     * @param captchaId 验证码ID
     * @param request 请求对象
     * @param response 响应对象
     */
    @Operation(summary = "注册的验证码")
    @GetMapping("/register/captcha")
    public void registerCaptcha(@Parameter(description = "验证码ID") @RequestParam @Length(min = 32) String captchaId,
                                HttpServletRequest request,
                                HttpServletResponse response) {
        HappyCaptcha happyCaptcha = HappyCaptcha.require(request, response).type(CaptchaType.WORD_NUMBER_UPPER).build().finish();
        String captchaCode = happyCaptcha.getCode();
        userService.cacheRegisterCaptcha(captchaId, captchaCode);
        happyCaptcha.output();
    }

    /**
     * 根据激活码激活账号并重定向到前端提示页。
     *
     * @param activeCode 激活码
     * @param response 响应对象
     * @return 激活流程完成标记
     */
    @Operation(summary = "激活")
    @GetMapping("active")
    public boolean active(@RequestParam("code") String activeCode, HttpServletResponse response) {
        // 使用重定向统一前端提示页，异常时回退到失败信息。
        try {
            userService.active(activeCode);
            response.sendRedirect(adiProperties.getFrontendUrl() + "/#/active?active=success&msg=" + URLEncoder.encode("激活成功，请登录", Charset.defaultCharset()));
        } catch (IOException e) {
            log.error("auth.active1:", e);
            try {
                response.sendRedirect(adiProperties.getFrontendUrl() + "/#/active?active=fail&msg=" + URLEncoder.encode("激活失败：系统错误，请重新注册或者登录", Charset.defaultCharset()));
            } catch (IOException ex) {
                log.error("auth.active2:", ex);
                throw new BaseException(B_ACTIVE_USER_ERROR);
            }
        } catch (Exception e) {
            try {
                response.sendRedirect(adiProperties.getFrontendUrl() + "/#/active?active=fail&msg=" + URLEncoder.encode(e.getMessage(), Charset.defaultCharset()));
            } catch (IOException ex) {
                log.error("auth.active3:", ex);
                throw new BaseException(B_ACTIVE_USER_ERROR);
            }
        }
        return true;
    }

    /**
     * 触发忘记密码流程并发送重置链接。
     *
     * @param email 用户邮箱
     * @return 提示文案
     */
    @Operation(summary = "忘记密码")
    @PostMapping("password/forgot")
    public String forgotPassword(@RequestParam @NotBlank String email) {
        userService.forgotPassword(email);
        return "重置密码链接已发送";
    }

    /**
     * 使用重置码重置密码并跳转到前端提示页。
     *
     * @param code 重置码
     * @param response 响应对象
     */
    @Operation(summary = "重置密码")
    @GetMapping("/password/reset")
    public void resetPassword(@RequestParam @NotBlank String code, HttpServletResponse response) {
        userService.resetPassword(code);
        try {
            response.sendRedirect(adiProperties.getFrontendUrl() + "/#/active?active=success&msg=" + URLEncoder.encode("密码已经重置", Charset.defaultCharset()));
        } catch (IOException e) {
            log.error("resetPassword:", e);
            throw new BaseException(B_RESET_PASSWORD_ERROR);
        }
    }

    /**
     * 用户登录并返回登录信息。
     *
     * @param loginReq 登录请求
     * @param response 响应对象
     * @return 登录响应
     */
    @Operation(summary = "登录")
    @PostMapping("login")
    public LoginResp login(@Validated @RequestBody LoginReq loginReq, HttpServletResponse response) {
        LoginResp loginResp = userService.login(loginReq);
        response.setHeader(AUTHORIZATION, loginResp.getToken());
        Cookie cookie = new Cookie(AUTHORIZATION, loginResp.getToken());
        response.addCookie(cookie);
        return loginResp;
    }

    /**
     * 生成登录验证码图片并缓存验证码值。
     *
     * @param captchaId 验证码ID
     * @param request 请求对象
     * @param response 响应对象
     */
    @Operation(summary = "获取登录验证码")
    @GetMapping("/login/captcha")
    public void captcha(@RequestParam @Length(min = 32) String captchaId, HttpServletRequest request, HttpServletResponse response) {
        HappyCaptcha happyCaptcha = HappyCaptcha.require(request, response).type(CaptchaType.WORD_NUMBER_UPPER).build().finish();
        String captchaCode = happyCaptcha.getCode();
        userService.cacheLoginCaptcha(captchaId, captchaCode);
        happyCaptcha.output();
    }

    /**
     * 查询已注册的搜索引擎列表。
     *
     * @return 搜索引擎信息列表
     */
    @Operation(summary = "Search engine list")
    @GetMapping(value = "/search-engine/list")
    public List<SearchEngineInfo> engines() {
        return SearchEngineServiceContext.getAllService().values().stream().map(item -> {
            SearchEngineInfo info = new SearchEngineInfo();
            info.setEnable(item.isEnabled());
            info.setName(item.getEngineName());
            return info;
        }).toList();
    }
}
