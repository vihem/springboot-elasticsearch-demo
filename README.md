# SpringBoot 整合 ElasticSearch-7.15.2
[https://blog.csdn.net/vihem/article/details/121428258](https://blog.csdn.net/vihem/article/details/121428258)

## 一、Linux 安装 ElasticSearch-7.15.2
[ElasticSearch 之 Linux 安装 ElasticSearch-7.15.2(ELK、IK)](https://blog.csdn.net/vihem/article/details/121404092)

&nbsp;
## 二、SpringBoot 整合 ElasticSearch
**项目GitHub地址**： [springboot-elasticsearch-demo](https://github.com/vihem/springboot-elasticsearch-demo)

### 1. 新建一个springboot项目，添加maven依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<!--swagger-->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.9.2</version>
</dependency>
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.9.2</version>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.75</version>
</dependency>
<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
    <version>2.6</version>
</dependency>
```

### 2.  添加配置文件 application.yml

```yaml
spring:
  elasticsearch:
    rest:
      uris: 192.168.25.137:9200
      connection-timeout: 600s
      read-timeout: 600s
server:
  port: 8888
```
或者使用java类：

```java
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
public class ElasticSearchConfig {
    
    @Bean
    public RestHighLevelClient getRestHighLevelClient(){
        return new RestHighLevelClient(
                RestClient.builder(new HttpHost("192.168.25.137", 9200, "http"))
                        .setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
                            @Override
                            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder builder) {
                                return builder.setConnectTimeout(5000 * 1000) // 连接超时（默认为1秒）
                                        .setSocketTimeout(6000 * 1000);// 套接字超时（默认为30秒）//更改客户端的超时限制默认30秒现在改为100*1000分钟
                            }
                        })
        );
    }
}
```
### 3. 配置swagger

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api(){
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("cn.ea.sbes.controller"))//Controller 包名
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("springboot-elasticsearch")
                .build();
    }
}
```
### 4. Pojo
重点在于pojo加一个@Document，指定索引，表明了要连接到 ElasticSearch 的哪个索引
```java
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
```

### 5. Dao
使用 ElasticsearchRepository ，简单的增删改查就不用另外实现了
```java
@Repository
public interface ProductDao extends ElasticsearchRepository<Product, String> {}
```

### 6. Service
对于导包，这里只写了部分包名。

**6.1 索引管理**
```java
public interface IEsService {
    String createIndex(String indexName) throws IOException;
    <T> String createIndex2(Class<T> tClass) throws IOException;
    boolean isIndexExist(String indexName) throws IOException;
    boolean checkExistIndex(String indexName) throws IOException;
    boolean deleteIndex(String indexName) throws IOException;
    String getDocument(String indexName, String id) throws IOException;
}
```

```java
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EsServiceImpl implements IEsService {
    
//    @Qualifier("getRestHighLevelClient")
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    ElasticsearchOperations elasticsearchOperations;

    @Override
    public String createIndex(String indexName) {
        if (isIndexExist(indexName)){
            return " 索引 "+ indexName + " 已经存在";
        }
        try {
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            CreateIndexResponse response = restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            System.out.println(" 创建索引 "+response.index() + " 成功");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return " 创建索引 "+ indexName + " 成功";
    }

    /**
     * 这种方式创建索引很奇葩，会报错，但是创建索引能成功，不建议使用，只用于对比: 
     * Elasticsearch exception [type=resource_already_exists_exception, reason=index [sb_es_product/iHwe11k9T0yNFrI_R9oipw] already exists]
     */
    @Override
    public <T> String createIndex2(Class<T> tClass) throws IOException {
        IndexOperations ops = elasticsearchOperations.indexOps(tClass);
        boolean b = ops.create();
        return b ? "创建索引成功":"创建索引失败";
    }

    @Override
    public boolean isIndexExist(String indexName) {
        GetIndexRequest getIndexRequest = new GetIndexRequest(indexName);
        boolean exists = false;
        try {
            exists = restHighLevelClient.indices().exists(getIndexRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exists;
    }

    @Override
    public boolean checkExistIndex(String indexName) throws IOException {
        boolean result = true;
        try {
            OpenIndexRequest openIndexRequest = new OpenIndexRequest(indexName);
            restHighLevelClient.indices().open(openIndexRequest, RequestOptions.DEFAULT).isAcknowledged();
        } catch (ElasticsearchStatusException ex) {
            String m = "Elasticsearch exception [type=index_not_found_exception, reason=no such index ["+ indexName +"]]";
            if (m.equals(ex.getMessage())) {
                result = false;
            }
        }
        return result;
    }

    @Override
    public boolean deleteIndex(String indexName) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        AcknowledgedResponse response = restHighLevelClient.indices().delete(request, RequestOptions.DEFAULT);
        return response.isAcknowledged();
    }

    @Override
    public String getDocument(String indexName, String id) throws IOException {
        GetRequest request = new GetRequest(indexName, id);

        GetResponse response = restHighLevelClient.get(request, RequestOptions.DEFAULT);
        if(!response.isExists()){
            System.out.println("检查到服务器上 id="+id+ " 的文档不存在");
            return "检查到服务器上 id="+id+ " 的文档不存在";
        } else {
            String source = response.getSourceAsString();
            System.out.print("获取到服务器上 id="+id+ " 的文档内容是：");
            System.out.println(source);
            return source;
        }
    }
}
```

**6.2 Product管理**
```java
public interface IProductService {
    String batchInsert(List<Product> products) throws IOException;
    List<Product> searchProducts(String keyword, int start, int count) throws IOException;
    List<Product> searchProducts2(String keyword, int page, int rows) throws IOException;
    Product addProduct(Product product) throws Exception;
    void deleteProduct(Product product) throws Exception;
    Product updateProduct(Product product) throws Exception;
    Optional<Product> findById(String id) throws Exception;
}
```

```java
import org.apache.commons.codec.binary.StringUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ProductServiceImpl implements IProductService {
    private final static String indexName = "sb_es_product";
    
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
/
/    @Qualifier("getRestHighLevelClient")
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private IEsService esService;

    @Autowired
    private ProductDao productDao;
    
    @Override
    public String batchInsert(List<Product> products) throws IOException {
        if (!esService.isIndexExist(indexName)){
            esService.createIndex(indexName);
        }
        
        BulkRequest bulkRequest = new BulkRequest();
        for (Product product : products) {
            // 封装除了id的其他信息，id做为索引的id
            Map<String,Object> m  = product.toMap();
            IndexRequest indexRequest= new IndexRequest(indexName).id(String.valueOf(product.getId())).source(m);
            bulkRequest.add(indexRequest);
        }
        BulkResponse bulkResponse = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return bulkResponse.hasFailures() ? "Failed": "Successful" ;
    }

    @Override
    public List<Product> searchProducts(String keyword, int start, int count) throws IOException {
        // 1. 创建 匹配查询构建器
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("name", keyword);//关键字匹配
        matchQueryBuilder.fuzziness(Fuzziness.AUTO);//模糊匹配

        // 2. 创建 搜索资源构建器，设置搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(matchQueryBuilder) // 为 SearchSourceBuilder 指定 MatchQueryBuilder
                .from(start)    //第几页
                .size(count)    //第几条
                .sort(new ScoreSortBuilder().order(SortOrder.DESC));    //设置搜索排序为降序，即匹配度从高到低

        // 3. 创建 搜索请求
        SearchRequest searchRequest = new SearchRequest(indexName);
        searchRequest.source(sourceBuilder);
        
        // 4. 执行查询，并使用 搜索响应-SearchResponse 接收查询结果
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        
        // 5. 获取 搜索结果-SearchHits
        SearchHits searchHits = searchResponse.getHits();
        // 6. 处理搜索结果，并返回结果 //推荐直接返回SearchHits结果
        List<Product> products = new ArrayList<>();
        for (SearchHit hit : searchHits) {
//            System.out.println(hit.getSourceAsString());
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            Product product = new Product();
            product.setId(Integer.parseInt(hit.getId()));
            product.setCode(String.valueOf(sourceAsMap.get("code")));
            product.setPrice(Float.parseFloat(sourceAsMap.get("price").toString()));
            product.setName((String) sourceAsMap.get("name"));
            product.setPlace((String) sourceAsMap.get("place"));
            product.setCategory((String) sourceAsMap.get("category"));
            
            products.add(product);
        }
        return products;
    }
    
    @Override
    public List<Product> searchProducts2(String keyword, int page, int rows) throws IOException{
        List<Product> arrayList = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, rows); // 设置分页参数
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("name", keyword).operator(Operator.AND)) // match查询
                .withPageable(pageable) //分页
                .withHighlightBuilder(getHighlightBuilder("name")) //高亮
                .withSort(new ScoreSortBuilder().order(SortOrder.DESC))
                .build();
        org.springframework.data.elasticsearch.core.SearchHits<Product> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, Product.class);
        for (org.springframework.data.elasticsearch.core.SearchHit<Product> searchHit : searchHits) { // 获取搜索到的数据
            Product content = searchHit.getContent();
            Product product = new Product();
            BeanUtils.copyProperties(content, product);

            //处理高亮
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            for (Map.Entry<String, List<String>> highlightFieldsStr : highlightFields.entrySet()){
                String key = highlightFieldsStr.getKey();
                System.out.println(" key = "+key);
                if (StringUtils.equals(key, "name")){
                    List<String> fragments = highlightFieldsStr.getValue();
                    StringBuilder sb = new StringBuilder();
                    for (String fragment : fragments){
                        System.out.println(" -- "+fragment);
                        sb.append(fragment);
                    }
                    product.setName(sb.toString());
                }
            }
            arrayList.add(product);
        }
        return arrayList;
    }
    @Override
    public Product addProduct(Product product) throws Exception {
        product.setId(currentTime());
        return productDao.save(product);
    }

    @Override
    public void deleteProduct(Product product) throws Exception {
        productDao.delete(product);
    }

    @Override
    public Product updateProduct(Product product) throws Exception {
        return productDao.save(product);
    }

    @Override
    public Optional<Product> findById(String id) throws Exception {
        return productDao.findById(id);
    }

    private int currentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
        String time= sdf.format(new Date());
        return Integer.parseInt(time);
    }
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
```

**关于SearchProducts和SearchProducts2函数**：

SearchProducts的jar包如下：
`import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;`

SearchProducts2：使用了`ElasticsearchRestTemplate`，而且其中有两个jar包不同：
`import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;`

详细可以查看代码。

### 7. Controller
**7.1 索引管理**
```java
@RestController
@RequestMapping("/es")
@Api("EsController")
public class EsController {

    @Autowired
    private IEsService esService;

    @PutMapping("/create/{indexName}")
    @ApiOperation("createIndex")
    public String createIndex(@PathVariable String indexName) throws IOException {
        return esService.createIndex(indexName);
    }
    
    @PutMapping("/create2")
    @ApiOperation("createIndex2")
    public String createIndex2() throws IOException {
        return esService.createIndex2(Product.class);
    }


    @DeleteMapping("/delete/{indexName}")
    @ApiOperation("deleteIndex")
    public String deleteIndex(@PathVariable String indexName) throws IOException{
        boolean b = esService.deleteIndex(indexName);
        return b ? (indexName + " 索引删除成功"):(indexName + " 索引删除失败");
    }
    
    @GetMapping("/getDocById/{indexName}/{id}")
    @ApiOperation("getDocById")
    public String getDocById(@PathVariable String indexName, @PathVariable String id) throws IOException {
        return esService.getDocument(indexName, id);
    }
}
```

**7.2 Product管理**

```java
@RestController
@RequestMapping("/product")
@Api("product")
public class ProductController {
    
    @Autowired
    private IProductService productService;
    
    @ApiOperation("search")
    @GetMapping("/search/{keyword}/{start}/{count}")
    public List<Product> searchProducts(@PathVariable String keyword,
                                        @PathVariable int start,
                                        @PathVariable int count) throws IOException {
        return productService.searchProducts(keyword,start,count);
    }

    @ApiOperation("add")
    @RequestMapping("/add")
    public Product addProduct(@RequestBody Product product) throws Exception{
        return productService.addProduct(product);
    }

    @ApiOperation("delete")
    @RequestMapping("/delete")
    public void deleteProduct(@RequestBody Product product) throws Exception{
        productService.deleteProduct(product);
    }

    @ApiOperation("update")
    @RequestMapping("/update")
    public Product updateProduct(@RequestBody Product product) throws Exception{
        return productService.updateProduct(product);
    }

    @ApiOperation("findById")
    @GetMapping("/findById/{id}")
    public Optional<Product> findById(@PathVariable String id) throws Exception{
        return productService.findById(id);
    }
}
```

## 三、启动测试
1. 启动ElasticSearch

2. 启动SpringBoot 的 Application
	```java
	@SpringBootApplication
	public class Application {
	    public static void main(String[] args) {
	        SpringApplication.run(Application.class, args);
	    }
	}
	```
3. 访问 [http://localhost:8888/swagger-ui.html/](http://localhost:8888/swagger-ui.html/)
		![在这里插入图片描述](https://img-blog.csdnimg.cn/efb964f2311548d0ad10ba196ebff2ad.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAdmloZW0=,size_20,color_FFFFFF,t_70,g_se,x_16)
	![在这里插入图片描述](https://img-blog.csdnimg.cn/5377e50dcea64efdad3ccbb1f960b968.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAdmloZW0=,size_20,color_FFFFFF,t_70,g_se,x_16)
	![在这里插入图片描述](https://img-blog.csdnimg.cn/a6185ae1605c463b834f4b960c53df7c.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAdmloZW0=,size_20,color_FFFFFF,t_70,g_se,x_16)
![在这里插入图片描述](https://img-blog.csdnimg.cn/cf5f694a63da41aea92dac897b69fe1e.png?x-oss-process=image/watermark,type_ZHJvaWRzYW5zZmFsbGJhY2s,shadow_50,text_Q1NETiBAdmloZW0=,size_20,color_FFFFFF,t_70,g_se,x_16)

**项目GitHub地址**： [springboot-elasticsearch-demo](https://github.com/vihem/springboot-elasticsearch-demo)

---
若有不正之处，请谅解和批评指正，谢谢~
转载请标明：
[https://blog.csdn.net/vihem/article/details/121428258](https://blog.csdn.net/vihem/article/details/121428258)
