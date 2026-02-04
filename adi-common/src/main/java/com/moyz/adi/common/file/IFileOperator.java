package com.moyz.adi.common.file;

import com.moyz.adi.common.entity.AdiFile;
import com.moyz.adi.common.vo.SaveRemoteImageResult;
import dev.langchain4j.data.document.Document;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.multipart.MultipartFile;
/**
 * 文件操作器接口。
 */
public interface IFileOperator {
    /**
     * 检查文件是否存在。
     *
     * @param adiFile 文件实体
     * @return 是否存在
     */
    boolean checkIfExist(AdiFile adiFile);

    /**
     * 保存文件并返回路径与后缀。
     *
     * @param file     文件
     * @param image    是否图片
     * @param fileName 文件名（不带后缀时根据file获取后缀并追加到fileName）
     * @return 文件路径及后缀
     */
    Pair<String, String> save(MultipartFile file, boolean image, String fileName);

    /**
     * 保存字节数组并返回路径与后缀。
     *
     * @param file     文件
     * @param image    是否图片
     * @param fileName 文件名
     * @return 文件路径及后缀
     */
    Pair<String, String> save(byte[] file, boolean image, String fileName);

    /**
     * 通过远程 URL 保存图片。
     *
     * @param imageUrl 图片地址
     * @param uuid 文件标识
     * @return 保存结果
     */
    SaveRemoteImageResult saveImageFromUrl(String imageUrl, String uuid);

    /**
     * 删除文件。
     *
     * @param adiFile 文件实体
     */
    void delete(AdiFile adiFile);

    /**
     * 获取文件访问 URL。
     *
     * @param adiFile 文件实体
     * @return 访问 URL
     */
    String getFileUrl(AdiFile adiFile);

    /**
     * 加载文档内容。
     *
     * @param adiFile 文件实体
     * @return 文档内容
     */
    Document loadDocument(AdiFile adiFile);
}
