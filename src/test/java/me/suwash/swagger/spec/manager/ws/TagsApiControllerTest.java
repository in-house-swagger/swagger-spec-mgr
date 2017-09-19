package me.suwash.swagger.spec.manager.ws;

import static me.suwash.swagger.spec.manager.ws.ControllerTestUtils.addMediaType;
import static me.suwash.swagger.spec.manager.ws.ControllerTestUtils.addRequestBody;
import static me.suwash.swagger.spec.manager.ws.ControllerTestUtils.addRequestHeader;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.ws.ControllerTestUtils.RequestMediaType;
import me.suwash.util.FileUtils;

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
public class TagsApiControllerTest {

    private static final String TAG_ID = "v1.0.0";
    private static final String SPEC_ID = "sample_spec";
    private static final String COMMIT_USER = TagsApiControllerTest.class.getSimpleName();
    private static final String DIR_DATA = TestConst.DIR_DATA + "/" + COMMIT_USER;

    private static final String URI_BASE = "/tags";

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
        CommitInfo commitInfo = new CommitInfo(COMMIT_USER, COMMIT_USER + "@example.com", COMMIT_USER + " test tag.");
        RequestMediaType requestMediaType = RequestMediaType.json;
        FileUtils.rmdirs(DIR_DATA);

        // ------------------------------------------------------------------------------------------
        // 準備
        // ------------------------------------------------------------------------------------------
        // payload
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> depth1_map = new HashMap<>();
        depth1_map.put("depth1.now", LocalDate.now());
        payload.put("depth1", depth1_map);

        // リポジトリ初期化
        mockMvc.perform(
            addRequestBody(
                post("/specs/" + SPEC_ID), requestMediaType, payload, commitInfo))
            .andExpect(status().isCreated());

        // 実行結果
        MvcResult result = null;

        // ------------------------------------------------------------------------------------------
        // tags/{tag} : 入力チェック
        // ------------------------------------------------------------------------------------------
        log.info("/tags/{tag} POST 入力チェック");
        mockMvc.perform(
            addRequestHeader(post(URI_BASE + "/NotExist"), commitInfo)
                .contentType(requestMediaType.value()))
//            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest());

        log.info("/tags/{tag} PUT 入力チェック");
        mockMvc.perform(
            addRequestHeader(put(URI_BASE + "/NotExist"), commitInfo)
                .contentType(requestMediaType.value()))
//            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest());


        // ------------------------------------------------------------------------------------------
        // tags : 取得 0件
        // ------------------------------------------------------------------------------------------
        log.info("/tags GET " + requestMediaType);
        result = mockMvc.perform(
            addRequestHeader(get(URI_BASE), commitInfo))
//            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNoContent())
            .andReturn();


        // ------------------------------------------------------------------------------------------
        // tags/{tag} : 取得 0件
        // ------------------------------------------------------------------------------------------
        log.info("/tags/{tag} GET " + requestMediaType);
        result = mockMvc.perform(
            addRequestHeader(get(URI_BASE + "/" + TAG_ID + "/"), commitInfo))
//            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotFound())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString(MessageConst.DATA_NOT_EXIST));


        // ------------------------------------------------------------------------------------------
        // tags/{tag} : 追加
        // ------------------------------------------------------------------------------------------
        log.info("/tags/{tag} POST " + requestMediaType);
        result = mockMvc.perform(
            addMediaType(
                post(URI_BASE + "/" + TAG_ID + "/" + "?object=master"), requestMediaType, commitInfo))
//            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString(TAG_ID));


        // ------------------------------------------------------------------------------------------
        // tags/{tag} : 取得
        // ------------------------------------------------------------------------------------------
        log.info("/tags/{tag} GET " + requestMediaType);
        result = mockMvc.perform(
            addRequestHeader(get(URI_BASE + "/" + TAG_ID + "/"), commitInfo))
//            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString(TAG_ID));

        // ------------------------------------------------------------------------------------------
        // tags/{tag} : 更新
        // ------------------------------------------------------------------------------------------
        log.info("/tags/{tag} PUT " + requestMediaType);
        result = mockMvc.perform(
            addRequestBody(
                put(URI_BASE + "/" + TAG_ID + "/" + "?to=" + "UPDATED_" + TAG_ID), requestMediaType, payload, commitInfo))
//            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("UPDATED_" + TAG_ID));


        // ------------------------------------------------------------------------------------------
        // tags
        // ------------------------------------------------------------------------------------------
        log.info("/tags GET " + requestMediaType);
        result = mockMvc.perform(
            addRequestHeader(get(URI_BASE), commitInfo))
//            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString("UPDATED_" + TAG_ID));


        // ------------------------------------------------------------------------------------------
        // tags/{tag} : 削除
        // ------------------------------------------------------------------------------------------
        log.info("/tags/{tag} DELETE " + requestMediaType);
        result = mockMvc.perform(
            addRequestHeader(delete(URI_BASE + "/" + "UPDATED_" + TAG_ID + "/"), commitInfo))
//            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), is(StringUtils.EMPTY));

        // ------------------------------------------------------------------------------------------
        // tags/{tag} : 取得 削除済み
        // ------------------------------------------------------------------------------------------
        log.info("/tags/{tag} GET " + requestMediaType);
        result = mockMvc.perform(
            addRequestHeader(get(URI_BASE + "/" + "UPDATED_" + TAG_ID + "/"), commitInfo))
//            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotFound())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString(MessageConst.DATA_NOT_EXIST));
    }

}
