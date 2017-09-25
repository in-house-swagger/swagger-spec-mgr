package me.suwash.swagger.spec.manager.sv.specification;

import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.validation.group.Create;
import me.suwash.swagger.spec.manager.infra.validation.group.Delete;
import me.suwash.swagger.spec.manager.infra.validation.group.Read;
import me.suwash.swagger.spec.manager.infra.validation.group.Update;
import me.suwash.swagger.spec.manager.sv.domain.Branch;
import me.suwash.swagger.spec.manager.sv.infra.BaseSpec;

import org.apache.commons.lang3.StringUtils;
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

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }

    public void canAdd(final Branch branch) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(branch, Create.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }

    public void canRename(final Branch branch, final String toBranch) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(branch, Update.class)) isValid = false;

        // 複数項目関連チェック
        if (StringUtils.isEmpty(toBranch)) {
            addError(Branch.class, MessageConst.CHECK_NOTNULL, "toBranch");
            isValid = false;
        }

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }

    public void canDelete(final Branch branch) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(branch, Delete.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }

    public void canMerge(final Branch from, final Branch to) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(from, Read.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        // 関連データチェック
        if (to == null || StringUtils.isEmpty(to.getId())) {
            addError(Branch.class, MessageConst.CHECK_NOTNULL, "toBranch");
            isValid = false;
        }

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }

    public void canSwitchBranch(final Branch branch) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(branch, Read.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }
}
