package cn.ea.sbes.test;

import cn.ea.sbes.pojo.D2;
import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;

public class TestD2 {

    public static <T> void createIndex3(ElasticsearchOperations elasticsearchOperations, Class<T> tClass){
        boolean b = elasticsearchOperations.indexOps(tClass).create();
        System.out.println(b?"创建索引成功":"创建索引失败");
    }
    public static void putD2(ElasticsearchOperations elasticsearchOperations, D2 d2){
        D2 d=elasticsearchOperations.save(d2);
        System.out.println(d);
    }

    public static <T> void getD2(ElasticsearchOperations elasticsearchOperations,String id, Class<T> tClass){
        D2 d = (D2) elasticsearchOperations.get(id, tClass);
        System.out.println(d);
    }

//    @Test
//    void createIndex3(){
//        IndexOperations ops = elasticsearchOperations.indexOps(D2.class);
//        boolean b = ops.create();
//        System.out.println(b?"创建索引成功":"创建索引失败");
//    }
//    @Test
//    void putD2(){
//        D2 d=elasticsearchOperations.save(new D2(1, "增加一个"));
//        System.out.println(d);
//    }
//    @Test
//    void getD2(){
//        D2 d = elasticsearchOperations.get("1",D2.class);
//        System.out.println(d);
//    }
}
