package me.suwash.swagger.spec.manager.sv.specification;

import java.util.List;

import me.suwash.swagger.spec.manager.infra.config.ApplicationProperties;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtils;
import me.suwash.swagger.spec.manager.infra.util.ValidationUtils;
import me.suwash.swagger.spec.manager.infra.validation.group.Create;
import me.suwash.swagger.spec.manager.infra.validation.group.Delete;
import me.suwash.swagger.spec.manager.infra.validation.group.Read;
import me.suwash.swagger.spec.manager.infra.validation.group.Update;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.infra.BaseSpec;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SpecSpec extends BaseSpec {

    private static final String DIRNAME_MERGED = "res";

    @Autowired
    private ApplicationProperties props;
    @Autowired
    private SpecMgrContext context;

    public void canFind(final Spec spec) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(spec, Read.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        // 関連データチェック
        // なし

        if (!isValid) throw new SpecMgrException(SPECIFICATION_ERROR);
    }

    public void canAdd(final Spec spec) {
        boolean isValid = true;
        // 単項目チェック
        if (!isValid(spec, Create.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        if (!isValid) throw new SpecMgrException(SPECIFICATION_ERROR);

        // 関連データチェック
        // Git作業ディレクトリ
        try {
            ValidationUtils.existDir(props.getUserRepoDir(context.getCommitInfo()));
        } catch (SpecMgrException e) {
            addError(Spec.class, e);
            isValid = false;
        }
        // 出力ディレクトリ
        try {
            ValidationUtils.notExistDir(getSplitOutputDir(spec));
        } catch (SpecMgrException e) {
            addError(Spec.class, e);
            isValid = false;
        }

        if (!isValid) throw new SpecMgrException(SPECIFICATION_ERROR);
    }

    public void canUpdate(final Spec spec) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(spec, Update.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        if (!isValid) throw new SpecMgrException(SPECIFICATION_ERROR);

        // 関連データチェック
        isValid = checkInit(spec);

        if (!isValid) throw new SpecMgrException(SPECIFICATION_ERROR);
    }

    public void canDelete(final Spec spec) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(spec, Delete.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        if (!isValid) throw new SpecMgrException(SPECIFICATION_ERROR);

        // 関連データチェック
        isValid = checkInit(spec);

        if (!isValid) throw new SpecMgrException(SPECIFICATION_ERROR);
    }

    private boolean checkInit(final Spec spec) {
        try {
            ValidationUtils.existDir(getSplitOutputDir(spec));
            return true;

        } catch (SpecMgrException e) {
            addError(Spec.class, e);
            return false;
        }
    }

    public String getSplitOutputDir(final Spec spec) {
        return SwaggerSpecUtils.getSplitDir(getSplitDir(), spec.getId());
    }

    public String getSplitDir() {
        return props.getDirSpecs(currentCommitInfo());
    }

    public String getMergedDir() {
        return props.getUserDir(currentCommitInfo()) + "/" + DIRNAME_MERGED;
    }

    private CommitInfo currentCommitInfo() {
        return context.getCommitInfo();
    }

    public List<String> getSplitIgnoreRegexList() {
        return props.getSplitIgnoreRegexList();
    }
}
