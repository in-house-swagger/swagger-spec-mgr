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
import org.apache.commons.lang3.StringUtils;
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
import me.suwash.swagger.spec.manager.SpecMgrTestUtils;
import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Tag;
import me.suwash.util.FileUtils;

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
  private UserService userService;
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
    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    final String userId = COMMIT_USER + "_error";
    final String email = userId + "@example.com";
    final CommitInfo commitInfo = new CommitInfo(userId, email, "TagService test tag.");
    this.context.putCommitInfo(commitInfo);

    // payload
    Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

    // リポジトリ初期化
    final String dirData = TestConst.DIR_DATA + "/" + userId;
    FileUtils.rmdirs(dirData);

    userService.addUser(userId, email);
    specService.addSpec(SPEC_ID, payload);

    // -----------------------------------------------------------------------------------------
    // 検索
    // -----------------------------------------------------------------------------------------
    try {
      service.findById(StringUtils.EMPTY);
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }

    // -----------------------------------------------------------------------------------------
    // 追加
    // -----------------------------------------------------------------------------------------
    // tagId が未設定
    try {
      service.addTag("master", null);
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }
    // gitObject が未設定
    try {
      service.addTag(null, "error");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }
    // tagId, gitObject が未設定
    try {
      service.addTag("", "");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty", "BeanValidator.NotEmpty"});
      context.clearErrors();
    }

    // 作成済み
    service.addTag("master", "v1");
    try {
      service.addTag("master", "v1");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("data.alreadyExist"));
    }

    // -----------------------------------------------------------------------------------------
    // リネーム
    // -----------------------------------------------------------------------------------------
    // id が未設定
    try {
      service.renameTag("", "error");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }
    // toTag が未設定
    try {
      service.renameTag("v1", "");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"check.notNull"});
      context.clearErrors();
    }
    // toTag が既に存在する
    try {
      service.renameTag("v1", "v1");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("data.alreadyExist"));
    }
    // fromTag が存在しない
    try {
      service.renameTag("v2", "v3");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("data.notExist"));
    }

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    // id が未設定
    try {
      service.deleteTag("");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }
    // 削除済み
    service.deleteTag("v1");
    try {
      service.deleteTag("v1");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("data.notExist"));
    }
  }

  @Test
  public final void test() {
    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    final String userId = COMMIT_USER;
    final String email = userId + "@example.com";
    final CommitInfo commitInfo = new CommitInfo(userId, email, "TagService test tag.");
    this.context.putCommitInfo(commitInfo);

    // payload
    Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

    // リポジトリ初期化
    final String dirData = TestConst.DIR_DATA + "/" + userId;
    FileUtils.rmdirs(dirData);

    userService.addUser(userId, email);
    specService.addSpec(SPEC_ID, payload);

    // -----------------------------------------------------------------------------------------
    // 0件
    // -----------------------------------------------------------------------------------------
    List<String> beforeIdList = service.idList();
    assertThat(beforeIdList, not(hasItem("v1.0.0")));

    try {
      service.findById("v1.0.0");
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("data.notExist"));
    }

    // -----------------------------------------------------------------------------------------
    // 追加
    // -----------------------------------------------------------------------------------------
    log.info("ADD");
    Tag tag = service.addTag("master", "v1.0.0");
    assertThat(tag.getId(), is("v1.0.0"));
    assertThat(tag.getGitObject(), is(nullValue()));

    List<String> addedIdList = service.idList();
    assertThat(addedIdList, hasItem("v1.0.0"));
    log.info("-- idList: " + addedIdList);

    Tag addedV100 = service.findById("v1.0.0");
    assertThat(addedV100, not(nullValue()));
    assertThat(addedV100.getId(), is("v1.0.0"));

    service.addTag("master", "v1.0.1");
    List<String> addedIdListV101 = service.idList();
    assertThat(addedIdListV101, hasItem("v1.0.1"));
    log.info("-- idList: " + addedIdListV101);

    Tag addedV101 = service.findById("v1.0.1");
    assertThat(addedV101, not(nullValue()));
    assertThat(addedV101.getId(), is("v1.0.1"));

    // -----------------------------------------------------------------------------------------
    // リネーム
    // -----------------------------------------------------------------------------------------
    log.info("UPDATE");
    Tag renamedVer100 = service.renameTag("v1.0.0", "ver1.0.0");
    assertThat(renamedVer100.getId(), is("ver1.0.0"));

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    log.info("DELETE");
    service.deleteTag("ver1.0.0");
    List<String> deletedIdList = service.idList();
    assertThat(deletedIdList, not(hasItem("ver1.0.0")));
    log.info("-- idList: " + deletedIdList);
  }

}
