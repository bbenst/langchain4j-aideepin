package com.moyz.adi.common.file;

import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.entity.AdiFile;
import com.moyz.adi.common.service.SysConfigService;
import com.moyz.adi.common.vo.SaveRemoteImageResult;
import dev.langchain4j.data.document.Document;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

import static com.moyz.adi.common.cosntant.AdiConstant.STORAGE_LOCATION_VALUE_ALI_OSS;
import static com.moyz.adi.common.cosntant.AdiConstant.STORAGE_LOCATION_VALUE_LOCAL;
/**
 * 文件操作器上下文。
 */
public class FileOperatorContext {

    /**
     * 存储类型与具体实现的映射。
     */
    private static final Map<Integer, IFileOperator> CONCRETE_OPT = new HashMap<>();

    static {
        CONCRETE_OPT.put(STORAGE_LOCATION_VALUE_LOCAL, new LocalFileOperator());
        CONCRETE_OPT.put(STORAGE_LOCATION_VALUE_ALI_OSS, new AliyunOssFileOperator());
    }
    /**
     * 当前使用的文件操作器。
     */
    private final IFileOperator currentOpt;
    /**
     * 根据存储位置初始化上下文。
     *
     * @param storageLocation 存储位置
     */
    public FileOperatorContext(Integer storageLocation) {
        this.currentOpt = CONCRETE_OPT.get(storageLocation);
    }
    /**
     * 读取系统配置初始化上下文。
     */
    public FileOperatorContext() {
        Integer storageLocation = SysConfigService.getIntByKey(AdiConstant.SysConfigKey.STORAGE_LOCATION, -1);
        this.currentOpt = CONCRETE_OPT.get(storageLocation);
    }
    /**
     * 检查文件是否存在。
     *
     * @param adiFile 文件实体
     * @return 是否存在
     */
    public static boolean checkIfExist(AdiFile adiFile) {
        return CONCRETE_OPT.get(adiFile.getStorageLocation()).checkIfExist(adiFile);
    }
    /**
     * 保存文件并返回路径与后缀。
     */
    public Pair<String, String> save(MultipartFile file, boolean image, String fileName) {
        return currentOpt.save(file, image, fileName);
    }
    /**
     * 保存字节数组并返回路径与后缀。
     */
    public Pair<String, String> save(byte[] file, boolean image, String fileName) {
        return currentOpt.save(file, image, fileName);
    }
    /**
     * 通过远程 URL 保存图片。
     */
    public SaveRemoteImageResult saveImageFromUrl(String imageUrl, String uuid) {
        return currentOpt.saveImageFromUrl(imageUrl, uuid);
    }
    /**
     * 删除文件。
     */
    public static void delete(AdiFile adiFile) {
        CONCRETE_OPT.get(adiFile.getStorageLocation()).delete(adiFile);
    }
    /**
     * 获取文件访问 URL。
     */
    public static String getFileUrl(AdiFile adiFile) {
        return CONCRETE_OPT.get(adiFile.getStorageLocation()).getFileUrl(adiFile);
    }
    /**
     * 加载文档内容。
     */
    public static Document loadDocument(AdiFile adiFile) {
        return CONCRETE_OPT.get(adiFile.getStorageLocation()).loadDocument(adiFile);
    }
    /**
     * 获取当前存储位置配置。
     *
     * @return 存储位置
     */
    public static int getStorageLocation() {
        return SysConfigService.getIntByKey(AdiConstant.SysConfigKey.STORAGE_LOCATION, -1);
    }
}
