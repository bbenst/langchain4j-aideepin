package com.moyz.adi.common.util;

import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.UpdateChainWrapper;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.entity.BaseEntity;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;

import static com.moyz.adi.common.cosntant.AdiConstant.*;
/**
 * 权限检查与资源获取工具类。
 */
public class PrivilegeUtil {
    /**
     * 工具类禁止实例化。
     */
    private PrivilegeUtil() {
    }
    /**
     * 校验权限并通过 ID 获取对象。
     *
     * @param id                      主键 ID
     * @param lambdaQueryChainWrapper 查询链
     * @param exceptionMessage        异常枚举
     * @param <T>                     目标类型
     * @return 目标对象
     */
    public static <T> T checkAndGetById(Long id, QueryChainWrapper<T> lambdaQueryChainWrapper, ErrorEnum exceptionMessage) {
        return checkAndGet(id, null, lambdaQueryChainWrapper, exceptionMessage);
    }
    /**
     * 校验权限并通过 UUID 获取对象。
     *
     * @param uuid                    UUID
     * @param lambdaQueryChainWrapper 查询链
     * @param exceptionMessage        异常枚举
     * @param <T>                     目标类型
     * @return 目标对象
     */
    public static <T> T checkAndGetByUuid(String uuid, QueryChainWrapper<T> lambdaQueryChainWrapper, ErrorEnum exceptionMessage) {
        return checkAndGet(null, uuid, lambdaQueryChainWrapper, exceptionMessage);
    }
    /**
     * 校验权限并获取对象。
     *
     * @param id                      主键 ID
     * @param uuid                    UUID
     * @param lambdaQueryChainWrapper 查询链
     * @param exceptionMessage        异常枚举
     * @param <T>                     目标类型
     * @return 目标对象
     */
    public static <T> T checkAndGet(Long id, String uuid, QueryChainWrapper<T> lambdaQueryChainWrapper, ErrorEnum exceptionMessage) {
        T target;
        if (Boolean.TRUE.equals(ThreadContext.getCurrentUser().getIsAdmin())) {
            target = lambdaQueryChainWrapper
                    .eq(null != id, COLUMN_NAME_ID, id)
                    .eq(null != uuid, COLUMN_NAME_UUID, uuid)
                    .eq(COLUMN_NAME_IS_DELETE, false).oneOpt()
                    .orElse(null);
        } else {
            target = lambdaQueryChainWrapper
                    .eq(null != id, COLUMN_NAME_ID, id)
                    .eq(null != uuid, COLUMN_NAME_UUID, uuid)
                    .eq(COLUMN_NAME_USER_ID, ThreadContext.getCurrentUserId())
                    .eq(COLUMN_NAME_IS_DELETE, false)
                    .oneOpt()
                    .orElse(null);
        }
        if (null == target) {
            throw new BaseException(exceptionMessage);
        }
        return target;
    }
    /**
     * 校验权限并软删除对象（按 UUID）。
     *
     * @param uuid                    UUID
     * @param lambdaQueryChainWrapper 查询链
     * @param updateChainWrapper      更新链
     * @param exceptionMessage        异常枚举
     * @param <T>                     目标类型
     * @return 被删除的对象
     */
    public static <T extends BaseEntity> T checkAndDelete(String uuid, QueryChainWrapper<T> lambdaQueryChainWrapper, UpdateChainWrapper<T> updateChainWrapper, ErrorEnum exceptionMessage) {
        return checkAndDelete(null, uuid, lambdaQueryChainWrapper, updateChainWrapper, exceptionMessage);
    }
    /**
     * 校验权限并软删除对象。
     *
     * @param id                      主键 ID
     * @param uuid                    UUID
     * @param lambdaQueryChainWrapper 查询链
     * @param updateChainWrapper      更新链
     * @param exceptionMessage        异常枚举
     * @param <T>                     目标类型
     * @return 被删除的对象
     */
    public static <T extends BaseEntity> T checkAndDelete(Long id, String uuid, QueryChainWrapper<T> lambdaQueryChainWrapper, UpdateChainWrapper<T> updateChainWrapper, ErrorEnum exceptionMessage) {
        T target = checkAndGet(id, uuid, lambdaQueryChainWrapper, exceptionMessage);
        updateChainWrapper.eq(COLUMN_NAME_ID, target.getId()).set(COLUMN_NAME_IS_DELETE, true).update();
        return target;
    }
}
