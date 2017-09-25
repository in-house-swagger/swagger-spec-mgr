package me.suwash.swagger.spec.manager.infra.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;

import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.util.SubProcess.ProcessResult;
import me.suwash.util.FileUtils;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@lombok.extern.slf4j.Slf4j
public class SubProcessTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    private static final String DIR_BASE = "src/main/scripts";
    private static final String DIR_BIN = new File(DIR_BASE + "/bin").getAbsolutePath();
    private static final String DIR_DATA = new File(DIR_BASE + "/data").getAbsolutePath();

    @Test
    public final void test() {
        FileUtils.rmdirs(DIR_DATA);

        ProcessResult result;

        // nullチェック
        try {
            SubProcess.newExecuter().execute();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        // workDir
        result = SubProcess.newExecuter()
            .workDir("/")
            .command("pwd")
            .execute();
        printResult(result);
        assertThat(result.getExitCode(), is(SubProcess.EXITCODE_SUCCESS));
        assertThat(result.getStdout(), hasItem("/"));

        // stdout/stderr
        result = SubProcess.newExecuter()
            .stdout("/tmp/test_stdout.log")
            .stderr("/tmp/test_stderr.log")
            .command("echo 'out'; echo 'err' >&2;")
            .execute();
        printResult(result);
        assertThat(result.getExitCode(), is(SubProcess.EXITCODE_SUCCESS));
        assertThat(result.getStdoutFilePath(), is("/tmp/test_stdout.log"));
        assertThat(result.getStderrFilePath(), is("/tmp/test_stderr.log"));

        result = SubProcess.newExecuter()
            .stdout("/tmp/test_stdout_err.log")
            .stderr("/tmp/test_stdout_err.log")
            .command("echo 'out'; echo 'err' >&2;")
            .execute();
        printResult(result);
        assertThat(result.getExitCode(), is(SubProcess.EXITCODE_SUCCESS));
        assertThat(result.getStdoutFilePath(), is("/tmp/test_stdout_err.log"));
        assertThat(result.getStderrFilePath(), is("/tmp/test_stdout_err.log"));

        // env
        result = SubProcess.newExecuter()
            .env("SOME_VAR", "SOME_VAR_VALUE1")
            .env("SOME_VAR", "SOME_VAR_VALUE2")
            .command("echo ${SOME_VAR}")
            .execute();
        printResult(result);
        assertThat(result.getExitCode(), is(SubProcess.EXITCODE_SUCCESS));
        assertThat(result.getStdout(), hasItem("SOME_VAR_VALUE2"));

        // timeout
        result = SubProcess.newExecuter()
            .timeout(1)
            .command("echo START; sleep 3; echo END;")
            .execute();
        printResult(result);
        assertThat(result.getExitCode(), is(SubProcess.EXITCODE_TIMEOUT));
        assertThat(result.getStdout(), not(hasItem("END")));

        // exitcode
        result = SubProcess.newExecuter()
            .command("whoami; pwd; echo ${PATH};  exit 10;")
            .execute();
        printResult(result);
        assertThat(result.getExitCode(), is(10));
        assertThat(result.getStdout(), not(hasItem("END")));

        // git script
        result = SubProcess.newExecuter()
            .command(DIR_BIN + "/git/clone.sh")
            .execute();
        printResult(result);
        assertThat(result.getExitCode(), is(SubProcess.EXITCODE_SUCCESS));
    }

    private void printResult(final ProcessResult result) {
        log.debug("-------------------- SubProcess Result START --------------------");
        log.debug("exit_code: " + result.getExitCode());
        log.debug("----- " + result.getStdoutFilePath() + " -----");
        result.getStdout().forEach((final String line) -> {
            log.debug(line);
        });
        log.debug("----- " + result.getStderrFilePath() + " -----");
        result.getStderr().forEach((final String line) -> {
            log.debug(line);
        });
        log.debug("-------------------- SubProcess Result END   --------------------");
    }

}
