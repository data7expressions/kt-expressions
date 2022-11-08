package com.flaviolionelrita.h3lp

import java.net.URLDecoder
class HttpHelper {

    fun decode(source: String): String {
        var url = source
        if (url.contains("~1")) {
            url = url.replace("~1", "/")
        }
        if (url.contains("~0")) {
            url = url.replace("~0", "~")
        }
        if (url.contains("%")) {
            url = URLDecoder.decode(url, "UTF-8")
        }
        return url
    }
}
