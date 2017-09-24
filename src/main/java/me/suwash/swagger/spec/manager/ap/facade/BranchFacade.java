package me.suwash.swagger.spec.manager.ap.facade;

import java.util.List;

import me.suwash.swagger.spec.manager.ap.dto.BranchDto;
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.infra.BaseFacade;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Branch;
import me.suwash.swagger.spec.manager.sv.service.BranchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BranchFacade extends BaseFacade {

    @Autowired
    private BranchService service;

    public IdListDto idList(final CommitInfo commitInfo) {
        registerCommitInfo(commitInfo);

        List<String> result = service.idList();
        return new IdListDto(context, result);
    }

    public BranchDto findById(final CommitInfo commitInfo, final String branchId) {
        registerCommitInfo(commitInfo);

        Branch result = null;
        try {
            result = service.findById(branchId);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new BranchDto(context, result);
    }

    public BranchDto add(final CommitInfo commitInfo, final String gitObject, final String branchId) {
        registerCommitInfo(commitInfo);

        // TODO facadeはserviceの1メソッドを呼ぶだけになるように、serviceでラップする。
        Branch result = null;
        try {
            final Branch branch = service.newBranch(branchId);
            branch.add(gitObject);
            result = service.findById(branchId);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new BranchDto(context, result);
    }

    public BranchDto rename(final CommitInfo commitInfo, final String fromBranch, final String toBranch) {
        registerCommitInfo(commitInfo);

        Branch result = null;
        try {
            final Branch finded = service.findById(fromBranch);
            finded.rename(toBranch);
            result = service.findById(toBranch);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new BranchDto(context, result);
    }

    public BranchDto delete(final CommitInfo commitInfo, final String branchId) {
        registerCommitInfo(commitInfo);

        try {
            final Branch finded = service.findById(branchId);
            finded.delete();
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new BranchDto(context, null);
    }

    public BranchDto switchBranch(final CommitInfo commitInfo, final String toBranch) {
        registerCommitInfo(commitInfo);

        Branch result = null;
        try {
            result = service.switchBranch(toBranch);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new BranchDto(context, result);
    }

    public BranchDto mergeBranch(final CommitInfo commitInfo, final String fromBranch, final String toBranch) {
        registerCommitInfo(commitInfo);

        Branch result = null;
        try {
            result = service.mergeBranch(fromBranch, toBranch);
        } catch (SpecMgrException e) {
            handleApplicationException(e);
        }
        return new BranchDto(context, result);
    }

}
