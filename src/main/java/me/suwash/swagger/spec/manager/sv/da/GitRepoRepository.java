package me.suwash.swagger.spec.manager.sv.da;

public interface GitRepoRepository {

    boolean isExist();

    void init();

    void pull();

    void push();

}
