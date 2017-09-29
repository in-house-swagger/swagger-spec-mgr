package me.suwash.swagger.spec.manager.sv.specification;

import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.util.ValidationUtils;
import me.suwash.swagger.spec.manager.infra.validation.group.Create;
import me.suwash.swagger.spec.manager.infra.validation.group.Delete;
import me.suwash.swagger.spec.manager.infra.validation.group.Read;
import me.suwash.swagger.spec.manager.infra.validation.group.Update;
import me.suwash.swagger.spec.manager.sv.domain.Branch;
import me.suwash.swagger.spec.manager.sv.infra.BaseSpec;

import org.springframework.stereotype.Component;

@Component
public class BranchSpec extends BaseSpec {

    public void canFind(final Branch branch) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(branch, Read.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(SPECIFICATION_ERROR);
    }

    public void canAdd(final Branch branch) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(branch, Create.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(SPECIFICATION_ERROR);
    }

    public void canRename(final Branch branch, final String toBranch) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(branch, Update.class)) isValid = false;

        // 複数項目関連チェック
        try {
            ValidationUtils.notEmpty("toBranch", toBranch);
        } catch (SpecMgrException e) {
            addError(Branch.class, e);
            isValid = false;
        }

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(SPECIFICATION_ERROR);
    }

    public void canDelete(final Branch branch) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(branch, Delete.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(SPECIFICATION_ERROR);
    }

    public void canMerge(final Branch from, final Branch to) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(from, Read.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        // 関連データチェック
        try {
            ValidationUtils.notEmpty("toBranch", to.getId());
        } catch (SpecMgrException e) {
            addError(Branch.class, e);
            isValid = false;
        }

        if (!isValid) throw new SpecMgrException(SPECIFICATION_ERROR);
    }

    public void canSwitchBranch(final Branch branch) {
        canFind(branch);
    }
}
