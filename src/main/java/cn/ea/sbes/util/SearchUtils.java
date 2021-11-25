package cn.ea.sbes.util;

import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;

public class SearchUtils {

    // 设置高亮字段
    public static HighlightBuilder getHighlightBuilder(String... fields) {
        // 高亮条件
        HighlightBuilder highlightBuilder = new HighlightBuilder(); // 生成高亮查询器
        for (String field : fields) {
            highlightBuilder.field(field);// 高亮查询字段
        }
        highlightBuilder.requireFieldMatch(false); // 如果要多个字段高亮,这项要为false
        highlightBuilder.preTags("<span style=\"color:red\">"); // 高亮设置
        highlightBuilder.postTags("</span>");
        // 下面这两项,如果你要高亮如文字内容等有很多字的字段,必须配置,不然会导致高亮不全,文章内容缺失等
        highlightBuilder.fragmentSize(800000); // 最大高亮分片数
        highlightBuilder.numOfFragments(0); // 从第一个分片获取高亮片段

        return highlightBuilder;
    }
}
