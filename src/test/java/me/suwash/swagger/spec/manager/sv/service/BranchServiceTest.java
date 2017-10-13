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
import me.suwash.swagger.spec.manager.SpecMgrTestUtils;
import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Branch;
import me.suwash.util.FileUtils;

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
  private UserService userService;
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

    // リポジトリ初期化
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
    // id が未設定
    try {
      service.addBranch("master", null);
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }
    // gitObject が未設定
    try {
      service.addBranch(null, "error");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }
    // id, gitObject が未設定
    try {
      service.addBranch("", "");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty", "BeanValidator.NotEmpty"});
      context.clearErrors();
    }

    // 作成済み
    service.addBranch("master", "develop");
    try {
      service.addBranch("master", "develop");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("data.alreadyExist"));
    }

    // -----------------------------------------------------------------------------------------
    // スイッチ
    // -----------------------------------------------------------------------------------------
    // id が未設定
    try {
      service.switchBranch(null);
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }

    // -----------------------------------------------------------------------------------------
    // マージ
    // -----------------------------------------------------------------------------------------
    // fromId, toBranch が未設定
    try {
      service.mergeBranch(null, null);
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty", "BeanValidator.NotEmpty"});
      context.clearErrors();
    }
    // toBranch が未設定
    try {
      service.mergeBranch("develop", null);
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"check.notNull"});
      context.clearErrors();
    }
    // fromId が未設定
    try {
      service.mergeBranch(null, "master");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }
    // fromId, toId が未設定
    try {
      service.mergeBranch("", "");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty", "BeanValidator.NotEmpty"});
      context.clearErrors();
    }

    // -----------------------------------------------------------------------------------------
    // リネーム
    // -----------------------------------------------------------------------------------------
    // fromId, toId が未設定
    try {
      service.renameBranch("", "");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty", "BeanValidator.NotEmpty"});
      context.clearErrors();
    }
    // toId が未設定
    try {
      service.renameBranch("develop", null);
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"check.notNull"});
      context.clearErrors();
    }
    // toId が既に存在する
    try {
      service.renameBranch("develop", "master");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("data.alreadyExist"));
    }
    // 削除済み
    service.deleteBranch("develop");
    try {
      service.renameBranch("develop", "feature/1");
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("data.notExist"));
    }

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    // id が未設定
    try {
      service.deleteBranch(null);
      fail();
    } catch (final SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }
    // 削除済み
    try {
      service.deleteBranch("develop");
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
    assertThat(beforeIdList, not(hasItem("develop")));

    try {
      service.findById("develop");
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("data.notExist"));
    }

    // -----------------------------------------------------------------------------------------
    // 追加
    // -----------------------------------------------------------------------------------------
    log.info("ADD");
    Branch develop = service.addBranch("master", "develop");
    assertThat(develop.getId(), is("develop"));
    assertThat(develop.getGitObject(), is(nullValue()));

    List<String> addedIdList = service.idList();
    assertThat(addedIdList, hasItem("develop"));
    log.info("-- idList: " + addedIdList);

    Branch addedDevelop = service.findById("develop");
    assertThat(addedDevelop, not(nullValue()));
    assertThat(addedDevelop.getId(), is("develop"));

    service.addBranch("develop", "feature/1");
    List<String> addedIdList2 = service.idList();
    assertThat(addedIdList2, hasItem("feature/1"));
    log.info("-- idList: " + addedIdList2);

    Branch addedFeature1 = service.findById("feature/1");
    assertThat(addedFeature1, not(nullValue()));
    assertThat(addedFeature1.getId(), is("feature/1"));

    // feature/1 に sample_spec_2 を追加
    specService.addSpec(SPEC_ID + "_2", payload);
    List<String> featureSpecList = specService.idList();
    assertThat(featureSpecList, hasItem(SPEC_ID + "_2"));

    // -----------------------------------------------------------------------------------------
    // 更新
    // -----------------------------------------------------------------------------------------
    log.info("UPDATE");
    service.renameBranch("feature/1", "feature/2");
    Branch renamedFeature2 = service.findById("feature/2");
    assertThat(renamedFeature2.getId(), is("feature/2"));

    // -----------------------------------------------------------------------------------------
    // switch
    // -----------------------------------------------------------------------------------------
    log.info("SWITCH");
    Branch switchedDevelop = service.switchBranch("develop");
    assertThat(switchedDevelop.getId(), is("develop"));

    // develop に sample_spec_2 が存在しないこと
    List<String> developSpecList = specService.idList();
    assertThat(developSpecList, not(hasItem(SPEC_ID + "_2")));

    // -----------------------------------------------------------------------------------------
    // merge
    // -----------------------------------------------------------------------------------------
    // developにコミット追加
    payload.put("UPDATE_KEY", this.getClass().getSimpleName());
    specService.updateSpec(SPEC_ID, payload);

    // develop → master にマージ
    log.info("MERGE");
    Branch mergedMaster = service.mergeBranch("develop", "master");
    assertThat(mergedMaster.getId(), is("master"));

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    log.info("DELETE - feature");
    service.deleteBranch("feature/2");

    List<String> deletedIdList = service.idList();
    assertThat(deletedIdList, not(hasItem("feature/2")));
    log.info("-- idList: " + deletedIdList);

    log.info("DELETE - develop");
    service.deleteBranch("develop");

    deletedIdList = service.idList();
    assertThat(deletedIdList, not(hasItem("develop")));
    log.info("-- idList: " + deletedIdList);
  }

}
