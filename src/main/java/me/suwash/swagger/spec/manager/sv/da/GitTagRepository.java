package me.suwash.swagger.spec.manager.sv.da;

import java.util.List;

public interface GitTagRepository {

    List<String> tagList();

    boolean isExistTag(String name);

    void addTag(String from, String to);

    void renameTag(String from, String to);

    void removeTag(String name);

}
