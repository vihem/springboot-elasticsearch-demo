package cn.ea.sbes.service.impl;

import cn.ea.sbes.pojo.User;
import cn.ea.sbes.service.IUserService;
import cn.ea.sbes.util.SearchUtils;
import com.alibaba.fastjson.JSON;
import org.apache.commons.codec.binary.StringUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements IUserService {
    private final static String indexName = "sb_es_user";

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    
//    @Qualifier("getRestHighLevelClient")
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    
    @Override
    public void addUser(User user) {
        IndexRequest request = new IndexRequest(indexName);
        request.id(user.getId());
        //对象转为json
        request.source(JSON.toJSONString(user), XContentType.JSON);

        try {
            //发送
            restHighLevelClient.index(request, RequestOptions.DEFAULT);
            restHighLevelClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addMultiUser(List<User> users) {
        //创建批量请求
        BulkRequest bulkRequest = new BulkRequest();
        //超时时间
        bulkRequest.timeout("10s");
        //模拟数据
//        int i=0 ;   //user.getId()
        //批量插入
        for(User user:users){
            bulkRequest.add(
                    new IndexRequest(indexName)
                            .id(user.getId())
                            .source(JSON.toJSONString(user), XContentType.JSON)
            );
        }
        try {
            //发送请求
            BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            //获取是否失败标志
            System.out.println(bulk.hasFailures());
            restHighLevelClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getUser(String id) {
        try {
            //获取索引内容看看
            GetRequest getRequest = new GetRequest(indexName, id);
            GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
            System.out.println(getResponse);
        } catch (Exception e) {
            e.printStackTrace();   
        }
    }
    /**
     * @param page 页数
     * @param rows 行数
     * @param keyword 搜索关键字
     * @return List<User>
     */
    @Override
    public List<User> searchUsers(String keyword, int page, int rows) {
        List<User> userList = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, rows); // 设置分页参数
        NativeSearchQuery nativeSearchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND)) // match查询
                .withPageable(pageable) //分页
                .withHighlightBuilder(SearchUtils.getHighlightBuilder("title")) //高亮
                .withSort(new ScoreSortBuilder().order(SortOrder.DESC))
                .build();
        SearchHits<User> searchHits = elasticsearchRestTemplate.search(nativeSearchQuery, User.class);
        for (SearchHit<User> searchHit : searchHits) { // 获取搜索到的数据
            User content = searchHit.getContent();
            User user = new User();
            BeanUtils.copyProperties(content, user);
            
            //处理高亮
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            for (Map.Entry<String, List<String>> highlightFieldsStr : highlightFields.entrySet()){
                String key = highlightFieldsStr.getKey();
                if (StringUtils.equals(key, "title")){
                    List<String> fragments = highlightFieldsStr.getValue();
                    StringBuilder sb = new StringBuilder();
                    for (String fragment : fragments){
                        sb.append(fragment);
                    }
                    user.setTitle(sb.toString());
                }
            }
            userList.add(user);
        }
        return userList;
    }
    
}
