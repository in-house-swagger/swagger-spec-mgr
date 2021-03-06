package me.suwash.swagger.spec.manager.da.repository;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import me.suwash.swagger.spec.manager.da.infra.BaseSubProcessRepository;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.util.SubProcess.ProcessResult;
import me.suwash.swagger.spec.manager.sv.da.GitRepository;
import me.suwash.swagger.spec.manager.sv.specification.BranchSpec;
import me.suwash.swagger.spec.manager.sv.specification.TagSpec;

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
    final String command = appendCommitUser(props.getDirBin() + "/git/pull.sh");
    subProc(command, "git pull");
  }

  @Override
  public void push() {
    final String command = appendCommitUser(props.getDirBin() + "/git/push.sh " + commitMessage());
    subProc(command, "git push");
  }

  private String commitMessage() {
    String commitMessage = props.getDefaultCommitMessage();
    // コミット情報.コミットメッセージを優先
    final CommitInfo commitInfo = commitInfo();
    if (commitInfo != null && !StringUtils.isEmpty(commitInfo.getMessage()))
      commitMessage = commitInfo.getMessage();

    // ダブルクォートで括る
    if ('"' != commitMessage.charAt(0))
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
  public String getCurrentBranch() {
    final String command = appendCommitUser(props.getDirBin() + "/git/branch_get_current.sh ");
    final ProcessResult result = subProc(command, "git branch_get_current");
    return result.getStdout().get(0);
  }

  @Override
  public boolean isExistBranch(final String name) {
    final String command = appendCommitUser(props.getDirBin() + "/git/branch_is_exist.sh " + name);
    final ProcessResult result = subProc(command, "git branch_is_exist");
    return "true".equals(result.getStdout().get(0));
  }

  @Override
  public void addBranch(final String from, final String to) {
    if (isExistBranch(to))
      branchSpec.alreadyExist(to);

    final String command =
        appendCommitUser(props.getDirBin() + "/git/branch_add.sh " + from + " " + to);
    subProc(command, "git branch_add");
  }

  @Override
  public void renameBranch(final String from, String to) {
    if (!isExistBranch(from))
      branchSpec.notExist(from);
    if (isExistBranch(to))
      branchSpec.alreadyExist(to);

    final String command =
        appendCommitUser(props.getDirBin() + "/git/branch_rename.sh " + from + " " + to);
    subProc(command, "git branch_rename");
  }

  @Override
  public void removeBranch(final String name) {
    if (!isExistBranch(name))
      branchSpec.notExist(name);

    final String command = appendCommitUser(props.getDirBin() + "/git/branch_remove.sh " + name);
    subProc(command, "git branch_remove");
  }

  @Override
  public void mergeBranch(final String from, String to) {
    if (!isExistBranch(from))
      branchSpec.notExist(from);
    if (!isExistBranch(to))
      branchSpec.notExist(to);

    final String command =
        appendCommitUser(props.getDirBin() + "/git/branch_merge.sh " + from + " " + to);
    subProc(command, "git branch_merge");
  }

  @Override
  public void switchBranch(final String name) {
    if (!isExistBranch(name))
      branchSpec.notExist(name);

    final String command = appendCommitUser(props.getDirBin() + "/git/switch.sh " + name);
    subProc(command, "git switch");
  }

  @Override
  public List<String> tagList() {
    final String command = appendCommitUser(props.getDirBin() + "/git/tag_list.sh");
    final ProcessResult result = subProc(command, "git tag_list");
    return result.getStdout();
  }

  @Override
  public boolean isExistTag(final String name) {
    final String command = appendCommitUser(props.getDirBin() + "/git/tag_is_exist.sh " + name);
    final ProcessResult result = subProc(command, "git tag_is_exist");
    return "true".equals(result.getStdout().get(0));
  }

  @Override
  public void addTag(final String from, final String to) {
    if (isExistTag(to))
      tagSpec.alreadyExist(to);

    final String command = appendCommitUser(
        props.getDirBin() + "/git/tag_add.sh " + from + " " + to + " " + commitMessage());
    subProc(command, "git tag_add");
  }

  @Override
  public void renameTag(final String from, String to) {
    if (!isExistTag(from))
      tagSpec.notExist(from);
    if (isExistTag(to))
      tagSpec.alreadyExist(to);

    final String command =
        appendCommitUser(props.getDirBin() + "/git/tag_rename.sh " + from + " " + to);
    subProc(command, "git tag_rename");
  }

  @Override
  public void removeTag(final String name) {
    if (!isExistTag(name))
      tagSpec.notExist(name);

    final String command = appendCommitUser(props.getDirBin() + "/git/tag_remove.sh " + name);
    subProc(command, "git tag_remove");
  }

}
