package me.suwash.swagger.spec.manager.ap;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Branch;
import me.suwash.util.FileUtils;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestCommandLineRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@lombok.extern.slf4j.Slf4j
public class BranchFacadeTest {

    private static final String SPEC_ID = "sample_spec";
    private static final String COMMIT_USER = BranchFacadeTest.class.getSimpleName();
    private static final String DIR_DATA = TestConst.DIR_DATA + "/" + COMMIT_USER;

    @Autowired
    private SpecMgrContext context;
    @Autowired
    private SpecFacade specFacade;
    @Autowired
    private BranchFacade facade;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.info(BranchFacadeTest.class.getSimpleName());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public final void test() {
        //------------------------------------------------------------------------------------------
        // 準備
        //------------------------------------------------------------------------------------------
        FileUtils.rmdirs(DIR_DATA);

        final CommitInfo commitInfo = new CommitInfo(COMMIT_USER, COMMIT_USER + "@example.com");

        // payload
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> depth1_map = new HashMap<>();
        depth1_map.put("depth1.now", LocalDate.now());
        payload.put("depth1", depth1_map);

        // リポジトリ初期化
        specFacade.add(commitInfo, SPEC_ID, payload);


        //------------------------------------------------------------------------------------------
        // 入力チェック
        //------------------------------------------------------------------------------------------
        try {
            facade.findById(commitInfo, StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            facade.add(commitInfo, StringUtils.EMPTY, StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }
        try {
            facade.add(commitInfo, "master", StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            facade.rename(commitInfo, StringUtils.EMPTY, StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }
        try {
            facade.rename(commitInfo, "master", StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            facade.delete(commitInfo, StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            facade.mergeBranch(commitInfo, StringUtils.EMPTY, StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }
        try {
            facade.mergeBranch(commitInfo, "master", StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }


        //------------------------------------------------------------------------------------------
        // 0件
        //------------------------------------------------------------------------------------------
        List<String> beforeIdList = facade.idList(commitInfo);
        assertThat(beforeIdList, not(hasItem("develop")));

        try {
            facade.findById(commitInfo, "develop");
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.DATA_NOT_EXIST));
        }


        //------------------------------------------------------------------------------------------
        // 追加
        //------------------------------------------------------------------------------------------
        log.info("ADD");
        Branch develop = facade.add(commitInfo, "master", "develop");
        assertThat(develop.getId(), is("develop"));

        Branch feature1 = facade.add(commitInfo, "develop", "feature/1");
        assertThat(feature1.getId(), is("feature/1"));

        List<String> addedIdList = facade.idList(commitInfo);
        assertThat(addedIdList, hasItem("develop"));
        assertThat(addedIdList, hasItem("feature/1"));
        log.info("-- idList: " + addedIdList);

        //------------------------------------------------------------------------------------------
        // リネーム
        //------------------------------------------------------------------------------------------
        log.info("RENAME");
        Branch feature2 = facade.rename(commitInfo, "feature/1", "feature/2");
        assertThat(feature2.getId(), is("feature/2"));

        List<String> updatedIdList = facade.idList(commitInfo);
        assertThat(updatedIdList, hasItem("feature/2"));
        log.info("-- idList: " + updatedIdList);

        //------------------------------------------------------------------------------------------
        // マージ
        //------------------------------------------------------------------------------------------
        log.info("MERGE");
        // feature/2 にコミット追加
        payload.put("depth1-2", "value");
        specFacade.update(commitInfo, SPEC_ID, payload);

        // feature/2 → develop にマージ
        develop = facade.mergeBranch(commitInfo, "feature/2", "develop");
        assertThat(develop.getId(), is("develop"));

        //------------------------------------------------------------------------------------------
        // switch
        //------------------------------------------------------------------------------------------
        log.info("SWITCH");
        Branch master = facade.switchBranch(commitInfo, "master");
        assertThat(master.getId(), is("master"));

        //------------------------------------------------------------------------------------------
        // 削除
        //------------------------------------------------------------------------------------------
        log.info("DELETE");
        facade.delete(commitInfo, "feature/2");

        facade.delete(commitInfo, "develop");

        List<String> deletedIdList = facade.idList(commitInfo);
        assertThat(deletedIdList, not(hasItem("feature/2")));
        assertThat(deletedIdList, not(hasItem("develop")));
        log.info("-- idList: " + deletedIdList);
     }

}
