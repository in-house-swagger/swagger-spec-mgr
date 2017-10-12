package me.suwash.swagger.spec.manager.ws.api;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.ws.api.ControllerTestUtils.RequestMediaType;
import me.suwash.util.FileUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestCommandLineRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@lombok.extern.slf4j.Slf4j
public class UsersApiControllerTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    log.info(UsersApiControllerTest.class.getSimpleName());
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {
    // mock登録
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        // .apply(documentationConfiguration(this.restDocumentation))
        // .alwaysDo(document("{method-name}", preprocessResponse(prettyPrint())))
        .build();
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public final void test() throws Exception {
    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    RequestMediaType requestMediaType = RequestMediaType.json;

    // ディレクトリ初期化
    FileUtils.rmdirs(TestConst.DIR_DATA + "/ws_test");

    // 実行結果
    MvcResult result = null;

    // -----------------------------------------------------------------------------------------
    // users/{userId} : 入力チェック
    // -----------------------------------------------------------------------------------------
    log.info("/users/{userId} POST 入力チェック");
    mockMvc.perform(post("/users/NotExist").contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest());

    // // -----------------------------------------------------------------------------------------
    // // users : 取得 0件
    // // -----------------------------------------------------------------------------------------
    // log.info("/users GET " + requestMediaType);
    // result = mockMvc.perform(
    // get("/users")
    // .contentType(requestMediaType.value())
    // )
    // // .andDo(MockMvcResultHandlers.print())
    // .andExpect(status().isNoContent())
    // .andReturn();

    // -----------------------------------------------------------------------------------------
    // users/{userId} : 取得 0件
    // -----------------------------------------------------------------------------------------
    log.info("/users/{userId} GET " + requestMediaType);
    result = mockMvc.perform(get("/users/ws_test").contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("data.notExist"));

    // -----------------------------------------------------------------------------------------
    // users/{userId} : 追加
    // -----------------------------------------------------------------------------------------
    log.info("/users/{userId} POST " + requestMediaType);
    result = mockMvc
        .perform(
            post("/users/ws_test?email=ws_test@test.com").contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isCreated()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("ws_test"));

    // 追加済み
    result = mockMvc
        .perform(
            post("/users/ws_test?email=ws_test@test.com").contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("data.alreadyExist"));

    // -----------------------------------------------------------------------------------------
    // users/{userId} : 取得
    // -----------------------------------------------------------------------------------------
    log.info("/users/{userId} GET " + requestMediaType);
    result = mockMvc.perform(get("/users/ws_test").contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("ws_test"));

    // -----------------------------------------------------------------------------------------
    // users
    // -----------------------------------------------------------------------------------------
    log.info("/users GET " + requestMediaType);
    result = mockMvc.perform(get("/users").contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("ws_test"));

    // -----------------------------------------------------------------------------------------
    // users/{userId} : 削除
    // -----------------------------------------------------------------------------------------
    log.info("/users/{userId} DELETE " + requestMediaType);
    result = mockMvc.perform(delete("/users/ws_test").contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk()).andReturn();
    assertThat(result.getResponse().getContentAsString(), is(StringUtils.EMPTY));

    // -----------------------------------------------------------------------------------------
    // users/{userId} : 取得 削除済み
    // -----------------------------------------------------------------------------------------
    log.info("/users/{userId} GET " + requestMediaType);
    result = mockMvc.perform(get("/users/ws_test").contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("data.notExist"));
  }

}
