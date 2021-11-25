package cn.ea.sbes.controller;

import cn.ea.sbes.pojo.User;
import cn.ea.sbes.service.IUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/user")
@Api("user")
public class UserController {
    @Autowired
    private IUserService userService;

    @ApiOperation("search")
    @GetMapping("/search/{keyword}/{page}/{rows}")
    List<User> search(@PathVariable String keyword, @PathVariable int page, @PathVariable int rows) throws IOException{
        return userService.searchUsers(keyword, page, rows);
    }
}
