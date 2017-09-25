package me.suwash.swagger.spec.manager.sv.specification;

import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.validation.group.Create;
import me.suwash.swagger.spec.manager.infra.validation.group.Delete;
import me.suwash.swagger.spec.manager.infra.validation.group.Read;
import me.suwash.swagger.spec.manager.infra.validation.group.Update;
import me.suwash.swagger.spec.manager.sv.domain.Tag;
import me.suwash.swagger.spec.manager.sv.infra.BaseSpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class TagSpec extends BaseSpec {

    public void canFind(final Tag tag) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(tag, Read.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }

    public void canAdd(final Tag tag) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(tag, Create.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }

    public void canRename(final Tag tag, final String toTag) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(tag, Update.class)) isValid = false;

        // 複数項目関連チェック
        if (StringUtils.isEmpty(toTag)) {
            addError(tag.getClass(), MessageConst.CHECK_NOTNULL, "toTag");
            isValid = false;
        }

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }

    public void canDelete(final Tag tag) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(tag, Delete.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }
}
