package me.suwash.swagger.spec.manager.ap.facade;

import static me.suwash.swagger.spec.manager.SpecMgrTestUtils.assertCheckErrors;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;
import java.util.Map;

import me.suwash.swagger.spec.manager.SpecMgrTestUtils;
import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.SpecDto;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.util.FileUtils;
import me.suwash.util.FindUtils;
import me.suwash.util.FindUtils.FileType;

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
    public final void test_error() {
        // ------------------------------------------------------------------------------------------
        // 準備
        // ------------------------------------------------------------------------------------------
        // データ初期化
        String commitUser = this.getClass().getSimpleName() + "_error";
        dirMerged = TestConst.DIR_DATA + "/" + commitUser + "/" + TestConst.DIRNAME_MERGED + "/" + SPEC_ID;
        dirSplit = TestConst.DIR_DATA + "/" + commitUser + "/" + TestConst.DIRNAME_SPLIT + "/" + SPEC_ID;
        FileUtils.rmdirs(dirMerged);
        FileUtils.rmdirs(dirSplit);

        final CommitInfo commitInfo = new CommitInfo(commitUser, "spec-mgr@example.com");

        // payload
        Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

        // ------------------------------------------------------------------------------------------
        // 検索
        // ------------------------------------------------------------------------------------------
        log.info("FIND");
        SpecDto dto = facade.findById(commitInfo, SPEC_ID);
        assertCheckErrors(dto.getErrors(), new String[] {
            MessageConst.DATA_NOT_EXIST
        });

        // ------------------------------------------------------------------------------------------
        // 追加
        // ------------------------------------------------------------------------------------------
        log.info("ADD");
        facade.add(commitInfo, SPEC_ID, payload);
        dto = facade.add(commitInfo, SPEC_ID, payload);
        assertCheckErrors(dto.getErrors(), new String[] {
            MessageConst.DATA_ALREADY_EXIST
        });

        // ------------------------------------------------------------------------------------------
        // 更新
        // ------------------------------------------------------------------------------------------
        log.info("UPDATE");
        dto = facade.update(commitInfo, "notExist", payload);
        assertCheckErrors(dto.getErrors(), new String[] {
            MessageConst.DATA_NOT_EXIST
        });

        // ------------------------------------------------------------------------------------------
        // 削除
        // ------------------------------------------------------------------------------------------
        log.info("DELETE");
        dto = facade.delete(commitInfo, "notExist");
        assertCheckErrors(dto.getErrors(), new String[] {
            MessageConst.DATA_NOT_EXIST
        });
    }

//    @Test
//    public void test_commitInfoなし() {
//        dirMerged = TestConst.DIR_DATA + "/" + TestConst.COMMITUSER_DEFAULT + "/" + TestConst.DIRNAME_MERGED + "/" + SPEC_ID;
//        dirSplit = TestConst.DIR_DATA + "/" + TestConst.COMMITUSER_DEFAULT + "/" + TestConst.DIRNAME_SPLIT + "/" + SPEC_ID;
//        FileUtils.rmdirs(dirMerged);
//        FileUtils.rmdirs(dirSplit);
//        test(null);
//    }

    @Test
    public void test_commitInfoあり() {
        String commitUser = this.getClass().getSimpleName();
        dirMerged = TestConst.DIR_DATA + "/" + commitUser + "/" + TestConst.DIRNAME_MERGED + "/" + SPEC_ID;
        dirSplit = TestConst.DIR_DATA + "/" + commitUser + "/" + TestConst.DIRNAME_SPLIT + "/" + SPEC_ID;
        FileUtils.rmdirs(dirMerged);
        FileUtils.rmdirs(dirSplit);

        final CommitInfo commitInfo = new CommitInfo(commitUser, "spec-mgr@example.com");
        test(commitInfo);
    }

    @SuppressWarnings("unchecked")
    private final void test(final CommitInfo commitInfo) {
        // ------------------------------------------------------------------------------------------
        // 準備
        // ------------------------------------------------------------------------------------------
        // payload
        Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

        // ------------------------------------------------------------------------------------------
        // 検索
        // ------------------------------------------------------------------------------------------
        IdListDto beforeListDto = facade.idList(commitInfo);
        assertThat(beforeListDto.getList(), not(hasItem(SPEC_ID)));

        // ------------------------------------------------------------------------------------------
        // 追加
        // ------------------------------------------------------------------------------------------
        log.info("ADD");
        SpecDto added = facade.add(commitInfo, SPEC_ID, payload);
        assertThat(added.getSpec().getId(), is(SPEC_ID));
        Object addedPayload = added.getSpec().getPayload();
        Map<String, Object> addedPayloadMap = (Map<String, Object>) addedPayload;
        assertThat(addedPayloadMap.keySet(), allOf(hasItem("depth1_map"), hasItem("depth1_list")));

        IdListDto addedIdList = facade.idList(commitInfo);
        assertThat(addedIdList.getList(), hasItem(SPEC_ID));
        log.info("-- idList: " + addedIdList);

        List<File> addedFileList = FindUtils.find(dirSplit, FileType.File);
        assertThat(addedFileList.size(), is(3));
        log.info("-- fileList: " + addedFileList);

        // ------------------------------------------------------------------------------------------
        // 更新
        // ------------------------------------------------------------------------------------------
        log.info("UPDATE");
        payload.put("KEY_FOR_UPDATE", this.getClass().getName());
        facade.update(commitInfo, SPEC_ID, payload);

        IdListDto updatedIdList = facade.idList(commitInfo);
        assertThat(updatedIdList.getList(), hasItem(SPEC_ID));
        log.info("-- idList: " + updatedIdList);

        List<File> updatedFileList = FindUtils.find(dirSplit, FileType.File);
        assertThat(updatedFileList.size(), is(3));
        log.info("-- fileList: " + updatedFileList);

        SpecDto updated = facade.findById(commitInfo, SPEC_ID);
        Object updatePayload = updated.getSpec().getPayload();
        assertThat(updatePayload, not(is(payload)));

        Map<String, Object> updatedPayloadMap = (Map<String, Object>) updatePayload;
        assertThat(updatedPayloadMap.get("KEY_FOR_UPDATE"), is(this.getClass().getName()));

        // ------------------------------------------------------------------------------------------
        // 削除
        // ------------------------------------------------------------------------------------------
        log.info("DELETE");
        facade.delete(commitInfo, SPEC_ID);

        IdListDto deletedIdList = facade.idList(commitInfo);
        assertThat(deletedIdList.getList(), not(hasItem(SPEC_ID)));
        log.info("-- idList: " + deletedIdList);

        final File dirMergedObj = new File(dirMerged);
        assertThat(dirMergedObj.exists(), is(false));

        final File dirSplitObj = new File(dirSplit);
        assertThat(dirSplitObj.exists(), is(false));
    }

}
