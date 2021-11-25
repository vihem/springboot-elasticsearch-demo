package cn.ea.sbes.service.impl;

import cn.ea.sbes.dao.ProductDao;
import cn.ea.sbes.pojo.Product;
import cn.ea.sbes.service.IEsService;
import cn.ea.sbes.service.IProductService;
import cn.ea.sbes.util.SearchUtils;
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
    private final static String indexName = "sb_es_product"; // 索引名，原生方法需要用到，使用了 ProductDao 的不需要
    
//    @Qualifier("getRestHighLevelClient")
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    
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
    public List<Product> searchProducts2(String keyword, int page, int rows) {
        // 1. 使用 本地搜索查询构造器 创建 NativeSearchQuery，并设置搜索条件
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("name", keyword).operator(Operator.AND)) // 匹配查询条件
                .withPageable(PageRequest.of(page, rows)) // 设置分页参数
                .withHighlightBuilder(SearchUtils.getHighlightBuilder("name")) //高亮
                .withSort(new ScoreSortBuilder().order(SortOrder.DESC)) //降序
                .build();

        // 2. 使用elasticsearchRestTemplate进行搜索
        //      SearchHits、SearchHit 两个包导入与searchProducts中的两个导入不同
        org.springframework.data.elasticsearch.core.SearchHits<Product> searchHits = 
                elasticsearchRestTemplate.search(nativeSearchQuery, Product.class);

        // 3. 处理搜索结果
        List<Product> productList = new ArrayList<>();
        for (org.springframework.data.elasticsearch.core.SearchHit<Product> searchHit : searchHits) { // 获取搜索到的数据
//            Product content = searchHit.getContent();
//            Product product = new Product();
//            BeanUtils.copyProperties(content, product);
            Product product = searchHit.getContent();
            
            // 4. 处理高亮
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            for (String key : highlightFields.keySet()) {
                if (StringUtils.equals(key, "name")) {
                    List<String> list = highlightFields.get(key);
                    for (String s : list) {
                        product.setName(s);
                    }
                }
            }
//            for (Map.Entry<String, List<String>> highlightFieldsStr : highlightFields.entrySet()){
//                String key = highlightFieldsStr.getKey();
//                if (StringUtils.equals(key, "name")){
//                    List<String> values = highlightFieldsStr.getValue();
//                    StringBuilder sb = new StringBuilder();
//                    for (String s : values){
//                        sb.append(s);
//                    }
//                    product.setName(sb.toString());
//                }
//            }
            
            productList.add(product);
        }
        return productList;
    }

    @Override
    public Product addProduct(Product product) {
        product.setId(currentTime());
        return productDao.save(product);
    }

    @Override
    public void deleteProduct(Product product) {
        productDao.delete(product);
    }

    @Override
    public Product updateProduct(Product product) {
        return productDao.save(product);
    }

    @Override
    public Optional<Product> findById(String id) {
        return productDao.findById(id);
    }

    private int currentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmss");
        String time= sdf.format(new Date());
        return Integer.parseInt(time);
    }
}
