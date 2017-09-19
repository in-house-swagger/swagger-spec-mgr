package me.suwash.swagger.spec.manager.sv.domain;

import javax.validation.constraints.NotNull;

import me.suwash.swagger.spec.manager.sv.da.GitBranchRepository;
import me.suwash.swagger.spec.manager.sv.domain.gen.BranchGen;

public class Branch extends BranchGen {
    @NotNull
    private GitBranchRepository repository;

    public Branch(final GitBranchRepository repository, final String id) {
        super(id, null);
        this.repository = repository;
    }

    public void add(final String gitObject) {
        this.gitObject = gitObject;
        repository.addBranch(this.gitObject, this.id);
    }

    public void rename(final String toBranch) {
        repository.renameBranch(this.id, toBranch);
    }

    public void delete() {
        repository.removeBranch(this.id);
    }
}
