package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.moyz.adi.common.dto.AiModelDto;
import com.moyz.adi.common.dto.AiModelSearchReq;
import com.moyz.adi.common.entity.AiModel;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.mapper.AiModelMapper;
import com.moyz.adi.common.util.MPPageUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 模型管理服务。
 */
@Slf4j
@Service
public class AiModelService extends ServiceImpl<AiModelMapper, AiModel> {

    /**
     * 模型初始化器。
     */
    @Resource
    private AiModelInitializer aiModelInitializer;

    /**
     * 初始化模型缓存与服务。
     */
    public void init() {
        log.info("Initializing AI model...");
        List<AiModel> aiModels = ChainWrappers.lambdaQueryChain(baseMapper).eq(AiModel::getIsDeleted, false).list();
        aiModelInitializer.init(aiModels);
    }

    /**
     * 按平台与类型查询模型列表。
     *
     * @param platform 平台
     * @param type     模型类型
     * @return 模型列表
     */
    public List<AiModel> listBy(String platform, String type) {
        return ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(AiModel::getPlatform, platform)
                .eq(AiModel::getType, type)
                .eq(AiModel::getIsDeleted, false)
                .list();
    }

    /**
     * 按模型名称查询模型。
     *
     * @param modelName 模型名称
     * @return 模型实体
     */
    public AiModel getByName(String modelName) {
        return ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(AiModel::getName, modelName)
                .eq(AiModel::getIsDeleted, false)
                .one();
    }

    /**
     * 按模型名称查询模型，不存在则抛异常。
     *
     * @param modelName 模型名称
     * @return 模型实体
     */
    public AiModel getByNameOrThrow(String modelName) {
        AiModel aiModel = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(AiModel::getName, modelName)
                .eq(AiModel::getIsDeleted, false)
                .one();
        if (null == aiModel) {
            throw new BaseException(ErrorEnum.A_MODEL_NOT_FOUND);
        }
        return aiModel;
    }

    /**
     * 按名称获取模型 ID。
     *
     * @param modelName 模型名称
     * @return 模型 ID
     */
    public Long getIdByName(String modelName) {
        AiModel aiModel = this.getByName(modelName);
        return null == aiModel ? 0L : aiModel.getId();
    }

    /**
     * 按 ID 查询模型，不存在则抛异常。
     *
     * @param id 模型 ID
     * @return 模型实体
     */
    public AiModel getByIdOrThrow(Long id) {
        AiModel existModel = baseMapper.selectById(id);
        if (null == existModel) {
            throw new BaseException(ErrorEnum.A_MODEL_NOT_FOUND);
        }
        return existModel;
    }

    /**
     * 分页查询模型列表。
     *
     * @param aiModelSearchReq 查询条件
     * @param currentPage      当前页
     * @param pageSize         页大小
     * @return 分页结果
     */
    public Page<AiModelDto> search(AiModelSearchReq aiModelSearchReq, Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<AiModel> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(aiModelSearchReq.getPlatform())) {
            lambdaQueryWrapper.eq(AiModel::getPlatform, aiModelSearchReq.getPlatform());
        }
        if (StringUtils.isNotBlank(aiModelSearchReq.getType())) {
            lambdaQueryWrapper.eq(AiModel::getType, aiModelSearchReq.getType());
        }
        if (null != aiModelSearchReq.getIsEnable()) {
            lambdaQueryWrapper.eq(AiModel::getIsEnable, aiModelSearchReq.getIsEnable());
        }
        lambdaQueryWrapper.eq(AiModel::getIsDeleted, false);
        lambdaQueryWrapper.orderByDesc(AiModel::getUpdateTime);
        Page<AiModel> aiModelPage = baseMapper.selectPage(new Page<>(currentPage, pageSize), lambdaQueryWrapper);
        return MPPageUtil.convertToPage(aiModelPage, new Page<>(), AiModelDto.class);
    }

    /**
     * 禁用模型。
     *
     * @param id 模型 ID
     */
    public void disable(Long id) {
        AiModel model = new AiModel();
        model.setId(id);
        model.setIsEnable(false);
        baseMapper.updateById(model);
    }

    /**
     * 启用模型。
     *
     * @param id 模型 ID
     */
    public void enable(Long id) {
        AiModel model = new AiModel();
        model.setId(id);
        model.setIsEnable(true);
        baseMapper.updateById(model);
    }

    /**
     * 查询启用的模型列表。
     *
     * @return 模型 DTO 列表
     */
    public List<AiModelDto> listEnable() {
        List<AiModel> aiModels = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(AiModel::getIsEnable, true)
                .eq(AiModel::getIsDeleted, false)
                .list();
        return MPPageUtil.convertToList(aiModels, AiModelDto.class);
    }

    /**
     * 新增模型。
     *
     * @param aiModelDto 模型信息
     * @return 新增后的模型信息
     */
    public AiModelDto addOne(AiModelDto aiModelDto) {
        Long count = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(AiModel::getName, aiModelDto.getName())
                .eq(AiModel::getPlatform, aiModelDto.getPlatform())
                .eq(AiModel::getIsDeleted, false)
                .count();
        if (count > 0) {
            throw new BaseException(ErrorEnum.A_MODEL_ALREADY_EXIST);
        }
        AiModel aiModel = new AiModel();
        aiModelDto.setName(aiModelDto.getName().strip());
        aiModelDto.setTitle(aiModelDto.getTitle().strip());
        BeanUtils.copyProperties(aiModelDto, aiModel);
        baseMapper.insert(aiModel);

        AiModelDto result = new AiModelDto();
        BeanUtils.copyProperties(aiModel, result);

        aiModelInitializer.addOrUpdate(aiModel);

        return result;
    }

    /**
     * 编辑模型信息。
     *
     * @param aiModelDto 模型信息
     */
    public void edit(AiModelDto aiModelDto) {
        // 增加非空判断，防止部分更新（字段为null）时调用 strip() 报空指针异常
        if (StringUtils.isNotBlank(aiModelDto.getName())) {
            aiModelDto.setName(aiModelDto.getName().strip());
        }
        if (StringUtils.isNotBlank(aiModelDto.getTitle())) {
            aiModelDto.setTitle(aiModelDto.getTitle().strip());
        }

        AiModel oldAiModel = getByIdOrThrow(aiModelDto.getId());

        AiModel aiModel = new AiModel();
        // 复制属性，null 值也会被复制，但 MyBatis-Plus updateById 默认策略通常忽略 null 值（SELECTIVE）
        // 从而实现只更新 isFree 字段的效果
        BeanUtils.copyProperties(aiModelDto, aiModel, "createTime", "updateTime");
        baseMapper.updateById(aiModel);

        AiModel updatedOne = getByIdOrThrow(aiModelDto.getId());
        aiModelInitializer.delete(oldAiModel);
        aiModelInitializer.addOrUpdate(updatedOne);
    }

    /**
     * 软删除模型并清理缓存。
     *
     * @param id 模型 ID
     */
    public void softDelete(Long id) {
        AiModel existModel = getByIdOrThrow(id);

        AiModel model = new AiModel();
        model.setId(id);
        model.setIsDeleted(true);
        baseMapper.updateById(model);

        aiModelInitializer.delete(existModel);
    }
}
