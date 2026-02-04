package com.moyz.adi.common.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
/**
 * MyBatis-Plus 分页与对象转换工具类。
 */
@Slf4j
public class MPPageUtil {

    /**
     * 工具类禁止实例化。
     */
    private MPPageUtil(){}
    /**
     * 将分页记录转换为指定类型分页。
     *
     * @param source            源分页
     * @param targetRecordClass 目标记录类型
     * @param <T>               源类型
     * @param <U>               目标类型
     * @return 转换后的分页
     */
    public static <T, U> Page<U> convertToPage(Page<T> source, Class<U> targetRecordClass) {
        return MPPageUtil.convertToPage(source, new Page<>(), targetRecordClass, null);
    }
    /**
     * 将分页记录转换为指定类型分页，并复用目标分页对象。
     *
     * @param source            源分页
     * @param target            目标分页对象
     * @param targetRecordClass 目标记录类型
     * @param <T>               源类型
     * @param <U>               目标类型
     * @return 转换后的分页
     */
    public static <T, U> Page<U> convertToPage(Page<T> source, Page<U> target, Class<U> targetRecordClass) {
        return MPPageUtil.convertToPage(source, target, targetRecordClass, null);
    }
    /**
     * 将分页记录转换为指定类型分页，并支持自定义转换逻辑。
     *
     * @param source            源分页
     * @param target            目标分页对象
     * @param targetRecordClass 目标记录类型
     * @param biFunction        自定义转换逻辑
     * @param <T>               源类型
     * @param <U>               目标类型
     * @return 转换后的分页
     */
    public static <T, U> Page<U> convertToPage(Page<T> source, Page<U> target, Class<U> targetRecordClass, BiFunction<T, U, U> biFunction) {
        BeanUtils.copyProperties(source, target);
        List<U> records = new ArrayList<>();
        target.setRecords(records);
        try {
            for (T t : source.getRecords()) {
                U u = targetRecordClass.getDeclaredConstructor().newInstance();
                BeanUtils.copyProperties(t, u);
                if (null != biFunction) {
                    biFunction.apply(t, u);
                }
                records.add(u);
            }
        } catch (NoSuchMethodException e1) {
            log.error("convertTo error1", e1);
        } catch (Exception e2) {
            log.error("convertTo error2", e2);
        }

        return target;
    }
    /**
     * 将列表转换为指定类型列表。
     *
     * @param source            源列表
     * @param targetRecordClass 目标类型
     * @param <T>               源类型
     * @param <U>               目标类型
     * @return 转换后的列表
     */
    public static <T, U> List<U> convertToList(List<T> source, Class<U> targetRecordClass) {
        return convertToList(source, targetRecordClass, null);
    }
    /**
     * 将列表转换为指定类型列表，并支持自定义转换逻辑。
     *
     * @param source            源列表
     * @param targetRecordClass 目标类型
     * @param biFunction        自定义转换逻辑
     * @param <T>               源类型
     * @param <U>               目标类型
     * @return 转换后的列表
     */
    public static <T, U> List<U> convertToList(List<T> source, Class<U> targetRecordClass, BiFunction<T, U, U> biFunction) {
        if (CollectionUtils.isEmpty(source)) {
            return Collections.emptyList();
        }
        List<U> result = new ArrayList<>();
        for (T t : source) {
            try {
                U u = targetRecordClass.getDeclaredConstructor().newInstance();
                BeanUtils.copyProperties(t, u);
                if (null != biFunction) {
                    biFunction.apply(t, u);
                }
                result.add(u);
            } catch (NoSuchMethodException e1) {
                log.error("convertTo error1", e1);
            } catch (Exception e2) {
                log.error("convertTo error2", e2);
            }
        }
        return result;
    }
    /**
     * 将对象转换为指定类型对象。
     *
     * @param source      源对象
     * @param targetClass 目标类型
     * @param <T>         源类型
     * @param <U>         目标类型
     * @return 转换后的对象
     */
    public static <T, U> U convertTo(T source, Class<U> targetClass) {
        try {
            U target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
