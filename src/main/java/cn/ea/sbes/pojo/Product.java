package cn.ea.sbes.pojo;

import lombok.Data;
import lombok.ToString;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.HashMap;
import java.util.Map;

@Data
@ToString
@Document(indexName = "sb_es_product")
public class Product {
    int id;
    String name;
    String category;
    float price;
    String place;
    String code;

    public Map<String, Object> toMap() {
        HashMap<String,Object> map = new HashMap<>();
        map.put("name", name);
        map.put("category", category);
        map.put("code", code);
        map.put("place", place);
        map.put("price", price);
        return map;
    }
}