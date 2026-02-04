package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import com.moyz.adi.common.dto.mcp.McpAddOrEditReq;
import com.moyz.adi.common.dto.mcp.McpCommonParam;
import com.moyz.adi.common.dto.mcp.McpSearchReq;
import com.moyz.adi.common.entity.Mcp;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.mapper.McpMapper;
import com.moyz.adi.common.util.AesUtil;
import com.moyz.adi.common.util.PrivilegeUtil;
import com.moyz.adi.common.util.UuidUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.moyz.adi.common.enums.ErrorEnum.A_MCP_SERVER_NOT_FOUND;

/**
 * MCP 服务类，MCP 信息只能由系统管理员进行维护。
 */
@Service
public class McpService extends ServiceImpl<McpMapper, Mcp> {

    /**
     * 新增或更新 MCP 服务。
     *
     * @param addOrEditReq 请求参数
     * @param decryptEnv   是否解密环境变量
     * @return MCP 实体
     */
    public Mcp addOrUpdate(McpAddOrEditReq addOrEditReq, boolean decryptEnv) {
        Mcp result;
        if (StringUtils.isBlank(addOrEditReq.getUuid())) {
            Mcp mcp = new Mcp();
            BeanUtils.copyProperties(addOrEditReq, mcp);
            mcp.setUuid(UuidUtil.createShort());
            encryptEnv(mcp.getPresetParams());
            this.save(mcp);
            result = mcp;
        } else {
            Mcp mcp = PrivilegeUtil.checkAndGetByUuid(addOrEditReq.getUuid(), this.query(), A_MCP_SERVER_NOT_FOUND);
            Mcp updateObj = new Mcp();
            BeanUtils.copyProperties(addOrEditReq, updateObj, "id", "uuid");
            encryptEnv(updateObj.getPresetParams());
            updateObj.setId(mcp.getId());
            this.updateById(updateObj);
            result = updateObj;
        }
        return decryptEnv(result, decryptEnv);
    }

    /**
     * 分页搜索 MCP 服务。
     *
     * @param req         查询条件
     * @param currentPage 当前页
     * @param pageSize    页大小
     * @param decryptEnv  是否解密环境变量
     * @return 分页结果
     */
    public Page<Mcp> search(McpSearchReq req, Integer currentPage, Integer pageSize, boolean decryptEnv) {
        Page<Mcp> page = this.lambdaQuery()
                .like(StringUtils.isNotBlank(req.getTitle()), Mcp::getTitle, req.getTitle())
                .eq(StringUtils.isNotBlank(req.getInstallType()), Mcp::getInstallType, req.getInstallType())
                .eq(StringUtils.isNotBlank(req.getTransportType()), Mcp::getTransportType, req.getTransportType())
                .eq(null != req.getIsEnable(), Mcp::getIsEnable, req.getIsEnable())
                .orderByDesc(Mcp::getUpdateTime)
                .page(new Page<>(currentPage, pageSize));
        for (Mcp mcp : page.getRecords()) {
            decryptEnv(mcp, decryptEnv);
        }
        return page;
    }

    /**
     * 根据 ID 列表查询 MCP 服务。
     *
     * @param ids        ID 列表
     * @param decryptEnv 是否解密环境变量
     * @return MCP 列表
     */
    public List<Mcp> listByIds(List<Long> ids, boolean decryptEnv) {
        List<Mcp> list = this.lambdaQuery()
                .in(Mcp::getId, ids)
                .eq(Mcp::getIsDeleted, false)
                .list();
        for (Mcp mcp : list) {
            decryptEnv(mcp, decryptEnv);
        }
        return list;
    }

    /**
     * 启用或禁用 MCP 服务。
     *
     * @param uuid     MCP UUID
     * @param isEnable 是否启用
     * @return 更新行数
     */
    public int enable(String uuid, Boolean isEnable) {
        Mcp existObj = PrivilegeUtil.checkAndGetByUuid(uuid, this.query(), A_MCP_SERVER_NOT_FOUND);
        Mcp updateObj = new Mcp();
        updateObj.setId(existObj.getId());
        updateObj.setIsEnable(isEnable);
        return baseMapper.updateById(updateObj);
    }

    /**
     * 软删除 MCP 服务。
     *
     * @param uuid MCP UUID
     */
    public void softDelete(String uuid) {
        PrivilegeUtil.checkAndDelete(uuid, this.query(), this.update(), A_MCP_SERVER_NOT_FOUND);
    }

    /**
     * 根据 ID 获取 MCP 服务，不存在则抛异常。
     *
     * @param id         MCP ID
     * @param decryptEnv 是否解密环境变量
     * @return MCP 实体
     */
    public Mcp getOrThrow(Long id, boolean decryptEnv) {
        Mcp mcp = ChainWrappers.lambdaQueryChain(baseMapper).eq(Mcp::getId, id)
                .eq(Mcp::getIsDeleted, false)
                .oneOpt()
                .orElseThrow(() -> new BaseException(A_MCP_SERVER_NOT_FOUND));
        return decryptEnv(mcp, decryptEnv);
    }

    /**
     * 加密环境变量。
     *
     * @param env 参数列表
     */
    private void encryptEnv(List<McpCommonParam> env) {
        if (env != null && !env.isEmpty()) {
            for (McpCommonParam e : env) {
                if (Boolean.TRUE.equals(e.getRequireEncrypt()) && e.getValue() != null) {
                    e.setValue(AesUtil.encrypt(String.valueOf(e.getValue())));
                    e.setEncrypted(true);
                }
            }
        }
    }

    /**
     * 解密环境变量，非管理员则将该变量替换为"***"。
     *
     * @param mcp        mcp对象
     * @param decryptEnv 是否需要解密环境变量
     * @return 解密后的mcp对象
     */
    private Mcp decryptEnv(Mcp mcp, boolean decryptEnv) {
        if (mcp.getPresetParams() != null && !mcp.getPresetParams().isEmpty()) {
            for (McpCommonParam e : mcp.getPresetParams()) {
                if (decryptEnv) {
                    //已经加密的内容需要解密
                    if (Boolean.TRUE.equals(e.getEncrypted()) && e.getValue() != null) {
                        e.setValue(AesUtil.decrypt(String.valueOf(e.getValue())));
                    }
                } else {
                    e.setValue("***");
                }
            }
        }
        return mcp;
    }
}
