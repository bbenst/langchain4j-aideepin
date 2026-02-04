package com.moyz.adi.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.DrawCommentDto;
import com.moyz.adi.common.service.DrawCommentService;
import com.moyz.adi.common.service.DrawService;
import com.moyz.adi.common.vo.DrawCommentAddReq;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
/**
 * 绘图评论相关接口控制器。
 */
@RestController
@RequestMapping("/draw/comment")
@Validated
public class DrawCommentController {

    /**
     * 绘图服务，提供评论相关能力。
     */
    @Resource
    private DrawService drawService;

    /**
     * 评论服务，负责评论的删除等操作。
     */
    @Resource
    private DrawCommentService drawCommentService;

    /**
     * 分页查询指定绘图任务的评论。
     *
     * @param uuid 绘图任务 UUID
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 评论分页结果
     */
    @GetMapping("/list/{uuid}")
    public Page<DrawCommentDto> listByPage(@PathVariable String uuid, @RequestParam(defaultValue = "1") Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return drawService.listCommentsByPage(uuid, currentPage, pageSize);
    }

    /**
     * 添加评论。
     *
     * @param commentAddReq 评论新增请求
     * @return 新增后的评论
     */
    @PostMapping("/add")
    public DrawCommentDto save(@RequestBody DrawCommentAddReq commentAddReq) {
        return drawService.addComment(commentAddReq.getDrawUuid(), commentAddReq.getComment());
    }

    /**
     * 删除指定评论（逻辑删除）。
     *
     * @param id 评论 ID
     * @return 是否删除成功
     */
    @PostMapping("/del")
    public boolean del(@RequestParam Long id) {
        return drawCommentService.softDel(id);
    }
}
