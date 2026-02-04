package com.moyz.adi.common.util;

import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisOutput;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.OSSUtils;
import com.moyz.adi.common.service.FileService;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.internal.Utils;
import jakarta.activation.MimetypesFileTypeMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
/**
 * 图片处理工具类。
 */
@Slf4j
public class ImageUtil {
    /**
     * 判断图片是否不是 ARGB 颜色模式。
     *
     * @param imagePath 图片路径
     * @return 是否非 ARGB
     */
    public static boolean isNotArgb(String imagePath) {
        try {
            // 读取图片
            BufferedImage image = ImageIO.read(new File(imagePath));
            // 获取图片的颜色模型
            int colorModel = image.getColorModel().getColorSpace().getType();
            if (colorModel != BufferedImage.TYPE_INT_ARGB) {
                return true;
            }
        } catch (IOException e) {
            log.error("isARGB error", e);
        }
        return false;
    }
    /**
     * 将 RGB 图片转为 RGBA 并输出到指定路径。
     *
     * @param rbgPath  RGB 图片路径
     * @param argbPath RGBA 图片输出路径
     */
    public static void rgbConvertToRgba(String rbgPath, String argbPath) {
        log.info("RGB convert to RGBA, rbgPath:{}, argbPath:{}", rbgPath, argbPath);
        try {
            // 读取RGB图片
            BufferedImage rgbImage = ImageIO.read(new File(rbgPath));

            // 创建一个RGBA图片，与原始RGB图片大小相同
            BufferedImage rgbaImage = new BufferedImage(rgbImage.getWidth(), rgbImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

            // 将RGB图片绘制到RGBA图片上，并设置透明度为不透明
            Graphics2D g = rgbaImage.createGraphics();
            g.drawImage(rgbImage, 0, 0, null);
            g.dispose();

            // 保存RGBA图片
            ImageIO.write(rgbaImage, "png", new File(argbPath));

        } catch (IOException e) {
            log.error("error", e);
        }
    }
    /**
     * 将图片转为 RGBA 格式并保存，返回可用文件。
     *
     * @param file     原始图片文件
     * @param rgbaPath RGBA 图片输出路径
     * @return RGBA 文件或原文件
     */
    public static File rgbConvertToRgba(File file, String rgbaPath) {
        try {
            BufferedImage image = ImageIO.read(file);

            // 获取图片的颜色模型
            int colorModel = image.getColorModel().getColorSpace().getType();
            if (colorModel != BufferedImage.TYPE_INT_ARGB) {
                // 创建一个RGBA图片，与原始RGB图片大小相同
                BufferedImage rgbaImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

                // 将RGB图片绘制到RGBA图片上，并设置透明度为不透明
                Graphics2D g = rgbaImage.createGraphics();
                g.drawImage(image, 0, 0, null);
                g.dispose();
                // 保存RGBA图片
                ImageIO.write(rgbaImage, "png", new File(rgbaPath));

                return new File(rgbaPath);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    /**
     * 图片地址转 ImageContent。
     * 本地图片转为 Base64，远程图片使用 URL。
     *
     * @param imageUrls 图片地址列表
     * @return 图片内容列表
     */
    public static List<Content> urlsToImageContent(List<String> imageUrls) {
        if (CollectionUtils.isEmpty(imageUrls)) {
            return new ArrayList<>();
        }
        List<Content> result = new ArrayList<>();
        try {
            for (String imageUrl : imageUrls) {
                log.info("urlsToImageContent,imageUrl:{}", imageUrl);
                if (!imageUrl.contains("http") && imageUrl.length() == 32) {
                    String absolutePath = SpringUtil.getBean(FileService.class).getImagePath(imageUrl);
                    File file = new File(absolutePath);
                    MimetypesFileTypeMap mimetypesFileTypeMap = new MimetypesFileTypeMap();
                    String mimeType = mimetypesFileTypeMap.getContentType(file);
                    try (FileInputStream fileInputStream = new FileInputStream(file)) {
                        byte[] fileBytes = new byte[(int) file.length()];
                        fileInputStream.read(fileBytes);
                        result.add(ImageContent.from(Base64.getEncoder().encodeToString(fileBytes), mimeType));
                    }
                } else {
                    result.add(ImageContent.from(imageUrl));
                }
            }
        } catch (IOException e) {
            log.error("urlsToImageContent error", e);
        }
        return result;
    }
    /**
     * 创建缩略图。
     *
     * @param inputFile   原图路径
     * @param outputFile  输出路径
     * @param thumbWidth  目标宽度
     * @param thumbHeight 目标高度
     * @param quality     质量参数（暂不参与处理）
     * @throws IOException 读取或写入异常
     */
    public static void createThumbnail(String inputFile, String outputFile, int thumbWidth, int thumbHeight, int quality) throws IOException {
        File input = new File(inputFile);
        BufferedImage image = ImageIO.read(input);
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();

        // 计算缩放比例
        double thumbRatio = (double) thumbWidth / (double) thumbHeight;
        double imageRatio = (double) imageWidth / (double) imageHeight;
        if (thumbRatio < imageRatio) {
            thumbHeight = (int) (thumbWidth / imageRatio);
        } else {
            thumbWidth = (int) (thumbHeight * imageRatio);
        }

        // 创建缩略图
        Image thumbnail = image.getScaledInstance(thumbWidth, thumbHeight, Image.SCALE_SMOOTH);
        BufferedImage outputImage = new BufferedImage(thumbWidth, thumbHeight, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(thumbnail, 0, 0, null);

        // 保存缩略图
        File output = new File(outputFile);
        ImageIO.write(outputImage, "JPEG", output);
    }
    /**
     * 从图片生成结果中提取图片列表。
     *
     * @param result 图片生成结果
     * @return 图片列表
     */
    public static List<dev.langchain4j.data.image.Image> imagesFrom(ImageSynthesisResult result) {
        return Optional.of(result)
                .map(ImageSynthesisResult::getOutput)
                .map(ImageSynthesisOutput::getResults)
                .orElse(Collections.emptyList())
                .stream()
                .map(resultMap -> resultMap.get("url"))
                .map(url -> dev.langchain4j.data.image.Image.builder().url(url).build())
                .collect(Collectors.toList());
    }
    /**
     * 获取图片可访问 URL。
     *
     * @param image  图片对象
     * @param model  模型名称
     * @param apiKey API Key
     * @return 图片 URL
     */
    public static String imageUrl(dev.langchain4j.data.image.Image image, String model, String apiKey) {
        String imageUrl;

        if (image.url() != null) {
            imageUrl = image.url().toString();
        } else if (Utils.isNotNullOrBlank(image.base64Data())) {
            String filePath = saveDataAsTemporaryFile(image.base64Data(), image.mimeType());
            try {
                imageUrl = OSSUtils.upload(model, filePath, apiKey);
            } catch (NoApiKeyException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new IllegalArgumentException("Failed to get image url from " + image);
        }

        return imageUrl;
    }
    /**
     * 将 Base64 数据保存为临时文件并返回路径。
     *
     * @param base64Data Base64 数据
     * @param mimeType   MIME 类型
     * @return 临时文件路径
     */
    public static String saveDataAsTemporaryFile(String base64Data, String mimeType) {
        String tmpDir = System.getProperty("java.io.tmpdir", "/tmp");
        String tmpFileName = UUID.randomUUID().toString();
        if (Utils.isNotNullOrBlank(mimeType)) {
            // 例如 "image/png", "image/jpeg"...
            int lastSlashIndex = mimeType.lastIndexOf("/");
            if (lastSlashIndex >= 0 && lastSlashIndex < mimeType.length() - 1) {
                String fileSuffix = mimeType.substring(lastSlashIndex + 1);
                tmpFileName = tmpFileName + "." + fileSuffix;
            }
        }

        Path tmpFilePath = Paths.get(tmpDir, tmpFileName);
        byte[] data = Base64.getDecoder().decode(base64Data);
        try {
            Files.copy(new ByteArrayInputStream(data), tmpFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tmpFilePath.toAbsolutePath().toString();
    }
    /**
     * 判断文件扩展名是否为图片类型。
     *
     * @param fileExt 文件扩展名
     * @return 是否为图片
     */
    public static boolean isImage(String fileExt) {
        List<String> imageExtensions = Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp");
        return imageExtensions.contains(fileExt.toLowerCase());
    }
}
