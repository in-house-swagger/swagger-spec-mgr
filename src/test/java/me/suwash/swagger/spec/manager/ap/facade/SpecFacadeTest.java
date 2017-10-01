package me.suwash.swagger.spec.manager.ap.facade;

import static me.suwash.swagger.spec.manager.SpecMgrTestUtils.assertCheckErrors;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
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
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.SpecDto;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.util.FileUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestCommandLineRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@lombok.extern.slf4j.Slf4j
public class SpecFacadeTest {

  private static final String SPEC_ID = SpecFacadeTest.class.getSimpleName();

  @Autowired
  private UserFacade userFacade;
  @Autowired
  private SpecFacade facade;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    log.info(SpecFacadeTest.class.getSimpleName());
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public final void test_error() {
    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    // データ初期化
    String commitUser = this.getClass().getSimpleName() + "_error";
    final String dirData = TestConst.DIR_DATA + "/" + commitUser;
    FileUtils.rmdirs(dirData);

    final CommitInfo commitInfo = new CommitInfo(commitUser, "spec-mgr@example.com");

    // payload
    Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

    // リポジトリ初期化
    userFacade.add(commitInfo.getUser(), commitInfo.getEmail());

    // -----------------------------------------------------------------------------------------
    // 検索
    // -----------------------------------------------------------------------------------------
    log.info("FIND");
    SpecDto dto = facade.findById(commitInfo, SPEC_ID);
    assertCheckErrors(dto.getErrors(), new String[] {"data.notExist"});

    // -----------------------------------------------------------------------------------------
    // 追加
    // -----------------------------------------------------------------------------------------
    log.info("ADD");
    facade.add(commitInfo, SPEC_ID, payload);
    dto = facade.add(commitInfo, SPEC_ID, payload);
    assertCheckErrors(dto.getErrors(), new String[] {"dir.alreadyExist"});

    // -----------------------------------------------------------------------------------------
    // 更新
    // -----------------------------------------------------------------------------------------
    log.info("UPDATE");
    dto = facade.update(commitInfo, "notExist", payload);
    assertCheckErrors(dto.getErrors(), new String[] {"dir.notExist"});

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    log.info("DELETE");
    dto = facade.delete(commitInfo, "notExist");
    assertCheckErrors(dto.getErrors(), new String[] {"dir.notExist"});
  }

  // @Test
  // public void testNoCommitInfo() {
  // dirMerged = TestConst.DIR_DATA + "/" + TestConst.COMMITUSER_DEFAULT + "/" +
  // TestConst.DIRNAME_MERGED + "/" + SPEC_ID;
  // dirSplit = TestConst.DIR_DATA + "/" + TestConst.COMMITUSER_DEFAULT + "/" +
  // TestConst.DIRNAME_SPLIT + "/" + SPEC_ID;
  // FileUtils.rmdirs(dirMerged);
  // FileUtils.rmdirs(dirSplit);
  // test(null);
  // }

  @Test
  public void testWithCommitInfo() {
    String commitUser = this.getClass().getSimpleName();
    final String dirData = TestConst.DIR_DATA + "/" + commitUser;
    FileUtils.rmdirs(dirData);

    final CommitInfo commitInfo = new CommitInfo(commitUser, "spec-mgr@example.com");
    test(commitInfo);
  }

  @SuppressWarnings("unchecked")
  private final void test(final CommitInfo commitInfo) {
    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    // payload
    Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

    // リポジトリ初期化
    if (commitInfo != null) {
      userFacade.add(commitInfo.getUser(), commitInfo.getEmail());
    }

    // -----------------------------------------------------------------------------------------
    // 検索
    // -----------------------------------------------------------------------------------------
    IdListDto beforeListDto = facade.idList(commitInfo);
    assertThat(beforeListDto.getList(), not(hasItem(SPEC_ID)));

    // -----------------------------------------------------------------------------------------
    // 追加
    // -----------------------------------------------------------------------------------------
    log.info("ADD");
    SpecDto added = facade.add(commitInfo, SPEC_ID, payload);
    assertThat(added.getSpec().getId(), is(SPEC_ID));
    Object addedPayload = added.getSpec().getPayload();
    Map<String, Object> addedPayloadMap = (Map<String, Object>) addedPayload;
    assertThat(addedPayloadMap.keySet(), allOf(hasItem("depth1_map"), hasItem("depth1_list")));

    IdListDto addedIdList = facade.idList(commitInfo);
    assertThat(addedIdList.getList(), hasItem(SPEC_ID));
    log.info("-- idList: " + addedIdList);

    // -----------------------------------------------------------------------------------------
    // 更新
    // -----------------------------------------------------------------------------------------
    log.info("UPDATE");
    payload.put("KEY_FOR_UPDATE", this.getClass().getName());
    facade.update(commitInfo, SPEC_ID, payload);

    IdListDto updatedIdList = facade.idList(commitInfo);
    assertThat(updatedIdList.getList(), hasItem(SPEC_ID));
    log.info("-- idList: " + updatedIdList);

    SpecDto updated = facade.findById(commitInfo, SPEC_ID);
    Object updatePayload = updated.getSpec().getPayload();
    assertThat(updatePayload, not(is(payload)));

    Map<String, Object> updatedPayloadMap = (Map<String, Object>) updatePayload;
    assertThat(updatedPayloadMap.get("KEY_FOR_UPDATE"), is(this.getClass().getName()));

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    log.info("DELETE");
    facade.delete(commitInfo, SPEC_ID);

    IdListDto deletedIdList = facade.idList(commitInfo);
    assertThat(deletedIdList.getList(), not(hasItem(SPEC_ID)));
    log.info("-- idList: " + deletedIdList);
  }

}
