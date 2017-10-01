package me.suwash.swagger.spec.manager.sv.service;

import static me.suwash.swagger.spec.manager.SpecMgrTestUtils.assertCheckErrors;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.io.File;
import java.util.List;
import java.util.Map;
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
import me.suwash.swagger.spec.manager.infra.config.ApplicationProperties;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.util.FileUtils;
import me.suwash.util.FindUtils;
import me.suwash.util.FindUtils.FileType;
import me.suwash.util.JsonUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestCommandLineRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@lombok.extern.slf4j.Slf4j
public class SpecServiceTest {

  private static final String SPEC_ID = SpecServiceTest.class.getSimpleName();

  @Autowired
  private ApplicationProperties props;
  @Autowired
  private SpecMgrContext context;
  @Autowired
  private UserService userService;
  @Autowired
  private SpecService service;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    log.info(SpecServiceTest.class.getSimpleName());
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testNoCommitInfo() {
    this.context.putCommitInfo(null);

    final String dirData = TestConst.DIR_DATA + "/" + props.getDefaultCommitUser();
    FileUtils.rmdirs(dirData);

    // payload
    Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

    try {
      service.addSpec("specId", payload);
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"dir.notExist"});
      context.clearErrors();
    }
  }

  @Test
  public void testSpec() {
    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    final String userId = SpecServiceTest.class.getSimpleName() + "_error";
    final String email = userId + "@example.com";
    final CommitInfo commitInfo = new CommitInfo(userId, email, "TagService test tag.");
    this.context.putCommitInfo(commitInfo);

    final String dirData = TestConst.DIR_DATA + "/" + userId;
    FileUtils.rmdirs(dirData);

    // payload
    Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

    // リポジトリ初期化
    userService.addUser(userId, email);

    // -----------------------------------------------------------------------------------------
    // 検索
    // -----------------------------------------------------------------------------------------
    // idが未設定
    try {
      service.findById(null);
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }

    // -----------------------------------------------------------------------------------------
    // 追加
    // -----------------------------------------------------------------------------------------
    // idが未設定
    try {
      service.addSpec(null, payload);
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }

    // payloadが未設定
    try {
      service.addSpec("error", null);
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotNull"});
      context.clearErrors();
    }

    // id, payloadが未設定
    try {
      service.addSpec("", null);
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty", "BeanValidator.NotNull"});
      context.clearErrors();
    }

    // 作成済み
    service.addSpec(SPEC_ID + "_error", payload);
    try {
      service.addSpec(SPEC_ID + "_error", payload);
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"dir.alreadyExist"});
      context.clearErrors();
    }

    // -----------------------------------------------------------------------------------------
    // 更新
    // -----------------------------------------------------------------------------------------
    // payloadが未設定
    try {
      service.updateSpec(SPEC_ID + "_error", null);
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotNull"});
      context.clearErrors();
    }

    // 削除済み
    service.deleteSpec(SPEC_ID + "_error");
    try {
      service.updateSpec(SPEC_ID + "_error", payload);
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"dir.notExist"});
      context.clearErrors();
    }

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    // id が未設定
    try {
      service.deleteSpec(null);
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }

    // 削除済み
    try {
      service.deleteSpec(SPEC_ID + "_error");
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"dir.notExist"});
      context.clearErrors();
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test() {
    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    final String userId = SpecServiceTest.class.getSimpleName();
    final String email = userId + "@example.com";
    final CommitInfo commitInfo = new CommitInfo(userId, email, "TagService test tag.");
    this.context.putCommitInfo(commitInfo);

    final String dirData = TestConst.DIR_DATA + "/" + userId;
    FileUtils.rmdirs(dirData);

    final String dirMerged = dirData + "/" + TestConst.DIRNAME_MERGED + "/" + SPEC_ID;
    final String dirSplit = dirData + "/" + TestConst.DIRNAME_SPLIT + "/" + SPEC_ID;

    // payload
    Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

    // リポジトリ初期化
    userService.addUser(userId, email);

    // -----------------------------------------------------------------------------------------
    // 0件
    // -----------------------------------------------------------------------------------------
    List<String> beforeIdList = service.idList();
    assertThat(beforeIdList, not(hasItem(SPEC_ID)));

    try {
      service.findById(SPEC_ID);
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("data.notExist"));
    }

    // -----------------------------------------------------------------------------------------
    // 追加
    // -----------------------------------------------------------------------------------------
    log.info("ADD");
    Spec spec = service.addSpec(SPEC_ID, payload);
    assertThat(spec.getId(), is(SPEC_ID));
    assertThat(JsonUtils.writeString(spec.getPayload()), is(JsonUtils.writeString(payload)));

    List<String> addedIdList = service.idList();
    assertThat(addedIdList, hasItem(SPEC_ID));
    log.info("-- idList: " + addedIdList);

    List<File> addedFileList = FindUtils.find(dirSplit, FileType.File);
    assertThat(addedFileList.size(), is(3));
    log.info("-- fileList: " + addedFileList);

    // -----------------------------------------------------------------------------------------
    // 更新
    // -----------------------------------------------------------------------------------------
    log.info("UPDATE");
    payload.put("KEY_FOR_UPDATE", this.getClass().getName());
    service.updateSpec(SPEC_ID, payload);

    List<String> updatedIdList = service.idList();
    assertThat(updatedIdList, hasItem(SPEC_ID));
    log.info("-- idList: " + updatedIdList);

    List<File> updatedFileList = FindUtils.find(dirSplit, FileType.File);
    assertThat(updatedFileList.size(), is(3));
    log.info("-- fileList: " + updatedFileList);

    Spec updated = service.findById(SPEC_ID);
    Object updatePayload = updated.getPayload();
    assertThat(updatePayload, not(is(payload)));

    Map<String, Object> updatedPayloadMap = (Map<String, Object>) updatePayload;
    assertThat(updatedPayloadMap.get("KEY_FOR_UPDATE"), is(this.getClass().getName()));

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    log.info("DELETE");
    service.deleteSpec(SPEC_ID);

    List<String> deletedIdList = service.idList();
    assertThat(deletedIdList, not(hasItem(SPEC_ID)));
    log.info("-- idList: " + deletedIdList);

    final File dirMergedObj = new File(dirMerged);
    assertThat(dirMergedObj.exists(), is(false));

    final File dirSplitObj = new File(dirSplit);
    assertThat(dirSplitObj.exists(), is(false));
  }

}
