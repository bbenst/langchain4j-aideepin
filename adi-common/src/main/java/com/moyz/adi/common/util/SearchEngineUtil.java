package com.moyz.adi.common.util;

import com.moyz.adi.common.cosntant.AdiConstant;
/**
 * 搜索引擎参数校验工具类。
 */
public class SearchEngineUtil {
    /**
     * 工具类禁止实例化。
     */
    private SearchEngineUtil() {
    }

    /**
     * 校验 Google 国家参数是否有效。
     *
     * @param country 国家代码
     * @return 是否有效
     */
    public static boolean checkGoogleCountry(String country) {
        boolean result = false;
        for (String googleCountry : AdiConstant.SearchEngineName.GOOGLE_COUNTRIES) {
            if (googleCountry.equalsIgnoreCase(country)) {
                result = true;
                break;
            }
        }
        return result;
    }
    /**
     * 校验 Google 语言参数是否有效。
     *
     * @param language 语言代码
     * @return 是否有效
     */
    public static boolean checkGoogleLanguage(String language) {
        boolean result = false;
        for (String googleCountry : AdiConstant.SearchEngineName.GOOGLE_LANGUAGES) {
            if (googleCountry.equalsIgnoreCase(language)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
