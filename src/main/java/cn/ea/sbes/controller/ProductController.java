package cn.ea.sbes.controller;

import cn.ea.sbes.pojo.Product;
import cn.ea.sbes.service.IProductService;
import cn.ea.sbes.util.ProductUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/product")
@Api("product")
public class ProductController {
    
    @Autowired
    private IProductService productService;

    @ApiOperation("batchInsert")
    @GetMapping("/batchInsert")
    public String batchInsert() throws IOException {
        List<Product> products = ProductUtil.file2list("src/main/resources/140k_products.txt");
        System.out.println(products.size());//147939
        return "批量插入 " + productService.batchInsert(products);
    }
    
    @ApiOperation("search")
    @GetMapping("/search/{keyword}/{start}/{count}")
    public List<Product> searchProducts(@PathVariable String keyword,
                                        @PathVariable int start,
                                        @PathVariable int count) throws IOException {
        return productService.searchProducts(keyword,start,count);
    }
    @ApiOperation("search2")
    @GetMapping("/search2/{keyword}/{start}/{count}")
    public List<Product> searchProducts2(@PathVariable String keyword,
                                        @PathVariable int start,
                                        @PathVariable int count) throws IOException {
        return productService.searchProducts2(keyword,start,count);
    }
    
    @ApiOperation("add")
    @PostMapping("/add")
    public Product addProduct(@RequestBody Product product) throws Exception{
        return productService.addProduct(product);
    }

    @ApiOperation("delete")
    @RequestMapping("/delete")
    public void deleteProduct(@RequestBody Product product) throws Exception{
        productService.deleteProduct(product);
    }

    @ApiOperation("update")
    @PostMapping("/update")
    public Product updateProduct(@RequestBody Product product) throws Exception{
        return productService.updateProduct(product);
    }

    @ApiOperation("findById")
    @GetMapping("/findById/{id}")
    public Optional<Product> findById(@PathVariable String id) throws Exception{
        return productService.findById(id);
    }
}
