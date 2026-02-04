package com.moyz.adi.chat.controller;

import com.moyz.adi.common.dto.DrawDto;
import com.moyz.adi.common.dto.DrawListResp;
import com.moyz.adi.common.service.DrawService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
/**
 * 绘图收藏相关接口控制器。
 */
@RestController
@RequestMapping("/draw/star")
@Validated
public class DrawStarController {

    /**
     * 绘图服务，负责收藏列表与收藏状态更新。
     */
    @Resource
    private DrawService drawService;

    /**
     * 获取当前用户收藏的绘图任务列表。
     *
     * @param maxId 游标 ID
     * @param pageSize 每页数量
     * @return 收藏列表
     */
    @GetMapping("/mine")
    public DrawListResp myStars(@RequestParam Long maxId, @RequestParam int pageSize) {
        return drawService.listStarred(maxId, pageSize);
    }

    /**
     * 切换绘图任务的收藏状态。
     *
     * @param uuid 绘图任务 UUID
     * @return 更新后的绘图任务信息
     */
    @Operation(summary = "将绘图任务设置为公开或私有")
    @PostMapping("/toggle/{uuid}")
    public DrawDto star(@PathVariable @NotBlank String uuid) {
        return drawService.toggleStar(uuid);
    }
}
