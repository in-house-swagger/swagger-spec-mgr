package me.suwash.swagger.spec.manager.sv.domain;

import javax.validation.constraints.NotNull;

import me.suwash.swagger.spec.manager.sv.da.GitTagRepository;
import me.suwash.swagger.spec.manager.sv.domain.gen.TagGen;

public class Tag extends TagGen {
    @NotNull
    private GitTagRepository repository;

    public Tag(final GitTagRepository repository, final String id) {
        super(id, null);
        this.repository = repository;
    }

    public void add(final String gitObject) {
        this.gitObject = gitObject;
        repository.addTag(this.gitObject, this.id);
    }

    public void rename(final String toTag) {
        repository.renameTag(this.id, toTag);
    }

    public void delete() {
        repository.removeTag(this.id);
    }
}
