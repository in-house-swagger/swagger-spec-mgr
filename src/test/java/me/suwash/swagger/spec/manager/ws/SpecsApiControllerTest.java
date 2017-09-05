package me.suwash.swagger.spec.manager.ws;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.infra.config.ScmInfo;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtils;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestCommandLineRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@lombok.extern.slf4j.Slf4j
public class SpecsApiControllerTest {

    private static final String SPEC_ID = SpecsApiControllerTest.class.getSimpleName();
    private static String dirMerged;
    private static String dirSplit;

    private static final String URI_BASE = "/specs";

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
    public final void test_yaml_scmなし() throws Exception {
        test(null, RequestMediaType.yaml);
    }

    @Test
    public final void test_yaml_scmあり() throws Exception {
        test(new ScmInfo("user-yaml", "user-yaml@test.com"), RequestMediaType.yaml);
    }

    @Test
    public final void test_json_scmあり() throws Exception {
        test(new ScmInfo("user-json", "user-json@test.com"), RequestMediaType.json);
    }

    public final void test(
        final ScmInfo scmInfo,
        final RequestMediaType requestMediaType) throws Exception {

        dirMerged = TestConst.DIR_DATA + "/" + TestConst.SCMUSER_DEFAULT + "/" + TestConst.DIRNAME_MERGED + "/" + SPEC_ID;
        dirSplit = TestConst.DIR_DATA + "/" + TestConst.SCMUSER_DEFAULT + "/" + TestConst.DIRNAME_SPLIT + "/" + SPEC_ID;
        FileUtils.rmdirs(dirMerged);
        FileUtils.rmdirs(dirSplit);

        // ------------------------------------------------------------------------------------------
        // 準備
        // ------------------------------------------------------------------------------------------
        // payload
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> depth1_map = new HashMap<>();
        depth1_map.put("depth1_map.key1", LocalDate.now());
        depth1_map.put("depth1_map.key2", LocalDateTime.now());
        List<Object> depth1_list = new ArrayList<>();
        depth1_list.add(LocalDateTime.now().toString());
        depth1_list.add(LocalDateTime.now().toString());
        depth1_list.add(LocalDateTime.now().toString());
        payload.put("depth1_map", depth1_map);
        payload.put("depth1_list", depth1_list);

        // 実行結果
        MvcResult result = null;

        // ------------------------------------------------------------------------------------------
        // specs/{specId} : 入力チェック
        // ------------------------------------------------------------------------------------------
        log.info("-- /specs/{specId} POST 入力チェック " + requestMediaType);
        mockMvc.perform(
            addRequestHeader(post(URI_BASE + "/NotExist"), scmInfo))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnsupportedMediaType());
        mockMvc.perform(
            addRequestHeader(post(URI_BASE + "/NotExist"), scmInfo)
                .contentType(requestMediaType.value()))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest());

        log.info("-- /specs/{specId} PUT 入力チェック " + requestMediaType);
        mockMvc.perform(
            addRequestHeader(put(URI_BASE + "/NotExist"), scmInfo))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isUnsupportedMediaType());
        mockMvc.perform(
            addRequestHeader(put(URI_BASE + "/NotExist"), scmInfo)
                .contentType(requestMediaType.value()))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isBadRequest());

        // ------------------------------------------------------------------------------------------
        // specs/{specId} : 取得 0件
        // ------------------------------------------------------------------------------------------
        log.info("-- /specs/{specId} GET " + requestMediaType);
        result = mockMvc.perform(
            addRequestHeader(get(URI_BASE + "/" + SPEC_ID), scmInfo))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotFound())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString(MessageConst.DATA_NOT_EXIST));

        // ------------------------------------------------------------------------------------------
        // specs/{specId} : 追加
        // ------------------------------------------------------------------------------------------
        log.info("-- /specs/{specId} POST " + requestMediaType);
        result = mockMvc.perform(
            addRequestBody(
                post(URI_BASE + "/" + SPEC_ID), requestMediaType, payload, scmInfo))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isCreated())
            .andReturn();
        assertThat(result.getResponse().getContentAsString() + "\n", is(SwaggerSpecUtils.writeString(payload)));

        // ------------------------------------------------------------------------------------------
        // specs/{specId} : 取得
        // ------------------------------------------------------------------------------------------
        log.info("-- /specs/{specId} GET " + requestMediaType);
        result = mockMvc.perform(
            addRequestHeader(get(URI_BASE + "/" + SPEC_ID), scmInfo))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString() + "\n", is(SwaggerSpecUtils.writeString(payload)));

        // ------------------------------------------------------------------------------------------
        // specs/{specId} : 更新
        // ------------------------------------------------------------------------------------------
        log.info("-- /specs/{specId} PUT " + requestMediaType);
        payload.put("KEY_FOR_UPDATE", this.getClass().getName());

        result = mockMvc.perform(
            addRequestBody(
                put(URI_BASE + "/" + SPEC_ID), requestMediaType, payload, scmInfo))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString() + "\n", is(SwaggerSpecUtils.writeString(payload)));

        // ------------------------------------------------------------------------------------------
        // specs
        // ------------------------------------------------------------------------------------------
        log.info("-- /specs GET " + requestMediaType);
        result = mockMvc.perform(
            addRequestHeader(get(URI_BASE), scmInfo))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString(SpecsApiControllerTest.class.getSimpleName()));

        // ------------------------------------------------------------------------------------------
        // specs/{specId} : 削除
        // ------------------------------------------------------------------------------------------
        log.info("-- /specs/{specId} DELETE " + requestMediaType);
        result = mockMvc.perform(
            addRequestHeader(delete(URI_BASE + "/" + SPEC_ID), scmInfo))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), is(StringUtils.EMPTY));

        // ------------------------------------------------------------------------------------------
        // specs/{specId} : 取得 削除済み
        // ------------------------------------------------------------------------------------------
        log.info("-- /specs/{specId} GET " + requestMediaType);
        result = mockMvc.perform(
            addRequestHeader(get(URI_BASE + "/" + SPEC_ID), scmInfo))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isNotFound())
            .andReturn();
        assertThat(result.getResponse().getContentAsString(), containsString(MessageConst.DATA_NOT_EXIST));
    }

    private enum RequestMediaType {
        json("application/json"),
        yaml("application/x-yaml");

        private String value;

        RequestMediaType(String value) {
            this.value = value;
        }

        String value() {
            return this.value;
        }
    }

    private MockHttpServletRequestBuilder addRequestBody(
        final MockHttpServletRequestBuilder builder,
        final RequestMediaType mediaType,
        final Object payload,
        final ScmInfo scmInfo) {

        String requestBody = StringUtils.EMPTY;
        if (RequestMediaType.json.equals(mediaType)) {
            requestBody = JsonUtils.writeString(payload);

        } else if (RequestMediaType.yaml.equals(mediaType)) {
            requestBody = SwaggerSpecUtils.writeString(payload);
        }

        return addRequestHeader(
            builder
                .contentType(mediaType.value())
                .content(requestBody),
            scmInfo);

    }

    private MockHttpServletRequestBuilder addRequestHeader(
        final MockHttpServletRequestBuilder builder,
        final ScmInfo scmInfo) {
        if (scmInfo == null) {
            return builder;

        } else {
            return builder
                .header("x-scm-user", scmInfo.getUser())
                .header("s-scm-email", scmInfo.getEmail());
        }
    }

}
