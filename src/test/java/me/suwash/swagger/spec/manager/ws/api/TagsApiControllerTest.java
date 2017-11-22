package me.suwash.swagger.spec.manager.ws.api;

import static me.suwash.swagger.spec.manager.ws.api.ControllerTestUtils.withCommitInfo;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import me.suwash.swagger.spec.manager.SpecMgrTestUtils;
import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.ws.api.ControllerTestUtils.RequestMediaType;
import me.suwash.util.FileUtils;
import me.suwash.util.JsonUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestCommandLineRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@lombok.extern.slf4j.Slf4j
public class TagsApiControllerTest {

  private static final String SPEC_ID = "sample_spec";
  private static final String COMMIT_USER = TagsApiControllerTest.class.getSimpleName();
  private static final String DIR_DATA = TestConst.DIR_DATA + "/" + COMMIT_USER;

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    log.info(TagsApiControllerTest.class.getSimpleName());
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
    CommitInfo commitInfo =
        new CommitInfo(COMMIT_USER, COMMIT_USER + "@example.com", COMMIT_USER + " test tag.");
    RequestMediaType requestMediaType = RequestMediaType.json;
    if(!FileUtils.rmdirs(DIR_DATA)) fail(DIR_DATA + " を初期化できません。");

    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    // payload
    Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

    // リポジトリ初期化
    mockMvc
        .perform(withCommitInfo(post("/users/" + commitInfo.getUser() + "?email=test@example.com"),
            commitInfo).contentType(requestMediaType.value()))
        .andExpect(status().isCreated());
    mockMvc
        .perform(withCommitInfo(post("/specs/" + SPEC_ID), commitInfo)
            .contentType(requestMediaType.value()).content(JsonUtils.writeString(payload)))
        .andExpect(status().isCreated());

    // 実行結果
    MvcResult result = null;

    // -----------------------------------------------------------------------------------------
    // tags/{tag} : 入力チェック
    // -----------------------------------------------------------------------------------------
    log.info("/tags/{tag} POST 入力チェック");
    mockMvc
        .perform(withCommitInfo(post("/tags/NotExist"), commitInfo)
            .contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest());

    log.info("/tags/{tag} PUT 入力チェック");
    mockMvc
        .perform(
            withCommitInfo(put("/tags/NotExist"), commitInfo).contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest());

    // -----------------------------------------------------------------------------------------
    // tags : 取得 0件
    // -----------------------------------------------------------------------------------------
    log.info("/tags GET " + requestMediaType);
    result = mockMvc
        .perform(withCommitInfo(get("/tags"), commitInfo).contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNoContent()).andReturn();

    // -----------------------------------------------------------------------------------------
    // tags/{tag} : 取得 0件
    // -----------------------------------------------------------------------------------------
    log.info("/tags/{tag} GET " + requestMediaType);
    result = mockMvc
        .perform(
            withCommitInfo(get("/tags/v1.0.0"), commitInfo).contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("data.notExist"));

    // -----------------------------------------------------------------------------------------
    // tags/{tag} : 追加
    // -----------------------------------------------------------------------------------------
    log.info("/tags/{tag} POST " + requestMediaType);
    result = mockMvc
        .perform(withCommitInfo(post("/tags/v1.0.0?object=master"), commitInfo)
            .contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isCreated()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("v1.0.0"));

    // 追加済み
    result = mockMvc
        .perform(withCommitInfo(post("/tags/v1.0.0?object=master"), commitInfo)
            .contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest()).andReturn();
    assertThat(result.getResponse().getContentAsString(),
        containsString("data.alreadyExist"));

    // -----------------------------------------------------------------------------------------
    // tags/{tag} : 取得
    // -----------------------------------------------------------------------------------------
    log.info("/tags/{tag} GET " + requestMediaType);
    result = mockMvc
        .perform(
            withCommitInfo(get("/tags/v1.0.0"), commitInfo).contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("v1.0.0"));

    // -----------------------------------------------------------------------------------------
    // tags/{tag} : 更新
    // -----------------------------------------------------------------------------------------
    log.info("/tags/{tag} PUT " + requestMediaType);
    result = mockMvc
        .perform(withCommitInfo(put("/tags/v1.0.0?to=release/v1.0.0"), commitInfo)
            .contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("release/v1.0.0"));

    // -----------------------------------------------------------------------------------------
    // tags
    // -----------------------------------------------------------------------------------------
    log.info("/tags GET " + requestMediaType);
    result = mockMvc
        .perform(withCommitInfo(get("/tags"), commitInfo).contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("release/v1.0.0"));

    // -----------------------------------------------------------------------------------------
    // tags/{tag} : 削除
    // -----------------------------------------------------------------------------------------
    log.info("/tags/{tag} DELETE " + requestMediaType);
    result = mockMvc
        .perform(withCommitInfo(delete("/tags/release/v1.0.0"), commitInfo)
            .contentType(requestMediaType.value()))
        .andDo(MockMvcResultHandlers.print()).andExpect(status().isOk()).andReturn();
    assertThat(result.getResponse().getContentAsString(), is(StringUtils.EMPTY));

    // -----------------------------------------------------------------------------------------
    // tags/{tag} : 取得 削除済み
    // -----------------------------------------------------------------------------------------
    log.info("/tags/{tag} GET " + requestMediaType);
    result = mockMvc
        .perform(withCommitInfo(get("/tags/release/v1.0.0"), commitInfo)
            .contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("data.notExist"));
  }

}
