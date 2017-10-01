package me.suwash.swagger.spec.manager.ap.facade;

import static me.suwash.swagger.spec.manager.SpecMgrTestUtils.assertCheckErrors;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
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
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.UserDto;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestCommandLineRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@lombok.extern.slf4j.Slf4j
public class UserFacadeTest {

  @Autowired
  private UserFacade facade;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    log.info(UserFacadeTest.class.getSimpleName());
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
    // なし

    // -----------------------------------------------------------------------------------------
    // 検索
    // -----------------------------------------------------------------------------------------
    UserDto dto = facade.findById("");
    assertCheckErrors(dto.getErrors(), new String[] {"BeanValidator.NotEmpty"});
    dto = facade.findById("facade-test_error");
    assertCheckErrors(dto.getErrors(), new String[] {"data.notExist"});

    // -----------------------------------------------------------------------------------------
    // 追加
    // -----------------------------------------------------------------------------------------
    dto = facade.add("", "");
    assertCheckErrors(dto.getErrors(),
        new String[] {"BeanValidator.NotEmpty", "BeanValidator.NotEmpty"});
    dto = facade.add("facade_test_error", "");
    assertCheckErrors(dto.getErrors(), new String[] {"BeanValidator.NotEmpty"});

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    dto = facade.delete("");
    assertCheckErrors(dto.getErrors(), new String[] {"BeanValidator.NotEmpty"});
  }

  @Test
  public final void test() {
    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    // なし

    // -----------------------------------------------------------------------------------------
    // 検索
    // -----------------------------------------------------------------------------------------
    IdListDto idListDto = facade.idList();
    assertThat(idListDto.getList(), not(hasItem("faade_test")));

    // -----------------------------------------------------------------------------------------
    // 追加
    // -----------------------------------------------------------------------------------------
    log.info("ADD");
    UserDto dto = facade.add("facade_test", "facade_test@test.com");
    assertThat(dto.getUser().getId(), is("facade_test"));
    assertThat(dto.getUser().getEmail(), is(nullValue()));

    idListDto = facade.idList();
    assertThat(idListDto.getList(), hasItem("facade_test"));
    log.info("-- idList: " + idListDto.getList());

    dto = facade.findById("facade_test");
    assertThat(dto.getUser().getId(), is("facade_test"));
    assertThat(dto.getUser().getEmail(), is(nullValue()));

    // -----------------------------------------------------------------------------------------
    // 削除
    // -----------------------------------------------------------------------------------------
    log.info("DELETE");
    facade.delete("facade_test");

    idListDto = facade.idList();
    assertThat(idListDto.getList(), not(hasItem("facade_test")));
    log.info("-- idList: " + idListDto.getList());
  }

}
