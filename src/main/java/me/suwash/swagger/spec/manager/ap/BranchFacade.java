package me.suwash.swagger.spec.manager.ap;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;

import java.util.List;

import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Branch;
import me.suwash.swagger.spec.manager.sv.service.BranchService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BranchFacade extends BaseFacade {

    @Autowired
    private BranchService service;

    public List<String> idList(final CommitInfo commitInfo) {
        registerCommitInfo(commitInfo);

        return service.idList();
    }

    public Branch findById(final CommitInfo commitInfo, final String branchId) {
        registerCommitInfo(commitInfo);

        final Branch finded = service.findById(branchId);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array("Branch", "branchId", branchId));

        return finded;
    }

    public Branch add(final CommitInfo commitInfo, final String gitObject, final String branchId) {
        if (StringUtils.isEmpty(gitObject))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("gitObject"));
        if (StringUtils.isEmpty(branchId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("branchId"));

        registerCommitInfo(commitInfo);

        final Branch tag = service.newBranch(branchId);
        tag.add(gitObject);
        return service.findById(branchId);
    }

    public Branch rename(final CommitInfo commitInfo, final String fromBranch, final String toBranch) {
        if (StringUtils.isEmpty(fromBranch))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("fromBranch"));
        if (StringUtils.isEmpty(toBranch))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("toBranch"));

        registerCommitInfo(commitInfo);

        final Branch finded = service.findById(fromBranch);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array("Branch", "branchId", fromBranch));

        finded.rename(toBranch);
        return service.findById(toBranch);
    }

    public void delete(final CommitInfo commitInfo, final String branchId) {
        if (StringUtils.isEmpty(branchId))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("branchId"));

        registerCommitInfo(commitInfo);

        final Branch finded = service.findById(branchId);
        if (finded == null)
            throw new SpecMgrException(MessageConst.DATA_NOT_EXIST, array("Branch", "branchId", branchId));

        finded.delete();
    }

    public Branch switchBranch(final CommitInfo commitInfo, final String toBranch) {
        if (StringUtils.isEmpty(toBranch))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("toBranch"));

        registerCommitInfo(commitInfo);

        return service.switchBranch(toBranch);
    }

    public Branch mergeBranch(final CommitInfo commitInfo, final String fromBranch, final String toBranch) {
        if (StringUtils.isEmpty(fromBranch))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("fromBranch"));
        if (StringUtils.isEmpty(toBranch))
            throw new SpecMgrException(MessageConst.CHECK_NOTNULL, array("toBranch"));

        registerCommitInfo(commitInfo);

        return service.mergeBranch(fromBranch, toBranch);
    }

}
