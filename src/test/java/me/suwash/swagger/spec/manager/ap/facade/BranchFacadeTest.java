package me.suwash.swagger.spec.manager.ap.facade;

import static me.suwash.swagger.spec.manager.SpecMgrTestUtils.assertCheckErrors;
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
import me.suwash.swagger.spec.manager.ap.dto.BranchDto;
import me.suwash.swagger.spec.manager.ap.dto.BranchListDto;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.util.FileUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestCommandLineRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@lombok.extern.slf4j.Slf4j
public class BranchFacadeTest {

  private static final String SPEC_ID = "sample_spec";
  private static final String COMMIT_USER = BranchFacadeTest.class.getSimpleName();

  @Autowired
  private UserFacade userFacade;
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
  public final void test_error() {
    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    final String commitUser = COMMIT_USER + "_error";
    final String dirData = TestConst.DIR_DATA + "/" + commitUser;
    FileUtils.rmdirs(dirData);

    final CommitInfo commitInfo = new CommitInfo(commitUser, commitUser + "@example.com");

    // payload
    Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

    // リポジトリ初期化
    userFacade.add(commitInfo.getUser(), commitInfo.getEmail());
    specFacade.add(commitInfo, SPEC_ID, payload);

    // -----------------------------------------------------------------------------------------
    // 検索
    // -----------------------------------------------------------------------------------------
    BranchDto dto = facade.findById(commitInfo, "");
    assertCheckErrors(dto.getErrors(), new String[] {"BeanValidator.NotEmpty"});

    // -----------------------------------------------------------------------------------------
    // 追加
    // -----------------------------------------------------------------------------------------
    dto = facade.add(commitInfo, "", "");
    assertCheckErrors(dto.getErrors(),
        new String[] {"BeanValidator.NotEmpty", "BeanValidator.NotEmpty"});
    dto = facade.add(commitInfo, "master", "");
    assertCheckErrors(dto.getErrors(), new String[] {"BeanValidator.NotEmpty"});

    // -----------------------------------------------------------------------------------------
    // リネーム
    // -----------------------------------------------------------------------------------------
    dto = facade.rename(commitInfo, "", "");
    assertCheckErrors(dto.getErrors(),
        new String[] {"BeanValidator.NotEmpty", "BeanValidator.NotEmpty"});

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    dto = facade.delete(commitInfo, "");
    assertCheckErrors(dto.getErrors(), new String[] {"BeanValidator.NotEmpty"});

    // -----------------------------------------------------------------------------------------
    // マージ
    // -----------------------------------------------------------------------------------------
    dto = facade.mergeBranch(commitInfo, "", "");
    assertCheckErrors(dto.getErrors(),
        new String[] {"BeanValidator.NotEmpty", "BeanValidator.NotEmpty"});

    // -----------------------------------------------------------------------------------------
    // スイッチ
    // -----------------------------------------------------------------------------------------
    dto = facade.switchBranch(commitInfo, "");
    assertCheckErrors(dto.getErrors(), new String[] {"BeanValidator.NotEmpty"});
  }

  @Test
  public final void test() {
    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    final String dirData = TestConst.DIR_DATA + "/" + COMMIT_USER;
    FileUtils.rmdirs(dirData);

    final CommitInfo commitInfo = new CommitInfo(COMMIT_USER, COMMIT_USER + "@example.com");

    // payload
    Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

    // リポジトリ初期化
    userFacade.add(commitInfo.getUser(), commitInfo.getEmail());
    specFacade.add(commitInfo, SPEC_ID, payload);

    // -----------------------------------------------------------------------------------------
    // 検索
    // -----------------------------------------------------------------------------------------
    BranchListDto branchListDto = facade.branchList(commitInfo);
    assertThat(branchListDto.getList(), not(hasItem("develop")));
    assertThat(branchListDto.getCurrent(), is("master"));

    // -----------------------------------------------------------------------------------------
    // 追加
    // -----------------------------------------------------------------------------------------
    log.info("ADD");
    BranchDto dto = facade.add(commitInfo, "master", "develop");
    assertThat(dto.getBranch().getId(), is("develop"));

    dto = facade.add(commitInfo, "develop", "feature/1");
    assertThat(dto.getBranch().getId(), is("feature/1"));

    branchListDto = facade.branchList(commitInfo);
    assertThat(branchListDto.getList(), hasItem("develop"));
    assertThat(branchListDto.getList(), hasItem("feature/1"));
    assertThat(branchListDto.getCurrent(), is("feature/1"));
    log.info("-- idList: " + branchListDto.getList());

    dto = facade.findById(commitInfo, "develop");
    assertThat(dto.getBranch().getId(), is("develop"));

    // -----------------------------------------------------------------------------------------
    // リネーム
    // -----------------------------------------------------------------------------------------
    log.info("RENAME");
    dto = facade.rename(commitInfo, "feature/1", "feature/2");
    assertThat(dto.getBranch().getId(), is("feature/2"));

    branchListDto = facade.branchList(commitInfo);
    assertThat(branchListDto.getList(), hasItem("feature/2"));
    assertThat(branchListDto.getCurrent(), is("feature/2"));
    log.info("-- idList: " + branchListDto.getList());

    // -----------------------------------------------------------------------------------------
    // マージ
    // -----------------------------------------------------------------------------------------
    log.info("MERGE");
    // feature/2 にコミット追加
    payload.put("UPDATE_KEY", "value");
    specFacade.update(commitInfo, SPEC_ID, payload);

    // feature/2 → develop にマージ
    dto = facade.mergeBranch(commitInfo, "feature/2", "develop");
    assertThat(dto.getBranch().getId(), is("develop"));

    // -----------------------------------------------------------------------------------------
    // switch
    // -----------------------------------------------------------------------------------------
    log.info("SWITCH");
    dto = facade.switchBranch(commitInfo, "master");
    assertThat(dto.getBranch().getId(), is("master"));
    branchListDto = facade.branchList(commitInfo);
    assertThat(branchListDto.getCurrent(), is("master"));

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    log.info("DELETE");
    facade.delete(commitInfo, "feature/2");

    facade.delete(commitInfo, "develop");

    branchListDto = facade.branchList(commitInfo);
    assertThat(branchListDto.getList(), not(hasItem("feature/2")));
    assertThat(branchListDto.getList(), not(hasItem("develop")));
    log.info("-- idList: " + branchListDto.getList());
  }

}
