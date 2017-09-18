package me.suwash.swagger.spec.manager.ap;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Tag;
import me.suwash.swagger.spec.manager.sv.service.TagServiceTest;
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

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = TestCommandLineRunner.class)
@ContextConfiguration(initializers = ConfigFileApplicationContextInitializer.class)
@ActiveProfiles("test")
@lombok.extern.slf4j.Slf4j
public class TagFacadeTest {

    private static final String TAG_ID = "v1.0";
    private static final String SPEC_ID = "sample_spec";
    private static final String COMMIT_USER = TagServiceTest.class.getSimpleName();
    private static final String DIR_DATA = TestConst.DIR_DATA + "/" + COMMIT_USER;

    @Autowired
    private SpecMgrContext context;
    @Autowired
    private SpecFacade specFacade;
    @Autowired
    private TagFacade facade;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.info(TagFacadeTest.class.getSimpleName());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public final void test() {
        //------------------------------------------------------------------------------------------
        // 準備
        //------------------------------------------------------------------------------------------
        FileUtils.rmdirs(DIR_DATA);

        final CommitInfo commitInfo = new CommitInfo(COMMIT_USER, COMMIT_USER + "@example.com");

        // payload
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> depth1_map = new HashMap<>();
        depth1_map.put("depth1.now", LocalDate.now());
        payload.put("depth1", depth1_map);

        // リポジトリ初期化
        specFacade.add(commitInfo, SPEC_ID, payload);


        //------------------------------------------------------------------------------------------
        // 入力チェック
        //------------------------------------------------------------------------------------------
        try {
            facade.findById(commitInfo, StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            facade.add(commitInfo, StringUtils.EMPTY, StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }
        try {
            facade.add(commitInfo, "master", StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            facade.rename(commitInfo, StringUtils.EMPTY, StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }
        try {
            facade.rename(commitInfo, TAG_ID, StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            facade.delete(commitInfo, StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }


        //------------------------------------------------------------------------------------------
        // 0件
        //------------------------------------------------------------------------------------------
        List<String> beforeIdList = facade.idList(commitInfo);
        assertThat(beforeIdList, not(hasItem(TAG_ID)));

        try {
            facade.findById(commitInfo, TAG_ID);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.DATA_NOT_EXIST));
        }


        //------------------------------------------------------------------------------------------
        // 追加
        //------------------------------------------------------------------------------------------
        log.info("ADD");
        Tag added = facade.add(commitInfo, "master", TAG_ID);
        assertThat(added.getId(), is(TAG_ID));

        List<String> addedIdList = facade.idList(commitInfo);
        assertThat(addedIdList, hasItem(TAG_ID));
        log.info("-- idList: " + addedIdList);


        //------------------------------------------------------------------------------------------
        // 更新
        //------------------------------------------------------------------------------------------
        log.info("UPDATE");
        facade.rename(commitInfo, TAG_ID, "RENAMED_" + TAG_ID);

        List<String> updatedIdList = facade.idList(commitInfo);
        assertThat(updatedIdList, hasItem("RENAMED_" + TAG_ID));
        log.info("-- idList: " + updatedIdList);


        //------------------------------------------------------------------------------------------
        // 削除
        //------------------------------------------------------------------------------------------
        log.info("DELETE");
        facade.delete(commitInfo, "RENAMED_" + TAG_ID);

        List<String> deletedIdList = facade.idList(commitInfo);
        assertThat(deletedIdList, not(hasItem("RENAMED_" + TAG_ID)));
        log.info("-- idList: " + deletedIdList);
     }

}
