package me.suwash.swagger.spec.manager.sv.service;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
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
import me.suwash.swagger.spec.manager.sv.domain.Spec;
import me.suwash.swagger.spec.manager.sv.domain.Tag;
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
public class TagServiceTest {

    private static final String TAG_ID = "v1.0";
    private static final String COMMIT_USER = TagServiceTest.class.getSimpleName();
    private static final String SPEC_ID = "sample_spec";
    private static final String dirData = TestConst.DIR_DATA + "/" + COMMIT_USER;

    @Autowired
    private SpecMgrContext context;
    @Autowired
    private SpecService specService;
    @Autowired
    private TagService service;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.info(TagServiceTest.class.getSimpleName());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {
        FileUtils.rmdirs(dirData);
    }

    @After
    public void tearDown() throws Exception {}

    @Test
    public final void test() {
        //------------------------------------------------------------------------------------------
        // 準備
        //------------------------------------------------------------------------------------------
        final CommitInfo commitInfo = new CommitInfo(COMMIT_USER, COMMIT_USER + "@example.com", "TagService test tag.");
        final String threadName = Thread.currentThread().getName();
        this.context.put(threadName, CommitInfo.class.getName(), commitInfo);

        // payload
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> depth1_map = new HashMap<>();
        depth1_map.put("depth1.now", LocalDate.now());
        payload.put("depth1", depth1_map);

        // リポジトリ初期化
        Spec spec = specService.newSpec(SPEC_ID, payload);
        spec.add();

        //------------------------------------------------------------------------------------------
        // 入力チェック
        //------------------------------------------------------------------------------------------
        try {
            service.newTag(null);
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
        assertThat(beforeIdList, not(hasItem(TAG_ID)));

        Tag finded = service.findById(TAG_ID);
        assertThat(finded, is(nullValue()));


        //------------------------------------------------------------------------------------------
        // 追加
        //------------------------------------------------------------------------------------------
        log.info("ADD");
        Tag tag = service.newTag(TAG_ID);
        assertThat(tag.getId(), is(TAG_ID));
        assertThat(tag.getGitObject(), is(nullValue()));

        tag.add("master");
        List<String> addedIdList = service.idList();
        assertThat(addedIdList, hasItem(TAG_ID));
        log.info("-- idList: " + addedIdList);

        Tag added = service.findById(TAG_ID);
        assertThat(added, not(nullValue()));
        assertThat(added.getId(), is(TAG_ID));

        Tag tag2 = service.newTag(TAG_ID + ".1");
        tag2.add(TAG_ID);
        List<String> addedIdList2 = service.idList();
        assertThat(addedIdList2, hasItem(TAG_ID + ".1"));
        log.info("-- idList: " + addedIdList2);

        Tag added2 = service.findById(TAG_ID + ".1");
        assertThat(added2, not(nullValue()));
        assertThat(added2.getId(), is(TAG_ID + ".1"));

        //------------------------------------------------------------------------------------------
        // 更新
        //------------------------------------------------------------------------------------------
        log.info("UPDATE");
        added.rename("RENAMED_" + TAG_ID);
        Tag renamed = service.findById("RENAMED_" + TAG_ID);
        assertThat(renamed.getId(), is("RENAMED_" + TAG_ID));


        //------------------------------------------------------------------------------------------
        // 削除
        //------------------------------------------------------------------------------------------
        log.info("DELETE");
        renamed.delete();

        List<String> deletedIdList = service.idList();
        assertThat(deletedIdList, not(hasItem(TAG_ID)));
        log.info("-- idList: " + deletedIdList);
     }

}
