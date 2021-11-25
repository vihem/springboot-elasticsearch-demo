package cn.ea.sbes.service;

import java.io.IOException;

public interface IEsService {
    String createIndex(String indexName) throws IOException;
    <T> String createIndex2(Class<T> tClass) throws IOException;
    
    boolean isIndexExist(String indexName) throws IOException;
    boolean checkExistIndex(String indexName) throws IOException;

    boolean deleteIndex(String indexName) throws IOException;

    String getDocument(String indexName, String id) throws IOException;
}
