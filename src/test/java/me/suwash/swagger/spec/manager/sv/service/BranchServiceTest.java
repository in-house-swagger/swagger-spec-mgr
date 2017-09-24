package me.suwash.swagger.spec.manager.sv.service;

import static me.suwash.swagger.spec.manager.SpecMgrTestUtils.assertCheckErrors;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;

import me.suwash.swagger.spec.manager.SpecMgrTestUtils;
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

    private static final String COMMIT_USER = BranchServiceTest.class.getSimpleName();
    private static final String SPEC_ID = "sample_spec";

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
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public final void testSpec() {
        // ------------------------------------------------------------------------------------------
        // 準備
        // ------------------------------------------------------------------------------------------
        final String user = COMMIT_USER + "_error";
        final CommitInfo commitInfo = new CommitInfo(user, user + "@example.com", "TagService test tag.");
        this.context.putCommitInfo(commitInfo);

        // payload
        Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

        // リポジトリ初期化
        final String dirData = TestConst.DIR_DATA + "/" + user;
        FileUtils.rmdirs(dirData);

        Spec spec = specService.newSpec(SPEC_ID, payload);
        spec.add();

        // -----------------------------------------------------------------------------------------
        // 検索
        // -----------------------------------------------------------------------------------------
        try {
            service.findById(StringUtils.EMPTY);
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }

        // -----------------------------------------------------------------------------------------
        // 追加
        // -----------------------------------------------------------------------------------------
        // id が未設定
        try {
            service.newBranch(null).add("master");
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }
        // gitObject が未設定
        try {
            service.newBranch("error").add(null);
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }
        // id, gitObject が未設定
        try {
            service.newBranch("").add("");
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty", "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }

        // 作成済み
        Branch develop = service.newBranch("develop");
        develop.add("master");
        try {
            service.newBranch("develop").add("master");
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.ERRORHANDLE));
            assertThat(e.getMessageArgs()[0], is("SubProcess"));
        }

        // -----------------------------------------------------------------------------------------
        // スイッチ
        // -----------------------------------------------------------------------------------------
        // id が未設定
        try {
            service.newBranch(null).switchBranch();
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }

        // -----------------------------------------------------------------------------------------
        // マージ
        // -----------------------------------------------------------------------------------------
        // fromId, toBranch が未設定
        try {
            service.newBranch(null).mergeInto(null);
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty", MessageConst.CHECK_NOTNULL
            });
            context.clearErrors();
        }
        // toBranch が未設定
        try {
            develop.mergeInto(null);
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                MessageConst.CHECK_NOTNULL
            });
            context.clearErrors();
        }
        // fromId が未設定
        try {
            service.mergeBranch(null, "master");
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }
        // toId が未設定
        try {
            service.mergeBranch("develop", null);
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }
        // fromId, toId が未設定
        try {
            service.mergeBranch("", "");
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty", "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }

        // -----------------------------------------------------------------------------------------
        // リネーム
        // -----------------------------------------------------------------------------------------
        // fromId, toId が未設定
        try {
            service.newBranch("").rename("");
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty", "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }
        // toId が未設定
        try {
            develop.rename(null);
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                MessageConst.CHECK_NOTNULL
            });
            context.clearErrors();
        }
        // toId が既に存在する
        try {
            develop.rename("master");
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.ERRORHANDLE));
            assertThat(e.getMessageArgs()[0], is("SubProcess"));
        }
        // 削除済み
        develop.delete();
        try {
            develop.rename("feature/1");
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.ERRORHANDLE));
            assertThat(e.getMessageArgs()[0], is("SubProcess"));
        }

        // -----------------------------------------------------------------------------------------
        // 削除
        // -----------------------------------------------------------------------------------------
        // id が未設定
        try {
            service.newBranch(null).delete();
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }
        // 削除済み
        try {
            develop.delete();
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.ERRORHANDLE));
            assertThat(e.getMessageArgs()[0], is("SubProcess"));
        }
    }

    @Test
    public final void test() {
        // ------------------------------------------------------------------------------------------
        // 準備
        // ------------------------------------------------------------------------------------------
        final CommitInfo commitInfo = new CommitInfo(COMMIT_USER, COMMIT_USER + "@example.com", "TagService test tag.");
        this.context.putCommitInfo(commitInfo);

        // payload
        Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

        // リポジトリ初期化
        final String dirData = TestConst.DIR_DATA + "/" + COMMIT_USER;
        FileUtils.rmdirs(dirData);

        Spec spec = specService.newSpec(SPEC_ID, payload);
        spec.add();

        // ------------------------------------------------------------------------------------------
        // 0件
        // ------------------------------------------------------------------------------------------
        List<String> beforeIdList = service.idList();
        assertThat(beforeIdList, not(hasItem("develop")));

        try {
            service.findById("develop");
            fail();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.DATA_NOT_EXIST));
        }

        // ------------------------------------------------------------------------------------------
        // 追加
        // ------------------------------------------------------------------------------------------
        log.info("ADD");
        Branch develop = service.newBranch("develop");
        assertThat(develop.getId(), is("develop"));
        assertThat(develop.getGitObject(), is(nullValue()));

        develop.add("master");
        List<String> addedIdList = service.idList();
        assertThat(addedIdList, hasItem("develop"));
        log.info("-- idList: " + addedIdList);

        Branch addedDevelop = service.findById("develop");
        assertThat(addedDevelop, not(nullValue()));
        assertThat(addedDevelop.getId(), is("develop"));

        Branch feature1 = service.newBranch("feature/1");
        feature1.add("develop");
        List<String> addedIdList2 = service.idList();
        assertThat(addedIdList2, hasItem("feature/1"));
        log.info("-- idList: " + addedIdList2);

        Branch addedFeature1 = service.findById("feature/1");
        assertThat(addedFeature1, not(nullValue()));
        assertThat(addedFeature1.getId(), is("feature/1"));

        // ------------------------------------------------------------------------------------------
        // 更新
        // ------------------------------------------------------------------------------------------
        log.info("UPDATE");
        addedFeature1.rename("feature/2");
        Branch renamedFeature2 = service.findById("feature/2");
        assertThat(renamedFeature2.getId(), is("feature/2"));

        // ------------------------------------------------------------------------------------------
        // switch
        // ------------------------------------------------------------------------------------------
        log.info("SWITCH");
        Branch switchedDevelop = service.switchBranch("develop");
        assertThat(switchedDevelop.getId(), is("develop"));

        // ------------------------------------------------------------------------------------------
        // merge
        // ------------------------------------------------------------------------------------------
        // developにコミット追加
        payload.put("UPDATE_KEY", this.getClass().getSimpleName());
        spec.update(payload);

        // develop → master にマージ
        log.info("MERGE");
        Branch mergedMaster = service.mergeBranch("develop", "master");
        assertThat(mergedMaster.getId(), is("master"));

        // ------------------------------------------------------------------------------------------
        // 削除
        // ------------------------------------------------------------------------------------------
        log.info("DELETE - feature");
        renamedFeature2.delete();

        List<String> deletedIdList = service.idList();
        assertThat(deletedIdList, not(hasItem("feature/2")));
        log.info("-- idList: " + deletedIdList);

        log.info("DELETE - develop");
        switchedDevelop.delete();

        deletedIdList = service.idList();
        assertThat(deletedIdList, not(hasItem("develop")));
        log.info("-- idList: " + deletedIdList);
    }

}
