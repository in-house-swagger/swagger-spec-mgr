package me.suwash.swagger.spec.manager.ap.facade;

import static me.suwash.swagger.spec.manager.SpecMgrTestUtils.assertCheckErrors;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.util.Map;

import me.suwash.swagger.spec.manager.SpecMgrTestUtils;
import me.suwash.swagger.spec.manager.TestCommandLineRunner;
import me.suwash.swagger.spec.manager.TestConst;
import me.suwash.swagger.spec.manager.ap.dto.IdListDto;
import me.suwash.swagger.spec.manager.ap.dto.TagDto;
import me.suwash.swagger.spec.manager.infra.config.CommitInfo;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.constant.MessageConst;
import me.suwash.util.FileUtils;

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
    private static final String SPEC_ID = "sample_spec";
    private static final String COMMIT_USER = TagFacadeTest.class.getSimpleName();

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
    public final void test_error() {
        // ------------------------------------------------------------------------------------------
        // 準備
        // ------------------------------------------------------------------------------------------
        final String commitUser = COMMIT_USER + "_error";
        final String dirData = TestConst.DIR_DATA + "/" + commitUser;
        FileUtils.rmdirs(dirData);

        final CommitInfo commitInfo = new CommitInfo(commitUser, commitUser + "@example.com");

        // payload
        Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

        // リポジトリ初期化
        specFacade.add(commitInfo, SPEC_ID, payload);

        // ------------------------------------------------------------------------------------------
        // 検索
        // ------------------------------------------------------------------------------------------
        TagDto dto = facade.findById(commitInfo, "");
        assertCheckErrors(dto.getErrors(), new String[] {
            "BeanValidator.NotEmpty"
        });
        dto = facade.findById(commitInfo, "v1.0.0");
        assertCheckErrors(dto.getErrors(), new String[] {
            MessageConst.DATA_NOT_EXIST
        });

        // ------------------------------------------------------------------------------------------
        // 追加
        // ------------------------------------------------------------------------------------------
        dto = facade.add(commitInfo, "", "");
        assertCheckErrors(dto.getErrors(), new String[] {
            "BeanValidator.NotEmpty", "BeanValidator.NotEmpty"
        });
        dto = facade.add(commitInfo, "master", "");
        assertCheckErrors(dto.getErrors(), new String[] {
            "BeanValidator.NotEmpty"
        });

        // ------------------------------------------------------------------------------------------
        // リネーム
        // ------------------------------------------------------------------------------------------
        dto = facade.rename(commitInfo, "", "");
        assertCheckErrors(dto.getErrors(), new String[] {
            "BeanValidator.NotEmpty"
        });

        // ------------------------------------------------------------------------------------------
        // 削除
        // ------------------------------------------------------------------------------------------
        dto = facade.delete(commitInfo, "");
        assertCheckErrors(dto.getErrors(), new String[] {
            "BeanValidator.NotEmpty"
        });
    }

    @Test
    public final void test() {
        // ------------------------------------------------------------------------------------------
        // 準備
        // ------------------------------------------------------------------------------------------
        final String dirData = TestConst.DIR_DATA + "/" + COMMIT_USER;
        FileUtils.rmdirs(dirData);

        final CommitInfo commitInfo = new CommitInfo(COMMIT_USER, COMMIT_USER + "@example.com");

        // payload
        Map<String, Object> payload = SpecMgrTestUtils.getTestPayload();

        // リポジトリ初期化
        specFacade.add(commitInfo, SPEC_ID, payload);

        // ------------------------------------------------------------------------------------------
        // 検索
        // ------------------------------------------------------------------------------------------
        IdListDto idListDto = facade.idList(commitInfo);
        assertThat(idListDto.getList(), not(hasItem("v1.0.0")));

        // ------------------------------------------------------------------------------------------
        // 追加
        // ------------------------------------------------------------------------------------------
        log.info("ADD");
        TagDto dto = facade.add(commitInfo, "master", "v1.0.0");
        assertThat(dto.getTag().getId(), is("v1.0.0"));

        idListDto = facade.idList(commitInfo);
        assertThat(idListDto.getList(), hasItem("v1.0.0"));
        log.info("-- idList: " + idListDto.getList());

        dto = facade.findById(commitInfo, "v1.0.0");
        assertThat(dto.getTag().getId(), is("v1.0.0"));

        // ------------------------------------------------------------------------------------------
        // 更新
        // ------------------------------------------------------------------------------------------
        log.info("UPDATE");
        dto = facade.rename(commitInfo, "v1.0.0", "ver1.0.0");

        idListDto = facade.idList(commitInfo);
        assertThat(idListDto.getList(), hasItem("ver1.0.0"));
        log.info("-- idList: " + idListDto.getList());

        // ------------------------------------------------------------------------------------------
        // 削除
        // ------------------------------------------------------------------------------------------
        log.info("DELETE");
        facade.delete(commitInfo, "ver1.0.0");

        idListDto = facade.idList(commitInfo);
        assertThat(idListDto.getList(), not(hasItem("ver1.0.0")));
        log.info("-- idList: " + idListDto.getList());
    }

}
