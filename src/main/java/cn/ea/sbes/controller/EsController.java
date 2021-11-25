package cn.ea.sbes.controller;

import cn.ea.sbes.pojo.Product;
import cn.ea.sbes.service.IEsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

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
