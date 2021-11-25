package cn.ea.sbes.service;

import cn.ea.sbes.pojo.Product;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface IProductService {
    
    String batchInsert(List<Product> products) throws IOException;

    /**
     * 根据 keyword 搜索
     * @param keyword 搜索关键字
     * @param start 页数
     * @param count 行数
     * @return List<Product>
     */
    List<Product> searchProducts(String keyword, int start, int count) throws IOException;
    /**
     * 根据 keyword 搜索
     * @param page 页数
     * @param rows 行数
     * @param keyword 搜索关键字
     * @return List<User>
     */
    List<Product> searchProducts2(String keyword, int page, int rows);

    Product addProduct(Product product);
    void deleteProduct(Product product);
    Product updateProduct(Product product);
    Optional<Product> findById(String id);
}
