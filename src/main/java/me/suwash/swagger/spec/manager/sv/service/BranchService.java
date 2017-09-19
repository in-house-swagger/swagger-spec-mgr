package me.suwash.swagger.spec.manager.sv.service;

import java.util.List;

import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.da.GitBranchRepository;
import me.suwash.swagger.spec.manager.sv.domain.Branch;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BranchService {

    @Autowired
    private GitBranchRepository repository;

    public List<String> idList() {
        return repository.branchList();
    }

    public Branch findById(final String branch) {
        if (StringUtils.isEmpty(branch))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"branch"});

        if (repository.isExistBranch(branch)) return newBranch(branch);
        return null;
    }

    public Branch newBranch(final String branch) {
        if (StringUtils.isEmpty(branch))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"branch"});

        return new Branch(repository, branch);
    }

    public Branch mergeBranch(final String from, final String to) {
        if (StringUtils.isEmpty(from))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"from"});
        if (StringUtils.isEmpty(to))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"to"});

        repository.mergeBranch(from, to);
        return newBranch(to);
    }

    public Branch switchBranch(final String branch) {
        if (StringUtils.isEmpty(branch))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, new Object[] {"branch"});

        repository.switchBranch(branch);
        return newBranch(branch);
    }

}
