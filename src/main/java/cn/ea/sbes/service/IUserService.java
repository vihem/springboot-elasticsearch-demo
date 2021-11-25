package cn.ea.sbes.service;

import cn.ea.sbes.pojo.User;

import java.io.IOException;
import java.util.List;

public interface IUserService {

    void addUser(User user) throws IOException;
    void addMultiUser(List<User> users) throws IOException;

    void getUser(String id) throws IOException;
    
    List<User> searchUsers(String keyword, int page, int rows);
}
