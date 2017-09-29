package me.suwash.swagger.spec.manager.infra.util;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.MSGCD_ERRORHANDLE;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

@lombok.AllArgsConstructor(access = AccessLevel.PRIVATE)
@lombok.extern.slf4j.Slf4j
public class SubProcess {

    public static final int EXITCODE_NOTEXECUTE = Integer.MIN_VALUE;
    public static final int EXITCODE_SUCCESS = 0;
    public static final int EXITCODE_WARN = 3;
    public static final int EXITCODE_ERROR = 6;
    public static final int EXITCODE_TIMEOUT = Integer.MAX_VALUE;

    private String command;
    private String workDir;
    private String stdout;
    private String stderr;
    private Map<String, String> env;
    private int timeoutSec;

    public static SubProcess newExecuter() {
        final LocalDateTime datetime = LocalDateTime.now();
        final String id = SubProcess.class.getSimpleName() +
            "_" + datetime.toString("yyyyMMdd_HHmmss_SSS") +
            "_" + Thread.currentThread().getName();
        final String workDir = "/tmp";
        final String stdout = workDir + "/" + id + "_stdout.log";
        final String stderr = workDir + "/" + id + "_stderr.log";
        final Map<String, String> env = new ConcurrentHashMap<>();
        final int defaultTimeoutSec = -1;

        return new SubProcess(StringUtils.EMPTY, workDir, stdout, stderr, env, defaultTimeoutSec);
    }

    public SubProcess command(final String command) {
        this.command = command;
        return this;
    }

    public SubProcess workDir(final String workDir) {
        this.workDir = workDir;
        return this;
    }

    public SubProcess stdout(final String stdout) {
        this.stdout = stdout;
        return this;
    }

    public SubProcess stderr(final String stderr) {
        this.stderr = stderr;
        return this;
    }

    public SubProcess env(final String var, final String value) {
        this.env.put(var, value);
        return this;
    }

    public SubProcess envMap(final Map<String, String> envMap) {
        this.env.putAll(envMap);
        return this;
    }

    public SubProcess timeout(final int seconds) {
        this.timeoutSec = seconds;
        return this;
    }

    private ProcessBuilder newProcessBuilder(
        final String command,
        final String workDir,
        final String stdoutFilePath,
        final String stderrFilePath) {

        // 外部プロセス実行コマンド
        final List<String> execCmdList = getExecCmdList(command);

        // ProcessBuilderを返却
        return new ProcessBuilder(execCmdList)
            .directory(new File(workDir))
            .redirectInput(Redirect.PIPE)
            // 標準出力ログ
            .redirectOutput(Redirect.appendTo(new File(stdoutFilePath)))
            // 標準エラーログ
            .redirectError(Redirect.appendTo(new File(stderrFilePath)));
    }

    private List<String> getExecCmdList(final String command) {
        return Arrays.asList(array(
            "/bin/bash", "-c",
            "[ -f ~/.bash_profile ] && . ~/.bash_profile; " + command
            ));
    }

    private String[] array(final String... strings) {
        return strings;
    }

    public ProcessResult execute() {
        ValidationUtils.notEmpty("command", command);

        final ProcessBuilder builder = newProcessBuilder(command, workDir, stdout, stderr);
        builder.environment().putAll(env);

        int exitCode = EXITCODE_NOTEXECUTE;
        Process process = null;
        try {
            log.trace("SubProcess: " + builder.command().toString());
            process = builder.start();
            // 外部プロセスの標準入力（外部プロセスへのoutputstream）を閉じる
            closeStreams(process);

            boolean isExit = true;
            if (timeoutSec < 0) {
                process.waitFor();
            } else {
                isExit = process.waitFor(timeoutSec, TimeUnit.SECONDS);
            }

            if (isExit) {
                exitCode = process.exitValue();
            } else {
                exitCode = EXITCODE_TIMEOUT;
            }

            process.destroy();
            log.trace("SubProcess: exit_code=" + exitCode);

        } catch (
            IOException | InterruptedException e) {
            if (process != null) closeStreams(process);
            throw new SpecMgrException(MSGCD_ERRORHANDLE, array(command, e.getMessage()), e);
        } finally {
            if (process != null) closeStreams(process);
        }

        // 結果を返却
        return new ProcessResult(exitCode, stdout, stderr);
    }

    private void closeStreams(Process process) {
        try {
            process.getInputStream().close();
            process.getOutputStream().close();
            process.getErrorStream().close();
        } catch (IOException e) {
            throw new SpecMgrException(MSGCD_ERRORHANDLE, array("closeStreams", e.getMessage()), e);
        }
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    public class ProcessResult {
        private final int exitCode;
        private final String stdoutFilePath;
        private final String stderrFilePath;

        public List<String> getStdout() {
            try {
                return Files.readAllLines(Paths.get(stdoutFilePath), Charset.defaultCharset());
            } catch (IOException e) {
                ValidationUtils.fileCantRead(stdoutFilePath, e);
                return new ArrayList<>();
            }
        }

        public List<String> getStderr() {
            try {
                return Files.readAllLines(Paths.get(stderrFilePath), Charset.defaultCharset());
            } catch (IOException e) {
                ValidationUtils.fileCantRead(stderrFilePath, e);
                return new ArrayList<>();
            }
        }
    }

}
