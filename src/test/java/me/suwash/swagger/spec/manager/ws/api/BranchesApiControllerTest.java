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

import me.suwash.swagger.spec.manager.SpecMgrTestUtils;
import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.ws.api.ControllerTestUtils.RequestMediaType;
import me.suwash.util.FileUtils;
import me.suwash.util.JsonUtils;

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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestCommandLineRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@lombok.extern.slf4j.Slf4j
public class BranchesApiControllerTest {

    private static final String SPEC_ID = "sample_spec";
    private static final String COMMIT_USER = BranchesApiControllerTest.class.getSimpleName();
    private static final String DIR_DATA = TestConst.DIR_DATA + "/" + COMMIT_USER;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.info(BranchesApiControllerTest.class.getSimpleName());
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
        CommitInfo commitInfo = new CommitInfo(COMMIT_USER, COMMIT_USER + "@example.com", COMMIT_USER + " test tag.");
        RequestMediaType requestMediaType = RequestMediaType.json;
        FileUtils.rmdirs(DIR_DATA);

        // ------------------------------------------------------------------------------------------
        // 準備
        // ------------------------------------------------------------------------------------------
        // payload
        Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

        // リポジトリ初期化
        mockMvc.perform(
            withCommitInfo(post("/specs/" + SPEC_ID), commitInfo)
                .contentType(requestMediaType.value())
                .content(JsonUtils.writeString(payload))
            )
            .andExpect(status().isCreated());

        // 実行結果
        MvcResult result = null;

        // ------------------------------------------------------------------------------------------
        // branches/{branch} : 入力チェック
        // ------------------------------------------------------------------------------------------
        log.info("/branches/{branch} POST 入力チェック");
        mockMvc.perform(
            withCommitInfo(post("/branches/NotExist"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest());

        log.info("/branches/{branch} PUT 入力チェック");
        mockMvc.perform(
            withCommitInfo(put("/branches/NotExist"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest());

        // ------------------------------------------------------------------------------------------
        // branches : 取得 1件(masterブランチのみ)
        // ------------------------------------------------------------------------------------------
        log.info("/branches GET " + requestMediaType);
        result = mockMvc.perform(
            withCommitInfo(get("/branches"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("master"));

        // ------------------------------------------------------------------------------------------
        // branches/{branch} : 取得 0件
        // ------------------------------------------------------------------------------------------
        log.info("/tags/{tag} GET " + requestMediaType);
        result = mockMvc.perform(
            withCommitInfo(get("/branches/develop"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotFound())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString(MessageConst.DATA_NOT_EXIST));

        // ------------------------------------------------------------------------------------------
        // branches/{branch} : 追加
        // ------------------------------------------------------------------------------------------
        log.info("/tags/{tag} POST " + requestMediaType);
        result = mockMvc.perform(
            withCommitInfo(post("/branches/develop?object=master"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("develop"));

        result = mockMvc.perform(
            withCommitInfo(post("/branches/feature/1/someUser?object=develop"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("feature/1/someUser"));

        // 追加済み
        result = mockMvc.perform(
            withCommitInfo(post("/branches/develop?object=master"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString(MessageConst.ERRORHANDLE));

        // ------------------------------------------------------------------------------------------
        // branches/{branch} : 取得
        // ------------------------------------------------------------------------------------------
        log.info("/branches/{branch} GET " + requestMediaType);
        result = mockMvc.perform(
            withCommitInfo(get("/branches/feature/1/someUser"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("feature/1/someUser"));

        // ------------------------------------------------------------------------------------------
        // branches/{branch} : 更新
        // ------------------------------------------------------------------------------------------
        log.info("/branches/{branch} PUT " + requestMediaType);
        result = mockMvc.perform(
            withCommitInfo(put("/branches/feature/1/someUser?to=feature/2/someUser"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("feature/2/someUser"));

        // ------------------------------------------------------------------------------------------
        // branches
        // ------------------------------------------------------------------------------------------
        log.info("/branches GET " + requestMediaType);
        result = mockMvc.perform(
            withCommitInfo(get("/branches"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("feature/2/someUser"));
        assertThat(result.getResponse().getContentAsString(), containsString("develop"));

        // ------------------------------------------------------------------------------------------
        // merges : マージ
        // ------------------------------------------------------------------------------------------
        log.info("/merges POST " + requestMediaType);
        result = mockMvc.perform(
            withCommitInfo(post("/merges?source=feature/2/someUser&target=develop"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("develop"));

        // ------------------------------------------------------------------------------------------
        // switch : スイッチ
        // ------------------------------------------------------------------------------------------
        log.info("/switch POST " + requestMediaType);
        result = mockMvc.perform(
            withCommitInfo(post("/switch/feature/2/someUser"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("feature/2/someUser"));

        // ------------------------------------------------------------------------------------------
        // branches/{branch} : 削除
        // ------------------------------------------------------------------------------------------
        log.info("/branches/{branch} DELETE " + requestMediaType);
        result = mockMvc.perform(
            withCommitInfo(delete("/branches/feature/2/someUser"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), is(StringUtils.EMPTY));

        result = mockMvc.perform(
            withCommitInfo(delete("/branches/develop"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), is(StringUtils.EMPTY));

        // ------------------------------------------------------------------------------------------
        // branches/{branch} : 取得 削除済み
        // ------------------------------------------------------------------------------------------
        log.info("/tags/{tag} GET " + requestMediaType);
        result = mockMvc.perform(
            withCommitInfo(get("/branches/develop"), commitInfo)
                .contentType(requestMediaType.value())
            )
            // .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotFound())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString(MessageConst.DATA_NOT_EXIST));
    }

}
