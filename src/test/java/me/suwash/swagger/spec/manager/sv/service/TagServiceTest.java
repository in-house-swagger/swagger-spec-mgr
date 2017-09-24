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
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.domain.Tag;
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
public class TagServiceTest {

    private static final String COMMIT_USER = TagServiceTest.class.getSimpleName();
    private static final String SPEC_ID = "sample_spec";

    @Autowired
    private SpecMgrContext context;
    @Autowired
    private SpecService specService;
    @Autowired
    private TagService service;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.info(TagServiceTest.class.getSimpleName());
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
        // tagId が未設定
        try {
            Tag tag = service.newTag(null);
            tag.add("master");
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
            Tag tag = service.newTag("error");
            tag.add(null);
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }
        // tagId, gitObject が未設定
        try {
            Tag tag = service.newTag("");
            tag.add("");
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty", "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }

        // 作成済み
        Tag v1 = service.newTag("v1");
        v1.add("master");
        try {
            Tag tag = service.newTag("v1");
            tag.add("master");
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.ERRORHANDLE));
            assertThat(e.getMessageArgs()[0], is("SubProcess"));
        }

        // -----------------------------------------------------------------------------------------
        // リネーム
        // -----------------------------------------------------------------------------------------
        // id が未設定
        try {
            service.newTag("").rename("error");
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }
        // toTag が未設定
        try {
            v1.rename(null);
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                MessageConst.CHECK_NOTNULL
            });
            context.clearErrors();
        }
        // toTag が既に存在する
        try {
            v1.rename("v1");
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.ERRORHANDLE));
            assertThat(e.getMessageArgs()[0], is("SubProcess"));
        }
        // 削除済み
        v1.delete();
        try {
            v1.rename("v2");
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
            service.newTag("").delete();
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
            v1.delete();
            fail();
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.ERRORHANDLE));
            assertThat(e.getMessageArgs()[0], is("SubProcess"));
        }
    }

    @Test
    public final void test() {
        // -----------------------------------------------------------------------------------------
        // 準備
        // -----------------------------------------------------------------------------------------
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
        assertThat(beforeIdList, not(hasItem("v1.0.0")));

        try {
            service.findById("v1.0.0");
            fail();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.DATA_NOT_EXIST));
        }

        // ------------------------------------------------------------------------------------------
        // 追加
        // ------------------------------------------------------------------------------------------
        log.info("ADD");
        Tag tag = service.newTag("v1.0.0");
        assertThat(tag.getId(), is("v1.0.0"));
        assertThat(tag.getGitObject(), is(nullValue()));

        tag.add("master");
        List<String> addedIdList = service.idList();
        assertThat(addedIdList, hasItem("v1.0.0"));
        log.info("-- idList: " + addedIdList);

        Tag addedV100 = service.findById("v1.0.0");
        assertThat(addedV100, not(nullValue()));
        assertThat(addedV100.getId(), is("v1.0.0"));

        Tag v101 = service.newTag("v1.0.1");
        v101.add("master");
        List<String> addedIdListV101 = service.idList();
        assertThat(addedIdListV101, hasItem("v1.0.1"));
        log.info("-- idList: " + addedIdListV101);

        Tag addedV101 = service.findById("v1.0.1");
        assertThat(addedV101, not(nullValue()));
        assertThat(addedV101.getId(), is("v1.0.1"));

        // ------------------------------------------------------------------------------------------
        // リネーム
        // ------------------------------------------------------------------------------------------
        log.info("UPDATE");
        addedV100.rename("ver1.0.0");
        Tag renamedVer100 = service.findById("ver1.0.0");
        assertThat(renamedVer100.getId(), is("ver1.0.0"));

        // ------------------------------------------------------------------------------------------
        // 削除
        // ------------------------------------------------------------------------------------------
        log.info("DELETE");
        renamedVer100.delete();
        List<String> deletedIdList = service.idList();
        assertThat(deletedIdList, not(hasItem("ver1.0.0")));
        log.info("-- idList: " + deletedIdList);
    }

}
