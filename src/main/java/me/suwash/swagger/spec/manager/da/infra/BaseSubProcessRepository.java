package me.suwash.swagger.spec.manager.da.infra;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.MSGCD_ERRORHANDLE;
import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.util.SubProcess;
import me.suwash.swagger.spec.manager.infra.util.SubProcess.ProcessResult;

@lombok.extern.slf4j.Slf4j
public abstract class BaseSubProcessRepository extends BaseRepository {

  protected ProcessResult subProc(final String command, final String procName) {
    final ProcessResult result = SubProcess.newSubProcess().workDir(System.getProperty("user.dir"))
        .command(command).envMap(props.getScriptEnv()).execute();

    printDebug(command, result);

    if (SubProcess.EXITCODE_ERROR <= result.getExitCode()) {
      throwSubProcessException(procName, result);
    }

    return result;
  }

  private void printDebug(final String command, final ProcessResult result) {
    if (log.isDebugEnabled()) {
      log.debug("-- command: " + command);
      log.debug("-- stdout");
      result.getStdout().forEach(log::debug);
      log.debug("-- stderr");
      result.getStderr().forEach(log::debug);
    }
  }

  private void throwSubProcessException(final String procName, final ProcessResult result) {
    final StringBuilder sb = new StringBuilder();
    sb.append(procName).append("\n");
    result.getStderr().forEach(curLine -> sb.append(curLine).append("\n"));
    throw new SpecMgrException(MSGCD_ERRORHANDLE, array("SubProcess", sb));
  }
}
