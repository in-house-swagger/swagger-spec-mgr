package me.suwash.swagger.spec.manager.sv.service;

import static me.suwash.swagger.spec.manager.SpecMgrTestUtils.assertCheckErrors;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.util.List;
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
import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.User;
import me.suwash.util.FileUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestCommandLineRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@lombok.extern.slf4j.Slf4j
public class UserServiceTest {

  @Autowired
  private SpecMgrContext context;
  @Autowired
  private UserService service;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    log.info(UserServiceTest.class.getSimpleName());
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testSpec() {
    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    // なし

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
      service.addUser(null, "error@test.com");
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }

    // emailが未設定
    try {
      service.addUser("error", null);
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }

    // id, payloadが未設定
    try {
      service.addUser("", "");
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty", "BeanValidator.NotEmpty"});
      context.clearErrors();
    }

    // 作成済み
    service.addUser("error", "error@test.com");
    try {
      service.addUser("error", "error@test.com");
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"dir.alreadyExist"});
      context.clearErrors();
    }

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    // id が未設定
    try {
      service.deleteUser(null);
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"BeanValidator.NotEmpty"});
      context.clearErrors();
    }

    // 削除済み
    service.deleteUser("error");
    try {
      service.deleteUser("error");
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("specificationError"));
      assertCheckErrors(context, new String[] {"dir.notExist"});
      context.clearErrors();
    }
  }

  @Test
  public void test() {
    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    FileUtils.rmdirs(TestConst.DIR_DATA + "/test-user");

    // -----------------------------------------------------------------------------------------
    // 0件
    // -----------------------------------------------------------------------------------------
    List<String> beforeIdList = service.idList();
    assertThat(beforeIdList, not(hasItem("test-user")));

    try {
      service.findById("test-user");
      fail();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("data.notExist"));
    }

    // -----------------------------------------------------------------------------------------
    // 追加
    // -----------------------------------------------------------------------------------------
    log.info("ADD");
    User user = service.addUser("test-user", "test-user@test.com");
    assertThat(user.getId(), is("test-user"));
    assertThat(user.getEmail(), is(nullValue()));

    List<String> addedIdList = service.idList();
    assertThat(addedIdList, hasItem("test-user"));
    log.info("-- idList: " + addedIdList);

    User finded = service.findById("test-user");
    assertThat(finded.getId(), is("test-user"));

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    log.info("DELETE");
    service.deleteUser("test-user");

    List<String> deletedIdList = service.idList();
    assertThat(deletedIdList, not(hasItem("test-user")));
    log.info("-- idList: " + deletedIdList);
  }

}
