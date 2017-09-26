package me.suwash.swagger.spec.manager.da.repository;

import java.io.File;
import java.util.List;

import me.suwash.swagger.spec.manager.da.infra.BaseSubProcessRepository;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.util.SubProcess.ProcessResult;
import me.suwash.swagger.spec.manager.sv.da.GitRepository;
import me.suwash.swagger.spec.manager.sv.specification.BranchSpec;
import me.suwash.swagger.spec.manager.sv.specification.TagSpec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class GitRepositoryImpl extends BaseSubProcessRepository implements GitRepository {

    @Autowired
    private BranchSpec branchSpec;
    @Autowired
    private TagSpec tagSpec;

    @Autowired
    private SpecMgrContext context;

    private CommitInfo commitInfo() {
        return context.getCommitInfo();
    }

    private String appendCommitUser(final String command) {
        final StringBuilder sb = new StringBuilder();
        sb.append(command);

        final CommitInfo commitInfo = commitInfo();
        if (commitInfo != null && !StringUtils.isEmpty(commitInfo.getUser()))
            sb.append(" ").append(commitInfo.getUser());

        return sb.toString();
    }

    @Override
    public boolean isExist() {
        final File userRepoDir = new File(props.getUserRepoDir(commitInfo()));
        return userRepoDir.isDirectory();
    }

    @Override
    public void init() {
        final StringBuilder commitArgs = new StringBuilder();
        final CommitInfo commitInfo = commitInfo();
        if (commitInfo != null) {
            commitInfo.canInit();
            commitArgs.append(" ").append(commitInfo.getUser());
            commitArgs.append(" ").append(commitInfo.getEmail());
        }

        subProc(props.getDirBin() + "/git/clone.sh" + commitArgs, "git clone");
    }

    @Override
    public void pull() {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/pull.sh");
        subProc(command, "git pull");
    }

    @Override
    public void push() {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/push.sh " + commitMessage());
        subProc(command, "git push");
    }

    private String commitMessage() {
        String commitMessage = props.getDefaultCommitMessage();
        // コミット情報.コミットメッセージを優先
        final CommitInfo commitInfo = commitInfo();
        if (commitInfo != null && !StringUtils.isEmpty(commitInfo.getMessage()))
            commitMessage = commitInfo.getMessage();

        // ダブルクォートで括る
        if (!"\"".equals(commitMessage.indexOf(0)))
            return "\"" + commitMessage + "\"";
        return commitMessage;
    }

    @Override
    public List<String> branchList() {
        final String command = appendCommitUser(props.getDirBin() + "/git/branch_list.sh");
        final ProcessResult result = subProc(command, "git branch_list");
        return result.getStdout();
    }

    @Override
    public boolean isExistBranch(final String name) {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/branch_is_exist.sh " + name);
        final ProcessResult result = subProc(command, "git branch_is_exist");
        if ("true".equals(result.getStdout().get(0))) return true;
        return false;
    }

    @Override
    public void addBranch(final String from, final String to) {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/branch_add.sh " + from + " " + to);
        subProc(command, "git branch_add");
    }

    @Override
    public void renameBranch(final String from, String to) {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/branch_rename.sh " + from + " " + to);
        subProc(command, "git branch_rename");
    }

    @Override
    public void removeBranch(final String name) {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/branch_remove.sh " + name);
        subProc(command, "git branch_remove");
    }

    @Override
    public void mergeBranch(final String from, String to) {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/branch_merge.sh " + from + " " + to);
        subProc(command, "git branch_merge");
    }

    @Override
    public void switchBranch(final String name) {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/switch.sh " + name);
        subProc(command, "git switch");
    }

    @Override
    public List<String> tagList() {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/tag_list.sh");
        final ProcessResult result = subProc(command, "git tag_list");
        return result.getStdout();
    }

    @Override
    public boolean isExistTag(final String name) {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/tag_is_exist.sh " + name);
        final ProcessResult result = subProc(command, "git tag_is_exist");
        if ("true".equals(result.getStdout().get(0))) return true;
        return false;
    }

    @Override
    public void addTag(final String from, final String to) {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/tag_add.sh " + from + " " + to + " " + commitMessage());
        subProc(command, "git tag_add");
    }

    @Override
    public void renameTag(final String from, String to) {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/tag_rename.sh " + from + " " + to);
        subProc(command, "git tag_rename");
    }

    @Override
    public void removeTag(final String name) {
        final String command = appendCommitUser(
            props.getDirBin() + "/git/tag_remove.sh " + name);
        subProc(command, "git tag_remove");
    }

}
