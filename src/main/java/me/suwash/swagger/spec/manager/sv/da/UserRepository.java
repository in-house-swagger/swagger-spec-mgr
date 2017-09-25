package me.suwash.swagger.spec.manager.sv.da;

import java.util.List;

import me.suwash.swagger.spec.manager.sv.domain.User;

public interface UserRepository {

    List<String> idList();

    User findById(String userId);

    void add(User user);

    // void update(User user);

    void delete(User user);
}
