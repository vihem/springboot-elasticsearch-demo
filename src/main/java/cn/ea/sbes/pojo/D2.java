package cn.ea.sbes.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

@Data
@ToString
@NoArgsConstructor  //创建无参构造函数
@AllArgsConstructor //创建全参构造函数
@Document(indexName = "test2") //标注了一个文档主题，对应了mysql中的库，只有一个表
public class D2 {
    @Id //ES中的id
    private Integer id;
    @Field  //可以设定类型
    private String title;
}
