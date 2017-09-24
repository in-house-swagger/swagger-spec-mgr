package me.suwash.swagger.spec.manager.sv.service;

import static me.suwash.swagger.spec.manager.SpecMgrTestUtils.assertCheckErrors;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Map;

import me.suwash.swagger.spec.manager.SpecMgrTestUtils;
import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.swagger.spec.manager.sv.domain.Spec;
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
public class SpecServiceTest {

    private static final String SPEC_ID = SpecServiceTest.class.getSimpleName();
    private static final String dirData = TestConst.DIR_DATA + "/" + TestConst.COMMITUSER_DEFAULT;
    private static final String dirMerged = dirData + "/" + TestConst.DIRNAME_MERGED + "/" + SPEC_ID;
    private static final String dirSplit = dirData + "/" + TestConst.DIRNAME_SPLIT + "/" + SPEC_ID;

    @Autowired
    private SpecMgrContext context;
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

    @Test
    public void testSpec() {
        // ------------------------------------------------------------------------------------------
        // 準備
        // ------------------------------------------------------------------------------------------
        // payload
        Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

        // ------------------------------------------------------------------------------------------
        // 検索
        // ------------------------------------------------------------------------------------------
        // idが未設定
        try {
            service.findById(null);
            fail();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }

        // ------------------------------------------------------------------------------------------
        // 追加
        // ------------------------------------------------------------------------------------------
        // idが未設定
        try {
            service.newSpec(null, payload).add();
            fail();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }

        // payloadが未設定
        try {
            service.newSpec("error", null).add();
            fail();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotNull"
            });
            context.clearErrors();
        }

        // id, payloadが未設定
        try {
            service.newSpec("", null).add();
            fail();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty", "BeanValidator.NotNull"
            });
            context.clearErrors();
        }

        // 作成済み
        Spec errorSpec = service.newSpec(SPEC_ID + "_error", payload);
        errorSpec.add();
        try {
            errorSpec.add();
            fail();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                MessageConst.DATA_ALREADY_EXIST
            });
            context.clearErrors();
        }

        // ------------------------------------------------------------------------------------------
        // 更新
        // ------------------------------------------------------------------------------------------
        // payloadが未設定
        try {
            errorSpec.update(null);
            fail();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotNull"
            });
            context.clearErrors();
        }

        // 削除済み
        errorSpec.delete();
        try {
            errorSpec.update(payload);
            fail();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                MessageConst.DATA_NOT_EXIST
            });
            context.clearErrors();
        }

        // ------------------------------------------------------------------------------------------
        // 削除
        // ------------------------------------------------------------------------------------------
        // id が未設定
        try {
            service.newSpec(null, null).delete();
            fail();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                "BeanValidator.NotEmpty"
            });
            context.clearErrors();
        }
        // 削除済み
        try {
            errorSpec.delete();
            fail();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.SPECIFICATION_ERROR));
            assertCheckErrors(context, new String[] {
                MessageConst.DATA_NOT_EXIST
            });
            context.clearErrors();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test() {
        // ------------------------------------------------------------------------------------------
        // 準備
        // ------------------------------------------------------------------------------------------
        // payload
        Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

        // ------------------------------------------------------------------------------------------
        // 0件
        // ------------------------------------------------------------------------------------------
        List<String> beforeIdList = service.idList();
        assertThat(beforeIdList, not(hasItem(SPEC_ID)));

        try {
            service.findById(SPEC_ID);
            fail();
        } catch (SpecMgrException e) {
            assertThat(e.getMessageId(), is(MessageConst.DATA_NOT_EXIST));
        }

        // ------------------------------------------------------------------------------------------
        // 追加
        // ------------------------------------------------------------------------------------------
        log.info("ADD");
        Spec spec = service.newSpec(SPEC_ID, payload);
        assertThat(spec.getId(), is(SPEC_ID));
        assertThat(spec.getPayload(), is(payload));

        spec.add();
        List<String> addedIdList = service.idList();
        assertThat(addedIdList, hasItem(SPEC_ID));
        log.info("-- idList: " + addedIdList);

        List<File> addedFileList = FindUtils.find(dirSplit, FileType.File);
        assertThat(addedFileList.size(), is(3));
        log.info("-- fileList: " + addedFileList);

        // ------------------------------------------------------------------------------------------
        // 更新
        // ------------------------------------------------------------------------------------------
        log.info("UPDATE");
        payload.put("KEY_FOR_UPDATE", this.getClass().getName());
        spec.update(payload);

        List<String> updatedIdList = service.idList();
        assertThat(updatedIdList, hasItem(SPEC_ID));
        log.info("-- idList: " + updatedIdList);

        List<File> updatedFileList = FindUtils.find(dirSplit, FileType.File);
        assertThat(updatedFileList.size(), is(3));
        log.info("-- fileList: " + updatedFileList);

        Spec updated = service.findById(SPEC_ID);
        Object updatePayload = updated.getPayload();
        assertThat(updatePayload, not(is(payload)));

        Map<String, Object> updatedPayloadMap = (Map<String, Object>) updatePayload;
        assertThat(updatedPayloadMap.get("KEY_FOR_UPDATE"), is(this.getClass().getName()));

        // ------------------------------------------------------------------------------------------
        // 削除
        // ------------------------------------------------------------------------------------------
        log.info("DELETE");
        spec.delete();

        List<String> deletedIdList = service.idList();
        assertThat(deletedIdList, not(hasItem(SPEC_ID)));
        log.info("-- idList: " + deletedIdList);

        final File dirMergedObj = new File(dirMerged);
        assertThat(dirMergedObj.exists(), is(false));

        final File dirSplitObj = new File(dirSplit);
        assertThat(dirSplitObj.exists(), is(false));
    }

}
