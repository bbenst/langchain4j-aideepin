package com.moyz.adi.common.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.moyz.adi.common.base.ThreadContext;
import com.moyz.adi.common.entity.AdiFile;
import com.moyz.adi.common.entity.User;
import com.moyz.adi.common.enums.ErrorEnum;
import com.moyz.adi.common.exception.BaseException;
import com.moyz.adi.common.file.FileOperatorContext;
import com.moyz.adi.common.file.LocalFileUtil;
import com.moyz.adi.common.mapper.FileMapper;
import com.moyz.adi.common.util.HashUtil;
import com.moyz.adi.common.util.UuidUtil;
import com.moyz.adi.common.vo.SaveRemoteImageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.moyz.adi.common.enums.ErrorEnum.A_AI_IMAGE_NO_AUTH;
import static com.moyz.adi.common.enums.ErrorEnum.A_FILE_NOT_EXIST;

/**
 * 文件与图片存储服务。
 */
@Slf4j
@Service
public class FileService extends ServiceImpl<FileMapper, AdiFile> {

    /**
     * 图片存储根路径。
     */
    @Value("${local.images}")
    private String imagePath;

    /**
     * 图片水印存储路径。
     */
    @Value("${local.watermark-images}")
    private String watermarkImagesPath;

    /**
     * 图片缩略图存储路径。
     */
    @Value("${local.thumbnails}")
    private String thumbnailsPath;

    /**
     * 水印缩略图存储路径。
     */
    @Value("${local.watermark-thumbnails}")
    private String watermarkThumbnailsPath;

    /**
     * 文件存储根路径。
     */
    @Value("${local.files}")
    private String filePath;

    /**
     * 临时图片存储路径。
     */
    @Value("${local.tmp-images}")
    private String tmpImagesPath;

    /**
     * 保存上传文件，若已存在则复用记录。
     *
     * @param file  上传文件
     * @param image 是否为图片
     * @return 文件记录
     */
    public AdiFile saveFile(MultipartFile file, boolean image) {
        String sha256 = HashUtil.sha256(file);
        Optional<AdiFile> existFile = this.lambdaQuery()
                .eq(AdiFile::getSha256, sha256)
                .eq(AdiFile::getIsDeleted, false)
                .last("limit 1")
                .oneOpt();
        if (existFile.isPresent()) {
            AdiFile adiFile = existFile.get();
            boolean exist = FileOperatorContext.checkIfExist(adiFile);
            if (exist) {
                return adiFile;
            } else {
                log.warn("文件不存在,删除记录以便后续重新生成,fileId:{},uuid:{},sha256:{}", adiFile.getId(), adiFile.getUuid(), adiFile.getSha256());
                this.lambdaUpdate().eq(AdiFile::getId, adiFile.getId()).set(AdiFile::getIsDeleted, true).update();
            }
        }
        String uuid = UuidUtil.createShort();
        Pair<String, String> originalFile = new FileOperatorContext().save(file, image, uuid);
        AdiFile adiFile = new AdiFile();
        adiFile.setName(file.getOriginalFilename());
        adiFile.setUuid(uuid);
        adiFile.setSha256(sha256);
        adiFile.setPath(originalFile.getLeft());
        adiFile.setExt(originalFile.getRight());
        adiFile.setUserId(ThreadContext.getCurrentUserId());
        adiFile.setStorageLocation(FileOperatorContext.getStorageLocation());
        this.getBaseMapper().insert(adiFile);
        return adiFile;
    }

    /**
     * 从远程图片地址保存图片。
     *
     * @param user           用户
     * @param sourceImageUrl 图片地址
     * @return 文件记录
     */
    public AdiFile saveImageFromUrl(User user, String sourceImageUrl) {
        log.info("saveImageFromUrl,sourceImageUrl:{}", sourceImageUrl);
        String uuid = UuidUtil.createShort();
        SaveRemoteImageResult saveResult = new FileOperatorContext().saveImageFromUrl(sourceImageUrl, uuid);
        AdiFile adiFile = new AdiFile();
        adiFile.setName(saveResult.getOriginalName());
        adiFile.setUuid(uuid);
        adiFile.setSha256(HashUtil.sha256(saveResult.getPathOrUrl()));
        adiFile.setPath(saveResult.getPathOrUrl());
        adiFile.setUserId(user.getId());
        adiFile.setExt(saveResult.getExt());
        adiFile.setStorageLocation(FileOperatorContext.getStorageLocation());
        this.getBaseMapper().insert(adiFile);
        return adiFile;
    }

    /**
     * 从本地路径保存文件记录。
     *
     * @param user      用户
     * @param pathOrUrl 本地路径或地址
     * @return 文件记录
     */
    public AdiFile saveFromPath(User user, String pathOrUrl) {
        log.info("saveImageFromPath,path:{}", pathOrUrl);
        Pair<String, String> nameAndExt = LocalFileUtil.getNameAndExt(pathOrUrl);
        String uuid = UuidUtil.createShort();
        AdiFile adiFile = new AdiFile();
        adiFile.setName(nameAndExt.getLeft());
        adiFile.setUuid(uuid);
        adiFile.setSha256(HashUtil.sha256(pathOrUrl));
        adiFile.setPath(pathOrUrl);
        adiFile.setUserId(user.getId());
        adiFile.setExt(nameAndExt.getRight());
        adiFile.setStorageLocation(FileOperatorContext.getStorageLocation());
        this.getBaseMapper().insert(adiFile);
        return adiFile;
    }

    /**
     * 软删除文件记录。
     *
     * @param uuid 文件 UUID
     * @return 是否更新成功
     */
    public boolean softDel(String uuid) {
        return this.lambdaUpdate()
                .eq(AdiFile::getUserId, ThreadContext.getCurrentUserId())
                .eq(AdiFile::getUuid, uuid)
                .set(AdiFile::getIsDeleted, true)
                .update();
    }

    /**
     * 删除文件实体并软删除记录。
     *
     * @param uuid 文件 UUID
     * @return 是否删除成功
     */
    public boolean removeFileAndSoftDel(String uuid) {
        AdiFile adiFile = this.lambdaQuery()
                .eq(AdiFile::getUserId, ThreadContext.getCurrentUserId())
                .eq(AdiFile::getUuid, uuid)
                .oneOpt()
                .orElse(null);
        if (null == adiFile) {
            return false;
        }
        FileOperatorContext.delete(adiFile);
        return this.softDel(uuid);
    }

    /**
     * 按 UUID 获取当前用户的文件记录。
     *
     * @param uuid 文件 UUID
     * @return 文件记录
     */
    public AdiFile getByUuid(String uuid) {
        return this.lambdaQuery()
                .eq(AdiFile::getUuid, uuid)
                .eq(AdiFile::getUserId, ThreadContext.getCurrentUserId())
                .oneOpt().orElse(null);
    }

    /**
     * 读取图片到 BufferedImage，管理员或图片拥有者才有权限查看。
     *
     * @param uuid      图片 UUID
     * @param thumbnail 是否读取缩略图
     * @return 图片内容
     */
    public BufferedImage readMyImage(String uuid, boolean thumbnail) {
        if (StringUtils.isBlank(ThreadContext.getToken())) {
            throw new BaseException(A_AI_IMAGE_NO_AUTH);
        }
        AdiFile adiFile = this.lambdaQuery()
                .eq(!ThreadContext.getCurrentUser().getIsAdmin(), AdiFile::getUserId, ThreadContext.getCurrentUserId())
                .eq(AdiFile::getUuid, uuid)
                .oneOpt().orElse(null);
        if (null == adiFile) {
            throw new BaseException(A_FILE_NOT_EXIST);
        }
        return LocalFileUtil.readLocalImage(adiFile, thumbnail, thumbnailsPath);
    }

    /**
     * 读取图片，不做用户权限校验。
     *
     * @param uuid      图片 UUID
     * @param thumbnail 是否读取缩略图
     * @return 图片内容
     */
    public BufferedImage readImage(String uuid, boolean thumbnail) {
        AdiFile adiFile = this.lambdaQuery()
                .eq(AdiFile::getUuid, uuid)
                .oneOpt().orElse(null);
        if (null == adiFile) {
            throw new BaseException(A_FILE_NOT_EXIST);
        }
        return LocalFileUtil.readLocalImage(adiFile, thumbnail, thumbnailsPath);
    }

    /**
     * 获取图片存储路径。
     *
     * @param uuid 图片 UUID
     * @return 图片路径
     */
    public String getImagePath(String uuid) {
        AdiFile adiFile = this.lambdaQuery()
                .eq(AdiFile::getUuid, uuid)
                .oneOpt().orElse(null);
        if (null == adiFile) {
            throw new BaseException(ErrorEnum.A_AI_IMAGE_NOT_FOUND);
        }
        return adiFile.getPath();
    }


    /**
     * 获取文件记录。
     *
     * @param uuid 文件 UUID
     * @return 文件记录
     */
    public AdiFile getFile(String uuid) {
        AdiFile adiFile = this.lambdaQuery()
                .eq(AdiFile::getUuid, uuid)
                .oneOpt().orElse(null);
        if (null == adiFile) {
            throw new BaseException(ErrorEnum.A_AI_IMAGE_NOT_FOUND);
        }
        return adiFile;
    }

    /**
     * 获取临时图片路径。
     *
     * @param uuid 图片 UUID
     * @return 临时图片路径
     */
    public String getTmpImagesPath(String uuid) {
        AdiFile adiFile = this.lambdaQuery()
                .eq(AdiFile::getUuid, uuid)
                .oneOpt().orElse(null);
        if (null == adiFile) {
            throw new BaseException(ErrorEnum.A_AI_IMAGE_NOT_FOUND);
        }
        return tmpImagesPath + uuid + "." + adiFile.getExt();
    }

    /**
     * 获取水印图片路径。
     *
     * @param uuid 图片 UUID
     * @return 水印图片路径
     */
    public String getWatermarkImagesPath(String uuid) {
        AdiFile adiFile = this.lambdaQuery()
                .eq(AdiFile::getUuid, uuid)
                .oneOpt().orElse(null);
        if (null == adiFile) {
            throw new BaseException(ErrorEnum.A_AI_IMAGE_NOT_FOUND);
        }
        return watermarkImagesPath + uuid + "." + adiFile.getExt();
    }

    /**
     * 获取水印图片路径（已存在文件对象）。
     *
     * @param adiFile 文件对象
     * @return 水印图片路径
     */
    public String getWatermarkImagesPath(AdiFile adiFile) {
        return watermarkImagesPath + adiFile.getUuid() + "." + adiFile.getExt();
    }

    /**
     * 获取单个文件 URL。
     *
     * @param fileUuid 文件 UUID
     * @return 文件 URL
     */
    public String getUrl(String fileUuid) {
        if (StringUtils.isBlank(fileUuid)) {
            return null;
        }
        List<String> list = getUrls(List.of(fileUuid));
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 获取文件 URL 列表。
     *
     * @param fileUuids 文件 UUID 列表
     * @return 文件 URL 列表
     */
    public List<String> getUrls(List<String> fileUuids) {
        if (CollectionUtils.isEmpty(fileUuids)) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        this.lambdaQuery()
                .in(AdiFile::getUuid, fileUuids)
                .eq(AdiFile::getIsDeleted, false)
                .list()
                .forEach(adiFile -> {
                    result.add(FileOperatorContext.getFileUrl(adiFile));
                });
        return result;
    }
}
