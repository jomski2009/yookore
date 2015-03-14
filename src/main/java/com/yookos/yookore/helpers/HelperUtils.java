package com.yookos.yookore.helpers;


import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Created by jome on 2014/08/28.
 */

@Component
public class HelperUtils {
    public static String sanitize(String string) {
        String result = string.replaceAll("\"", StringUtils.EMPTY);
        return result;
    }
}
