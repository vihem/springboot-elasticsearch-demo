package cn.ea.sbes.test;

import java.io.IOException;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;

public class TestElasticSearch4J {
    
    private static final RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("192.168.25.137", 9200, "http")));

    public static void main(String[] args) throws IOException {
        String indexName = "ea";

        if(!checkExistIndex(indexName)){
            createIndex(indexName);
        }

        if(checkExistIndex(indexName)){
            deleteIndex(indexName);
        }
        checkExistIndex(indexName);
        client.close();
    }

    private static boolean checkExistIndex(String indexName) throws IOException {
        boolean result =true;
        try {
            OpenIndexRequest openIndexRequest = new OpenIndexRequest(indexName);
            client.indices().open(openIndexRequest, RequestOptions.DEFAULT).isAcknowledged();
        } catch (ElasticsearchStatusException ex) {
            String m = "Elasticsearch exception [type=index_not_found_exception, reason=no such index ["+ indexName +"]]";
//            System.out.println("ex.getMessage()= "+ex.getMessage());
            if (m.equals(ex.getMessage())) {
                result = false;
            }
        }
        if(result) System.out.println("==================================================== 索引:" +indexName + " 是存在的");
        else System.out.println("==================================================== 索引:" +indexName + " 不存在");

        return result;
    }

    private static void deleteIndex(String indexName) throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println("==================================================== 删除了索引："+indexName);

    }

    private static void createIndex(String indexName) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(indexName);
        client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println("==================================================== 创建了索引："+indexName);
    }

}