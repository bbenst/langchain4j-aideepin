package com.moyz.adi.common.util;

import com.moyz.adi.common.cosntant.AdiConstant;
import com.moyz.adi.common.vo.TtsSetting;
/**
 * 语音合成工具类。
 */

public class TtsUtil {

    /**
     * 工具类禁止实例化。
     */
    private TtsUtil() {
    }

    /**
     * 判断是否需要将文本转语音。
     * 条件：系统设置为服务端合成且当前返回内容为音频。
     *
     * @param ttsSetting        TTS 设置
     * @param answerContentType 内容类型
     * @return 是否需要进行文本转语音
     * @throws NullPointerException ttsSetting 为空时抛出异常
     */
    public static boolean needTts(TtsSetting ttsSetting, int answerContentType) {
        return AdiConstant.TtsConstant.SYNTHESIZER_SERVER.equals(ttsSetting.getSynthesizerSide()) && answerContentType == AdiConstant.ConversationConstant.ANSWER_CONTENT_TYPE_AUDIO;
    }
}
