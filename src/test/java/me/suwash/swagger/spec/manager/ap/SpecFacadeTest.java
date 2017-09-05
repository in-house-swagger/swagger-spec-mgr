package me.suwash.swagger.spec.manager.ap;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
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
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.util.FileUtils;
import me.suwash.util.FindUtils;
import me.suwash.util.FindUtils.FileType;

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
public class SpecFacadeTest {

    private static final String SPEC_ID = SpecFacadeTest.class.getSimpleName();
    private static String dirMerged;
    private static String dirSplit;

    @Autowired
    private SpecFacade facade;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.info(SpecFacadeTest.class.getSimpleName());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    @Test
    public void test_scmInfoなし() {
        dirMerged = TestConst.DIR_DATA + "/" + TestConst.SCMUSER_DEFAULT + "/" + TestConst.DIRNAME_MERGED + "/" + SPEC_ID;
        dirSplit = TestConst.DIR_DATA + "/" + TestConst.SCMUSER_DEFAULT + "/" + TestConst.DIRNAME_SPLIT + "/" + SPEC_ID;
        FileUtils.rmdirs(dirMerged);
        FileUtils.rmdirs(dirSplit);
        test(null);
    }

    @Test
    public void test_scmInfoあり() {
        dirMerged = TestConst.DIR_DATA + "/" + "scm-user" + "/" + TestConst.DIRNAME_MERGED + "/" + SPEC_ID;
        dirSplit = TestConst.DIR_DATA + "/" + "scm-user" + "/" + TestConst.DIRNAME_SPLIT + "/" + SPEC_ID;
        FileUtils.rmdirs(dirMerged);
        FileUtils.rmdirs(dirSplit);

        final ScmInfo scmInfo = new ScmInfo("scm-user", "scm-user@example.com");
        test(scmInfo);
    }

    @SuppressWarnings("unchecked")
    public final void test(final ScmInfo scmInfo) {
        //------------------------------------------------------------------------------------------
        // 準備
        //------------------------------------------------------------------------------------------
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


        //------------------------------------------------------------------------------------------
        // 入力チェック
        //------------------------------------------------------------------------------------------
        try {
            facade.findById(StringUtils.EMPTY, scmInfo);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            facade.add(StringUtils.EMPTY, null, scmInfo);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }
        try {
            facade.add(SPEC_ID, null, scmInfo);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            facade.update(StringUtils.EMPTY, null, scmInfo);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }
        try {
            facade.update(SPEC_ID, null, scmInfo);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            facade.delete(StringUtils.EMPTY, scmInfo);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }


        //------------------------------------------------------------------------------------------
        // 0件
        //------------------------------------------------------------------------------------------
        List<String> beforeIdList = facade.idList(scmInfo);
        assertThat(beforeIdList, not(hasItem(SPEC_ID)));

        try {
            facade.findById(SPEC_ID, scmInfo);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.DATA_NOT_EXIST));
        }


        //------------------------------------------------------------------------------------------
        // 追加
        //------------------------------------------------------------------------------------------
        log.info("-- add");
        Spec added = facade.add(SPEC_ID, payload, scmInfo);
        assertThat(added.getId(), is(SPEC_ID));
        Object addedPayload = added.getPayload();
        Map<String, Object> addedPayloadMap = (Map<String, Object>) addedPayload;
        assertThat(addedPayloadMap.keySet(), allOf(hasItem("depth1_map"), hasItem("depth1_list")));

        List<String> addedIdList = facade.idList(scmInfo);
        assertThat(addedIdList, hasItem(SPEC_ID));
        log.info("---- idList: " + addedIdList);

        List<File> addedFileList = FindUtils.find(dirSplit, FileType.File);
        assertThat(addedFileList.size(), is(3));
        log.info("---- fileList: " + addedFileList);


        //------------------------------------------------------------------------------------------
        // 更新
        //------------------------------------------------------------------------------------------
        log.info("-- update");
        payload.put("KEY_FOR_UPDATE", this.getClass().getName());
        facade.update(SPEC_ID, payload, scmInfo);

        List<String> updatedIdList = facade.idList(scmInfo);
        assertThat(updatedIdList, hasItem(SPEC_ID));
        log.info("---- idList: " + updatedIdList);

        List<File> updatedFileList = FindUtils.find(dirSplit, FileType.File);
        assertThat(updatedFileList.size(), is(3));
        log.info("---- fileList: " + updatedFileList);

        Spec updated = facade.findById(SPEC_ID, scmInfo);
        Object updatePayload = updated.getPayload();
        assertThat(updatePayload, not(is(payload)));

        Map<String, Object> updatedPayloadMap = (Map<String, Object>) updatePayload;
        assertThat(updatedPayloadMap.get("KEY_FOR_UPDATE"), is(this.getClass().getName()));


        //------------------------------------------------------------------------------------------
        // 削除
        //------------------------------------------------------------------------------------------
        log.info("-- delete");
        facade.delete(SPEC_ID, scmInfo);

        List<String> deletedIdList = facade.idList(scmInfo);
        assertThat(deletedIdList, not(hasItem(SPEC_ID)));
        log.info("---- idList: " + deletedIdList);

        final File dirMergedObj = new File(dirMerged);
        assertThat(dirMergedObj.exists(), is(false));

        final File dirSplitObj = new File(dirSplit);
        assertThat(dirSplitObj.exists(), is(false));
     }

}
