package me.suwash.swagger.spec.manager.sv.service;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
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
public class SpecServiceTest {

    private static final String SPEC_ID = SpecServiceTest.class.getSimpleName();
    private static final String dirData = TestConst.DIR_DATA + "/" + TestConst.SCMUSER_DEFAULT;
    private static final String dirMerged = dirData + "/" + TestConst.DIRNAME_MERGED + "/" + SPEC_ID;
    private static final String dirSplit = dirData + "/" + TestConst.DIRNAME_SPLIT + "/" + SPEC_ID;

    @Autowired
    private SpecService service;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.info(SpecServiceTest.class.getSimpleName());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {
        FileUtils.rmdirs(dirMerged);
        FileUtils.rmdirs(dirSplit);
    }

    @After
    public void tearDown() throws Exception {}

    @SuppressWarnings("unchecked")
    @Test
    public final void test() {
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
            service.newSpec(null, null);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }
        try {
            service.newSpec(SPEC_ID, null);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }

        try {
            service.findById(StringUtils.EMPTY);
        } catch (final SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.CHECK_NOTNULL));
        }


        //------------------------------------------------------------------------------------------
        // 0件
        //------------------------------------------------------------------------------------------
        List<String> beforeIdList = service.idList();
        assertThat(beforeIdList, not(hasItem(SPEC_ID)));

        Spec finded = service.findById(SPEC_ID);
        assertThat(finded, is(nullValue()));


        //------------------------------------------------------------------------------------------
        // 追加
        //------------------------------------------------------------------------------------------
        log.info("-- add");
        Spec spec = service.newSpec(SPEC_ID, payload);
        assertThat(spec.getId(), is(SPEC_ID));
        assertThat(spec.getPayload(), is(payload));

        spec.add();
        List<String> addedIdList = service.idList();
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
        spec.update(payload);

        List<String> updatedIdList = service.idList();
        assertThat(updatedIdList, hasItem(SPEC_ID));
        log.info("---- idList: " + updatedIdList);

        List<File> updatedFileList = FindUtils.find(dirSplit, FileType.File);
        assertThat(updatedFileList.size(), is(3));
        log.info("---- fileList: " + updatedFileList);

        Spec updated = service.findById(SPEC_ID);
        Object updatePayload = updated.getPayload();
        assertThat(updatePayload, not(is(payload)));

        Map<String, Object> updatedPayloadMap = (Map<String, Object>) updatePayload;
        assertThat(updatedPayloadMap.get("KEY_FOR_UPDATE"), is(this.getClass().getName()));


        //------------------------------------------------------------------------------------------
        // 削除
        //------------------------------------------------------------------------------------------
        log.info("-- delete");
        spec.delete();

        List<String> deletedIdList = service.idList();
        assertThat(deletedIdList, not(hasItem(SPEC_ID)));
        log.info("---- idList: " + deletedIdList);

        final File dirMergedObj = new File(dirMerged);
        assertThat(dirMergedObj.exists(), is(false));

        final File dirSplitObj = new File(dirSplit);
        assertThat(dirSplitObj.exists(), is(false));
     }

}
