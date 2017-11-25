package me.suwash.swagger.spec.manager.ws.api;

import static me.suwash.swagger.spec.manager.ws.api.ControllerTestUtils.withCommitInfo;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import me.suwash.swagger.spec.manager.SpecMgrTestUtils;
import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtils;
import me.suwash.swagger.spec.manager.ws.api.ControllerTestUtils.RequestMediaType;
import me.suwash.util.FileUtils;
import me.suwash.util.JsonUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestCommandLineRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@lombok.extern.slf4j.Slf4j
public class SpecsApiControllerTest {

  private static final String SPEC_ID = SpecsApiControllerTest.class.getSimpleName();

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    log.info(SpecsApiControllerTest.class.getSimpleName());
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
  public final void testYamlNoCommitInfo() throws Exception {
    test(null, RequestMediaType.yaml);
  }

  @Test
  public final void testYamlWithCommitInfo() throws Exception {
    test(new CommitInfo("user-yaml", "user-yaml@test.com", "yaml:コミットメッセージ"),
        RequestMediaType.yaml);
  }

  @Test
  public final void testJsonWithCommitInfo() throws Exception {
    test(new CommitInfo("user-json", "user-json@test.com", "json:コミットメッセージ"),
        RequestMediaType.json);
  }

  private final void test(final CommitInfo commitInfo, final RequestMediaType requestMediaType)
      throws Exception {

    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    // payload
    Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();
    String requestBody = convertBody(requestMediaType, payload);

    // 実行結果
    MvcResult result = null;

    // リポジトリ初期化
    if (commitInfo != null) {
      FileUtils.rmdirs(TestConst.DIR_DATA + "/" + commitInfo.getUser());
      mockMvc
          .perform(withCommitInfo(
              post("/users/" + commitInfo.getUser() + "?email=" + commitInfo.getEmail()),
              commitInfo).contentType(RequestMediaType.json.value()))
          // .andDo(MockMvcResultHandlers.print())
          .andExpect(status().isCreated());

    } else {
      FileUtils.rmdirs(TestConst.DIR_DATA + "/" + TestConst.COMMITUSER_DEFAULT);
      mockMvc.perform(post("/users").contentType(RequestMediaType.json.value()))
          .andExpect(status().isCreated());
    }

    // -----------------------------------------------------------------------------------------
    // specs/{specId} : 入力チェック
    // -----------------------------------------------------------------------------------------
    log.info("/specs/{specId} POST 入力チェック " + requestMediaType);
    // mediaTypeなし
    mockMvc.perform(withCommitInfo(post("/specs/NotExist"), commitInfo))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isUnsupportedMediaType());
    // bodyなし
    mockMvc
        .perform(withCommitInfo(post("/specs/NotExist"), commitInfo)
            .contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest());

    log.info("/specs/{specId} PUT 入力チェック " + requestMediaType);
    // mediaTypeなし
    mockMvc.perform(withCommitInfo(put("/specs/NotExist"), commitInfo))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isUnsupportedMediaType());
    // bodyなし
    mockMvc
        .perform(withCommitInfo(put("/specs/NotExist"), commitInfo)
            .contentType(requestMediaType.value()))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest());

    // -----------------------------------------------------------------------------------------
    // specs : 取得 0件
    // -----------------------------------------------------------------------------------------
    if (commitInfo != null) {
      log.info("/specs GET " + requestMediaType);
      result = mockMvc.perform(withCommitInfo(get("/specs"), commitInfo))
          // .andDo(MockMvcResultHandlers.print())
          .andExpect(status().isNoContent()).andReturn();
    }

    // -----------------------------------------------------------------------------------------
    // specs/{specId} : 取得 0件
    // -----------------------------------------------------------------------------------------
    log.info("/specs/{specId} GET " + requestMediaType);
    result = mockMvc.perform(withCommitInfo(get("/specs/" + SPEC_ID), commitInfo))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("data.notExist"));

    // -----------------------------------------------------------------------------------------
    // specs/{specId} : 追加
    // -----------------------------------------------------------------------------------------
    log.info("/specs/{specId} POST " + requestMediaType);
    result = mockMvc
        .perform(withCommitInfo(post("/specs/" + SPEC_ID), commitInfo)
            .contentType(requestMediaType.value()).content(requestBody))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isCreated()).andReturn();
    assertThat(result.getResponse().getContentAsString() + "\n",
        is(SwaggerSpecUtils.writeString(payload)));

    // 追加済み
    result = mockMvc
        .perform(withCommitInfo(post("/specs/" + SPEC_ID), commitInfo)
            .contentType(requestMediaType.value()).content(requestBody))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isBadRequest()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("data.alreadyExist"));

    // -----------------------------------------------------------------------------------------
    // specs/{specId} : 取得
    // -----------------------------------------------------------------------------------------
    log.info("/specs/{specId} GET " + requestMediaType);
    result = mockMvc.perform(withCommitInfo(get("/specs/" + SPEC_ID), commitInfo))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk()).andReturn();
    assertThat(result.getResponse().getContentAsString() + "\n",
        is(SwaggerSpecUtils.writeString(payload)));

    // -----------------------------------------------------------------------------------------
    // specs/{specId} : 更新
    // -----------------------------------------------------------------------------------------
    log.info("/specs/{specId} PUT " + requestMediaType);
    payload.put("KEY_FOR_UPDATE", this.getClass().getName());
    requestBody = convertBody(requestMediaType, payload);

    result = mockMvc
        .perform(withCommitInfo(put("/specs/" + SPEC_ID), commitInfo)
            .contentType(requestMediaType.value()).content(requestBody))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk()).andReturn();
    // System.out.println("---- response");
    // System.out.println(result.getResponse().getContentAsString() + "\n");
    // System.out.println("---- parsed");
    // System.out.println(SwaggerSpecUtils.writeString(payload));
    assertThat(result.getResponse().getContentAsString() + "\n",
        is(SwaggerSpecUtils.writeString(payload)));

    // -----------------------------------------------------------------------------------------
    // specs
    // -----------------------------------------------------------------------------------------
    log.info("/specs GET " + requestMediaType);
    result = mockMvc.perform(withCommitInfo(get("/specs"), commitInfo))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk()).andReturn();
    assertThat(result.getResponse().getContentAsString(),
        containsString(SpecsApiControllerTest.class.getSimpleName()));

    // -----------------------------------------------------------------------------------------
    // specs/{specId} : 削除
    // -----------------------------------------------------------------------------------------
    log.info("/specs/{specId} DELETE " + requestMediaType);
    result = mockMvc.perform(withCommitInfo(delete("/specs/" + SPEC_ID), commitInfo))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isOk()).andReturn();
    assertThat(result.getResponse().getContentAsString(), is(StringUtils.EMPTY));

    // -----------------------------------------------------------------------------------------
    // specs/{specId} : 取得 削除済み
    // -----------------------------------------------------------------------------------------
    log.info("/specs/{specId} GET " + requestMediaType);
    result = mockMvc.perform(withCommitInfo(get("/specs/" + SPEC_ID), commitInfo))
        // .andDo(MockMvcResultHandlers.print())
        .andExpect(status().isNotFound()).andReturn();
    assertThat(result.getResponse().getContentAsString(), containsString("data.notExist"));
  }

  /**
   * mediaTypeに合わせて、payloadを文字列に変換します。
   *
   * @param mediaType RequestするmediaType
   * @param payload Requestするpayload
   * @return payloadの文字列表現
   */
  private String convertBody(final RequestMediaType mediaType, Map<String, Object> payload) {
    String requestBody = StringUtils.EMPTY;
    if (RequestMediaType.json.equals(mediaType)) {
      requestBody = JsonUtils.writeString(payload);

    } else if (RequestMediaType.yaml.equals(mediaType)) {
      requestBody = SwaggerSpecUtils.writeString(payload);
    }
    return requestBody;
  }

}
