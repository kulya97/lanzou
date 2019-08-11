package com.kulya.lanzou.http;
/*
项目名称： lanzou
创建人：黄大神
类描述：
创建时间：2019/8/6 19:28
*/
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;

import java.security.PublicKey;

public class JsoupUtil {

    public static String LoginInfo(String html) {
        if (html == null)
            return null;
        return Jsoup.clean(html, Whitelist.none());
    }
    // 只有纯文本可以通过
    public static String getText(String html) {
        if (html == null)
            return null;
        return Jsoup.clean(html, Whitelist.none());
    }

    // 以下标签可以通过
    // b, em, i, strong, u. 纯文本
    public static String getSimpleHtml(String html) {
        if (html == null)
            return null;
        return Jsoup.clean(html, Whitelist.simpleText());
    }

    // 以下标签可以通过
    //a, b, blockquote, br, cite, code, dd, dl, dt, em, i, li, ol, p, pre, q, small, strike, strong, sub, sup, u, ul
    public static String getBasicHtml(String html) {
        if (html == null)
            return null;
        return Jsoup.clean(html, Whitelist.basic());
    }

    //在basic基础上  增加图片通过
    public static String getBasicHtmlandimage(String html) {
        if (html == null)
            return null;
        return Jsoup.clean(html, Whitelist.basicWithImages());
    }
    // 以下标签可以通过
    //a, b, blockquote, br, caption, cite, code, col, colgroup, dd, dl, dt, em, h1, h2, h3, h4, h5, h6, i, img, li, ol, p, pre, q, small, strike, strong, sub, sup, table, tbody, td, tfoot, th, thead, tr, u, ul
    public static String getFullHtml(String html) {
        if (html == null)
            return null;
        return Jsoup.clean(html, Whitelist.relaxed());
    }

    //只允许指定的html标签
    public static String clearTags(String html, String ...tags) {
        Whitelist wl = new Whitelist();
        return Jsoup.clean(html, wl.addTags(tags));
    }

    // 获取文章中的img url
    public static String getImgSrc(String html) {
        if (html == null)
            return null;
        Document doc = Jsoup.parseBodyFragment(html);
        Element image = doc.select("img").first();
        return image == null ? null : image.attr("src");
    }
}