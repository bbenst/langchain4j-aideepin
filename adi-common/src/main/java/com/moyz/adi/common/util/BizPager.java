package com.moyz.adi.common.util;

import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.entity.BaseEntity;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;
/**
 * 业务分页遍历工具类。
 */
public class BizPager {

    /**
     * 工具类禁止实例化。
     */
    private BizPager(){}

    /**
     * 逐行获取数据。
     * <br/>以 Long 类型的唯一字段（通常为 id）为锚点，批量查询后台数据库，基于 MyBatis-Plus。
     * <br/>回调函数传值为单行数据。
     *
     * @param queryWrapper 查询条件
     * @param service      业务 Service
     * @param idSupplier   锚点字段
     * @param consumer     回调
     * @param <T>          数据表对应的实体类
     */
    public static <T extends BaseEntity> void oneByOneWithAnchor(LambdaQueryWrapper<T> queryWrapper, IService<T> service, SFunction<T, Long> idSupplier, Consumer<T> consumer) {
        long minId = 0;
        List<T> records;
        do {
            queryWrapper.gt(idSupplier, minId);
            queryWrapper.orderByAsc(idSupplier);
            queryWrapper.last("limit " + AdiConstant.DEFAULT_PAGE_SIZE);
            records = service.list(queryWrapper);
            if (CollectionUtils.isNotEmpty(records)) {
                minId = records.stream().map(idSupplier).reduce(Long::max).get();
                for (T t : records) {
                    consumer.accept(t);
                }
            }
        } while (!Thread.currentThread().isInterrupted() && records.size() == AdiConstant.DEFAULT_PAGE_SIZE);
    }

    /**
     * 按页获取数据。
     * <br/>以 Long 类型的唯一字段（通常为 id）为锚点，批量查询后台数据库，基于 MyBatis-Plus。
     * <br/>回调函数传值为列表。
     *
     * @param queryWrapper 查询条件
     * @param service      业务 Service
     * @param idSupplier   锚点字段
     * @param consumer     回调
     * @param <T>          数据表对应的实体类
     */
    public static <T extends BaseEntity> void batchWithAnchor(LambdaQueryWrapper<T> queryWrapper, IService<T> service, SFunction<T, Long> idSupplier, Consumer<List<T>> consumer) {
        long minId = 0;
        List<T> records;
        do {
            queryWrapper.gt(idSupplier, minId);
            queryWrapper.orderByAsc(idSupplier);
            queryWrapper.last("limit " + AdiConstant.DEFAULT_PAGE_SIZE);
            records = service.list(queryWrapper);
            if (CollectionUtils.isNotEmpty(records)) {
                minId = records.stream().map(idSupplier).reduce(Long::max).get();
                consumer.accept(records);
            }
        } while (!Thread.currentThread().isInterrupted() && records.size() == AdiConstant.DEFAULT_PAGE_SIZE);
    }
    /**
     * 按最大 ID 查询一页数据，并返回最小 ID。
     *
     * @param maxId        最大 ID（小于该值）
     * @param queryWrapper 查询条件
     * @param service      业务 Service
     * @param idSupplier   锚点字段
     * @param consumer     回调，参数为列表与最小 ID
     * @param <T>          数据表对应的实体类
     */
    public static <T extends BaseEntity> void listByMaxId(Long maxId, LambdaQueryWrapper<T> queryWrapper, IService<T> service, SFunction<T, Long> idSupplier, ObjLongConsumer<List<T>> consumer) {
        if (maxId > 0) {
            queryWrapper.lt(idSupplier, maxId);
        }
        queryWrapper.orderByDesc(idSupplier);
        queryWrapper.last("limit " + AdiConstant.DEFAULT_PAGE_SIZE);

        long minId = 0;
        List<T> records = service.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(records)) {
            minId = records.stream().map(idSupplier).reduce(Long::min).get();
        }
        consumer.accept(records, minId);
    }

    /**
     * 以 Long 类型的唯一字段（通常为 id）为锚点，按页获取数据。
     * <br/>不依赖 MyBatis-Plus。
     *
     * @param supplier   每页查询的数据提供者
     * @param consumer   回调函数
     * @param idSupplier ID 字段提供者
     * @param asc        是否升序
     * @param <T>        数据类型
     */
    public static <T> void batchWithAnchor(Supplier<List<T>> supplier, ObjLongConsumer<List<T>> consumer, SFunction<T, Long> idSupplier, boolean asc) {
        long anchorId;
        List<T> records;
        do {
            records = supplier.get();
            if (CollectionUtils.isNotEmpty(records)) {
                if (asc) {
                    anchorId = records.stream().map(idSupplier).reduce(Long::max).get();
                } else {
                    anchorId = records.stream().map(idSupplier).reduce(Long::min).get();
                }
                consumer.accept(records, anchorId);
            }
        } while (!Thread.currentThread().isInterrupted() && records.size() == AdiConstant.DEFAULT_PAGE_SIZE);
    }
}
