package me.suwash.swagger.spec.manager.sv.service;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
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
import me.suwash.swagger.spec.manager.sv.domain.Spec;
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
public class BranchServiceTest {

    private static final String BRANCH = "develop";
    private static final String COMMIT_USER = BranchServiceTest.class.getSimpleName();
    private static final String SPEC_ID = "sample_spec";
    private static final String dirData = TestConst.DIR_DATA + "/" + COMMIT_USER;

    @Autowired
    private SpecMgrContext context;
    @Autowired
    private SpecService specService;
    @Autowired
    private BranchService service;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.info(BranchServiceTest.class.getSimpleName());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {
        FileUtils.rmdirs(dirData);
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public final void test() {
        //------------------------------------------------------------------------------------------
        // 準備
        //------------------------------------------------------------------------------------------
        final CommitInfo commitInfo = new CommitInfo(COMMIT_USER, COMMIT_USER + "@example.com", "TagService test tag.");
        final String threadName = Thread.currentThread().getName();
        this.context.put(threadName, CommitInfo.class.getName(), commitInfo);

        // payload
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> depth1_map = new HashMap<>();
        depth1_map.put("depth1.now", LocalDate.now());
        payload.put("depth1", depth1_map);

        // リポジトリ初期化
        Spec spec = specService.newSpec(SPEC_ID, payload);
        spec.add();

        //------------------------------------------------------------------------------------------
        // 入力チェック
        //------------------------------------------------------------------------------------------
        try {
            service.newBranch(null);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            service.findById(StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            service.mergeBranch(StringUtils.EMPTY, StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }
        try {
            service.mergeBranch(StringUtils.EMPTY, "notExist");
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }
        try {
            service.mergeBranch("notExist", StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            service.switchBranch(StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }


        //------------------------------------------------------------------------------------------
        // 0件
        //------------------------------------------------------------------------------------------
        List<String> beforeIdList = service.idList();
        assertThat(beforeIdList, not(hasItem(BRANCH)));

        Branch finded = service.findById(BRANCH);
        assertThat(finded, is(nullValue()));


        //------------------------------------------------------------------------------------------
        // 追加
        //------------------------------------------------------------------------------------------
        log.info("ADD");
        Branch branch = service.newBranch(BRANCH);
        assertThat(branch.getId(), is(BRANCH));
        assertThat(branch.getGitObject(), is(nullValue()));

        branch.add("master");
        List<String> addedIdList = service.idList();
        assertThat(addedIdList, hasItem(BRANCH));
        log.info("-- idList: " + addedIdList);

        Branch added = service.findById(BRANCH);
        assertThat(added, not(nullValue()));
        assertThat(added.getId(), is(BRANCH));

        Branch branch2 = service.newBranch("feature");
        branch2.add(BRANCH);
        List<String> addedIdList2 = service.idList();
        assertThat(addedIdList2, hasItem("feature"));
        log.info("-- idList: " + addedIdList2);

        Branch added2 = service.findById("feature");
        assertThat(added2, not(nullValue()));
        assertThat(added2.getId(), is("feature"));

        //------------------------------------------------------------------------------------------
        // 更新
        //------------------------------------------------------------------------------------------
        log.info("UPDATE");
        added.rename("RENAMED_" + BRANCH);
        Branch renamed = service.findById("RENAMED_" + BRANCH);
        assertThat(renamed.getId(), is("RENAMED_" + BRANCH));

        //------------------------------------------------------------------------------------------
        // switch
        //------------------------------------------------------------------------------------------
        log.info("SWITCH");
        Branch switched = service.switchBranch("feature");
        assertThat(switched.getId(), is("feature"));


        //------------------------------------------------------------------------------------------
        // merge
        //------------------------------------------------------------------------------------------
        // featureにコミット追加
        Map<String, Object> depth1_map2 = new HashMap<>();
        depth1_map2.put("depth1-2.now", LocalDate.now());
        payload.put("depth1-2", depth1_map2);
        spec.update(payload);

        // feature → RENAMED_develop にマージ
        log.info("MERGE");
        Branch merged = service.mergeBranch("feature", "RENAMED_" + BRANCH);
        assertThat(merged.getId(), is("RENAMED_" + BRANCH));


        //------------------------------------------------------------------------------------------
        // 削除
        //------------------------------------------------------------------------------------------
        log.info("DELETE - feature");
        switched.delete();

        List<String> deletedIdList = service.idList();
        assertThat(deletedIdList, not(hasItem("feature")));
        log.info("-- idList: " + deletedIdList);


        log.info("DELETE - RENAMED_" + BRANCH);
        merged.delete();

        deletedIdList = service.idList();
        assertThat(deletedIdList, not(hasItem("RENAMED_" + BRANCH)));
        log.info("-- idList: " + deletedIdList);
    }

}
