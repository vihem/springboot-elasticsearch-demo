package cn.ea.sbes;

import cn.ea.sbes.pojo.D2;
import cn.ea.sbes.pojo.Product;
import cn.ea.sbes.pojo.User;
import cn.ea.sbes.service.IEsService;
import cn.ea.sbes.service.IProductService;
import cn.ea.sbes.service.IUserService;
import cn.ea.sbes.test.TestD2;
import cn.ea.sbes.util.ProductUtil;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ApplicationTests {

    @Autowired
    private IEsService esService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IProductService productService;
    
    @Test
    public void createIndex(){
        try {
            esService.createIndex("sb_es_product");
            System.out.println(esService.isIndexExist("sb_es_product"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testCreateIndex3(){
        try {
            esService.createIndex2(User.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testProduct() {
        try {
//            List<Product> products = ProductUtil.file2list("src/main/resources/140k_products.txt");
//            System.out.println(products.size());//147939
//            productService.batchInsert(products);
            
            String keyword = "时尚连衣裙";
            int start = 0;
            int count = 10;
            List<Product> products = productService.searchProducts2(keyword, start, count);
            System.out.println(Arrays.toString(products.toArray()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void test(){
        try {
            esService.getDocument("sb_es_product", "157939");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testUser() throws IOException {
        //测试对象的操作
//        userService.addUser(new User("5","测试", 13));
//
//        //测试批量插入
//        ArrayList<User> users = new ArrayList<>();
//        users.add(new User("1","张1",1));
//        users.add(new User("2","张2",2));
//        users.add(new User("3","张3",3));
//        users.add(new User("4","张4",4));
//
//        userService.addMultiUser(users);
//        userService.getUser("1");
        List<User> products = userService.searchUsers("手机", 0, 10);
        System.out.println(Arrays.toString(products.toArray()));
    }

    @Autowired
    ElasticsearchOperations elasticsearchOperations;
    @Test
    void testD2(){
        elasticsearchOperations.indexOps(D2.class).create();
        
//        TestD2.createIndex3(elasticsearchOperations, D2.class);
        TestD2.putD2(elasticsearchOperations,new D2(1, "增加一个"));
        TestD2.getD2(elasticsearchOperations,"1", D2.class);
    }
}
