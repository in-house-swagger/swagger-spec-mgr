package me.suwash.swagger.spec.manager.da.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.suwash.swagger.spec.manager.da.infra.BaseSubProcessRepository;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.sv.da.GitRepoRepository;
import me.suwash.swagger.spec.manager.sv.da.UserRepository;
import me.suwash.swagger.spec.manager.sv.domain.User;
import me.suwash.swagger.spec.manager.sv.specification.UserSpec;
import me.suwash.util.FileUtils;
import me.suwash.util.FindUtils;
import me.suwash.util.FindUtils.FileType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryImpl extends BaseSubProcessRepository implements UserRepository {

    @Autowired
    private UserSpec userSpec;

    @Autowired
    private GitRepoRepository gitRepoRepository;

    @Autowired
    private SpecMgrContext context;

    @Override
    public List<String> idList() {
        final List<String> idList = getUserIdList();
        return Collections.unmodifiableList(idList);
    }

    private List<String> getUserIdList() {
        final List<String> idList = new ArrayList<>();
        for (final File curDir : getUserDirList()) {
            idList.add(curDir.getName());
        }
        return idList;
    }

    private List<File> getUserDirList() {
        final String dirUsers = userSpec.getUsersDir();
        if (!new File(dirUsers).exists()) return new ArrayList<>();

        return FindUtils.find(dirUsers, 1, 1, FileType.Directory);
    }

    @Override
    public User findById(final String id) {
        if (!getUserIdList().contains(id)) return null;

        return new User(userSpec, this, id, null);
    }

    @Override
    public void add(User user) {
        context.putCommitInfo(new CommitInfo(user.getId(), user.getEmail()));
        gitRepoRepository.init();
    }

    // @Override
    // public void update(User user) {
    // }

    @Override
    public void delete(User user) {
        FileUtils.rmdirs(userSpec.getUserDir(user));
    }

}
