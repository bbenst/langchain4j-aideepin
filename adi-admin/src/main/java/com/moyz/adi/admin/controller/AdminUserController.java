package com.moyz.adi.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.moyz.adi.common.dto.UserAddReq;
import com.moyz.adi.common.dto.UserEditReq;
import com.moyz.adi.common.dto.UserInfoDto;
import com.moyz.adi.common.dto.UserSearchReq;
import com.moyz.adi.common.entity.User;
import com.moyz.adi.common.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口控制器。
 */
@RestController
@RequestMapping("/admin/user")
@Validated
public class AdminUserController {

    /**
     * 用户服务，负责后台用户管理。
     */
    @Resource
    private UserService userService;

    /**
     * 搜索用户并分页返回。
     *
     * @param userSearchReq 搜索条件
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 用户分页结果
     */
    @PostMapping("/search")
    public Page<UserInfoDto> search(@RequestBody UserSearchReq userSearchReq, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return userService.search(userSearchReq, currentPage, pageSize);
    }

    /**
     * 查询用户详情。
     *
     * @param uuid 用户 UUID
     * @return 用户信息
     */
    @Operation(summary = "用户信息")
    @GetMapping("/info/{uuid}")
    public UserInfoDto info(@PathVariable String uuid) {
        User user = userService.getByUuidOrThrow(uuid);
        UserInfoDto result = new UserInfoDto();
        BeanUtils.copyProperties(user, result);
        return result;
    }

    /**
     * 新增用户。
     *
     * @param addUserReq 新增请求
     * @return 新增后的用户信息
     */
    @PostMapping("/addOne")
    public UserInfoDto addOne(@Validated @RequestBody UserAddReq addUserReq) {
        return userService.addUser(addUserReq);
    }

    /**
     * 通过 UUID 激活用户。
     *
     * @param uuid 用户 UUID
     */
    @PostMapping("/active/{uuid}")
    public void activeByUuid(@PathVariable String uuid) {
        userService.activeByUuid(uuid);
    }

    /**
     * 通过 UUID 冻结用户。
     *
     * @param uuid 用户 UUID
     */
    @PostMapping("/freeze/{uuid}")
    public void freezeByUuid(@PathVariable String uuid) {
        userService.freeze(uuid);
    }

    /**
     * 编辑用户信息。
     *
     * @param userEditReq 编辑请求
     */
    @PostMapping("/edit")
    public void editUser(@Validated @RequestBody UserEditReq userEditReq) {
        userService.editUser(userEditReq);
    }
}
