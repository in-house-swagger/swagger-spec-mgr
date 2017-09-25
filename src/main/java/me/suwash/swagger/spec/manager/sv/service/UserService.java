package me.suwash.swagger.spec.manager.sv.service;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;

import java.util.List;

import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.da.UserRepository;
import me.suwash.swagger.spec.manager.sv.domain.User;
import me.suwash.swagger.spec.manager.sv.specification.UserSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserSpec userSpec;
    @Autowired
    private UserRepository userRepository;

    private User newUser(final String userId, final String email) {
        return new User(userSpec, userRepository, userId, email);
    }

    public List<String> idList() {
        return userRepository.idList();
    }

    public User findById(final String userId) {
        final User criteria = newUser(userId, null);
        userSpec.canFind(criteria);

        final User finded = userRepository.findById(userId);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array(User.class.getSimpleName(), "id", userId));
        return finded;
    }

    public User addUser(final String userId, final String email) {
        final User user = newUser(userId, email);
        user.add();
        return findById(userId);
    }

    public void deleteUser(final String userId) {
        final User user = newUser(userId, null);
        user.delete();
    }

}
