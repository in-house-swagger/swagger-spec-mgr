package me.suwash.swagger.spec.manager.sv.da;

import java.util.List;

public interface GitBranchRepository {

    List<String> branchList();

    boolean isExistBranch(String name);

    void addBranch(String from, String to);

    void renameBranch(String from, String to);

    void removeBranch(String name);

    void mergeBranch(String from, String to);

    void switchBranch(String name);

}
