package me.suwash.swagger.spec.manager.sv.da;

public interface GitRepoRepository {

    void init();

    void pull();

    void push();

}
