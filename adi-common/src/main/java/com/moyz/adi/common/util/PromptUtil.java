package com.moyz.adi.common.util;

import com.moyz.adi.common.exception.BaseException;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.model.input.Prompt;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.moyz.adi.common.cosntant.AdiConstant.*;
import static com.moyz.adi.common.enums.ErrorEnum.A_USER_QUESTION_NOT_FOUND;
/**
 * 提示词拼装工具类。
 */
public class PromptUtil {
    /**
     * 创建最终提示词文本。
     *
     * @param question   用户问题
     * @param memory     历史记忆
     * @param information 检索信息
     * @param extraInfo  额外信息
     * @return 拼装后的提示词
     */
    public static String createPrompt(String question, String memory, String information, String extraInfo) {
        if (StringUtils.isBlank(question)) {
            throw new BaseException(A_USER_QUESTION_NOT_FOUND);
        }
        if (StringUtils.isAllBlank(memory, information, extraInfo)) {
            return question;
        }
        return PROMPT_INFO_EXTRA_TEMPLATE.apply(Map.of("question", question, "memory", memory, "information", Matcher.quoteReplacement(information), "extraInfo", extraInfo)).text();
    }
    /**
     * 从方法参数中提取图片内容。
     *
     * @param method 方法
     * @param args   参数值
     * @return 图片内容列表
     */
    public static List<Content> findImagesContentInParameters(Method method, Object[] args) {
        return Arrays.stream(method.getParameters())
                .filter(PromptUtil::isImageParameter)
                .flatMap(parameter -> {
                    List<ImageContent> imageContents = new ArrayList<>();
                    Object arg = args[Arrays.asList(method.getParameters()).indexOf(parameter)];
                    if (arg instanceof List imageList) {
                        for (Object ic : imageList) {
                            if (ic instanceof ImageContent imageContent) imageContents.add(imageContent);
                        }
                    }
                    return imageContents.stream();
                })
                .collect(Collectors.toList());
    }
    /**
     * 判断参数是否为图片相关参数。
     *
     * @param parameter 参数信息
     * @return 是否为图片参数
     */
    public static boolean isImageParameter(Parameter parameter) {
        return parameter.isAnnotationPresent(dev.langchain4j.service.UserMessage.class) &&
               (
                       parameter.getType().equals(Image.class)
                       || parameter.getType().equals(ImageContent.class)
                       || parameter.getType().equals(List.class)
               );
    }
    /**
     * 将提示词与图片内容合并为统一内容列表。
     *
     * @param prompt       提示词
     * @param imageContent 图片内容
     * @return 合并后的内容列表
     */
    public static List<Content> promptAndImages(Prompt prompt, List<Content> imageContent) {
        return Stream.concat(Stream.of(TextContent.from(prompt.text())), imageContent.stream()).toList();
    }
}
