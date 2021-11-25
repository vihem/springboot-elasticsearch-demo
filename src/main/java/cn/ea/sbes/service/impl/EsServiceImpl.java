package cn.ea.sbes.service.impl;

import cn.ea.sbes.service.IEsService;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
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
            return indexName + " already exists.";
        }
        try {
            CreateIndexRequest request = new CreateIndexRequest(indexName);
            restHighLevelClient.indices().create(request, RequestOptions.DEFAULT);
            return "Successful";
        } catch (IOException e) {
            e.printStackTrace();
            return "Failed";
        }
    }

    /**
     * 这种方式创建索引很奇葩，会报错，但是索引能创建成功，不建议使用，只用于对比: 
     * Elasticsearch exception [type=resource_already_exists_exception, reason=index [sb_es_product/iHwe11k9T0yNFrI_R9oipw] already exists]
     */
    @Override
    public <T> String createIndex2(Class<T> tClass) throws IOException {
        boolean b = elasticsearchOperations.indexOps(tClass).create();
        return b ? "Successful":"Failed";
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
        if(result) 
            System.out.println("==================================================== 索引 " +indexName + " 存在");
        else
            System.out.println("==================================================== 索引 " +indexName + " 不存在");

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
            System.out.println("检查到服务器上 "+"id="+id+ "的文档不存在");
            return "检查到服务器上 "+"id="+id+ "的文档不存在";
        } else {
            String source = response.getSourceAsString();
            System.out.print("获取到服务器上 "+"id="+id+ "的文档内容是：");
            System.out.println(source);
            return source;
        }
    }
}
