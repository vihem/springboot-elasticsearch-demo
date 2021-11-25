package cn.ea.sbes.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "sb_es_user")
public class User {
    private String id;  // 用于索引的id
    private String username;
    private Integer age;
    private String title;
}