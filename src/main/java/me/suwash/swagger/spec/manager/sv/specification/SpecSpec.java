package me.suwash.swagger.spec.manager.sv.specification;

import java.io.File;
import java.util.List;

import me.suwash.swagger.spec.manager.infra.config.ApplicationProperties;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtils;
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

    private final String DIRNAME_MERGED = "res";

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

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }

    public void canAdd(final Spec spec) {
        boolean isValid = true;
        // 単項目チェック
        if (!isValid(spec, Create.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);

        // 関連データチェック
        final File outputDir = new File(getSplitOutputDir(spec));
        if (outputDir.isDirectory()) {
            addError(spec.getClass(),
                MessageConst.DATA_ALREADY_EXIST,
                spec.getClass().getSimpleName(), "id", spec.getId());
            isValid = false;
        }

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }

    public void canUpdate(final Spec spec) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(spec, Update.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);

        // 関連データチェック
        final File outputDir = new File(getSplitOutputDir(spec));
        if (!outputDir.isDirectory()) {
            addError(spec.getClass(),
                MessageConst.DATA_NOT_EXIST,
                spec.getClass().getSimpleName(), "id", spec.getId());
            isValid = false;
        }

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }

    public void canDelete(final Spec spec) {
        boolean isValid = true;

        // 単項目チェック
        if (!isValid(spec, Delete.class)) isValid = false;

        // 複数項目関連チェック
        // なし

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);

        // 関連データチェック
        final File outputDir = new File(getSplitOutputDir(spec));
        if (!outputDir.isDirectory()) {
            addError(spec.getClass(),
                MessageConst.DATA_NOT_EXIST,
                spec.getClass().getSimpleName(), "id", spec.getId());
            isValid = false;
        }

        if (!isValid) throw new SpecMgrException(MessageConst.SPECIFICATION_ERROR);
    }

    public String getSplitOutputDir(final Spec spec) {
        return SwaggerSpecUtils.getSplitOutputDir(getSplitDir(), spec.getId());
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
