package me.suwash.swagger.spec.manager.infra.util;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.util.Yaml;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.test.TestUtils;
import me.suwash.util.CompareUtils;
import me.suwash.util.FileUtils;

@lombok.extern.slf4j.Slf4j
public class SwaggerSpecUtilsTest {
  /** yaml mapper。 */
  private static final ObjectMapper mapper = Yaml.mapper();

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    log.info(SwaggerSpecUtilsTest.class.getSimpleName());
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  private static final String DIR_BASE =
      "src/test/scripts/util/" + SwaggerSpecUtilsTest.class.getSimpleName();

  @Test
  public final void testParse() {
    // -----------------------------------------------------------------------------------------
    // 異常系
    // -----------------------------------------------------------------------------------------
    // nullチェック
    try {
      SwaggerSpecUtils.parse(null, null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // nullチェック
    try {
      SwaggerSpecUtils.parse("/tmp", null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // ファイル存在チェック
    try {
      SwaggerSpecUtils.parse("/tmp", "dummy");
    } catch (SpecMgrException e) {
      assertEquals("file.notExist", e.getMessageId());
      log.debug(e.getMessage());
    }

    // -----------------------------------------------------------------------------------------
    // TODO 正常系
    // -----------------------------------------------------------------------------------------
  }

  @Test
  public final void testWriteString() {
    // -----------------------------------------------------------------------------------------
    // 異常系
    // -----------------------------------------------------------------------------------------
    // nullチェック
    try {
      SwaggerSpecUtils.writeString(null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // -----------------------------------------------------------------------------------------
    // TODO 正常系
    // -----------------------------------------------------------------------------------------
  }

  @SuppressWarnings("unchecked")
  @Test
  public final void testWriteSplit() throws JsonParseException, JsonMappingException, IOException {
    // -----------------------------------------------------------------------------------------
    // 異常系
    // -----------------------------------------------------------------------------------------
    // nullチェック
    try {
      SwaggerSpecUtils.writeSplit(null, null, null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // nullチェック
    try {
      SwaggerSpecUtils.writeSplit(new Object(), null, null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // nullチェック
    try {
      SwaggerSpecUtils.writeSplit(new Object(), "/path/to/notExist", null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // -----------------------------------------------------------------------------------------
    // 設定
    // -----------------------------------------------------------------------------------------
    final String DIR_METHOD = DIR_BASE + "/writeSplit";
    final String DIR_INPUT = DIR_METHOD + "/input";
    final String DIR_OUTPUT = DIR_METHOD + "/output";
    final String DIR_EXPECT = DIR_METHOD + "/expect";
    final String specId = "sample";

    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    // yamlのparse
    final File inputFile = new File(DIR_INPUT + "/" + specId + "/swagger.yaml");
    final Map<String, Object> content = mapper.readValue(inputFile, Map.class);

    // 出力ディレクトリの初期化
    if (!FileUtils.rmdirs(DIR_OUTPUT + "/" + specId)) {
      throw new RuntimeException("出力ディレクトリの初期化に失敗しました。ディレクトリ：" + DIR_OUTPUT + "/" + specId);
    }

    // -----------------------------------------------------------------------------------------
    // 実行
    // -----------------------------------------------------------------------------------------
    SwaggerSpecUtils.writeSplit(content, DIR_OUTPUT, specId);

    // -----------------------------------------------------------------------------------------
    // 確認
    // -----------------------------------------------------------------------------------------
    TestUtils.assertDirEquals(DIR_EXPECT, DIR_OUTPUT);
  }

  @Test
  public final void testWriteMerged() {
    // -----------------------------------------------------------------------------------------
    // 異常系
    // -----------------------------------------------------------------------------------------
    // nullチェック
    try {
      SwaggerSpecUtils.writeMerged(null, null, null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // nullチェック
    try {
      SwaggerSpecUtils.writeMerged("/tmp", null, null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // nullチェック
    try {
      SwaggerSpecUtils.writeMerged("/tmp", "/tmp/output", null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // 入力ディレクトリチェック
    try {
      SwaggerSpecUtils.writeMerged("/path/to/input", "/tmp", "specId");
    } catch (SpecMgrException e) {
      assertEquals("dir.notExist", e.getMessageId());
      log.debug(e.getMessage());
    }

    // 入力ファイルチェック
    try {
      SwaggerSpecUtils.writeMerged("/tmp", "/path/to/output", "specId");
    } catch (SpecMgrException e) {
      assertEquals("file.notExist", e.getMessageId());
      log.debug(e.getMessage());
    }

    // -----------------------------------------------------------------------------------------
    // 設定
    // -----------------------------------------------------------------------------------------
    final String DIR_METHOD = DIR_BASE + "/writeMerged";
    final String DIR_INPUT = DIR_METHOD + "/input";
    final String DIR_OUTPUT = DIR_METHOD + "/output";
    final String DIR_EXPECT = DIR_METHOD + "/expect";
    final String specId = "sample";

    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    // 出力ディレクトリの初期化
    if (!FileUtils.rmdirs(DIR_OUTPUT + "/" + specId)) {
      throw new RuntimeException("出力ディレクトリの初期化に失敗しました。ディレクトリ：" + DIR_OUTPUT + "/" + specId);
    }

    // -----------------------------------------------------------------------------------------
    // 実行
    // -----------------------------------------------------------------------------------------
    final String outputFilePath = SwaggerSpecUtils.writeMerged(DIR_INPUT, DIR_OUTPUT, specId);

    // -----------------------------------------------------------------------------------------
    // 確認
    // -----------------------------------------------------------------------------------------
    TestUtils.assertFileEquals(DIR_EXPECT + "/" + specId + "/swagger.yaml", outputFilePath, "utf8");
  }

  @SuppressWarnings("unchecked")
  @Test
  public final void testSplitAndMerge()
      throws JsonParseException, JsonMappingException, IOException {
    // -----------------------------------------------------------------------------------------
    // 設定
    // -----------------------------------------------------------------------------------------
    final String DIR_METHOD = DIR_BASE + "/splitAndMerge";
    final String DIR_INPUT = DIR_METHOD + "/input";
    final String DIR_OUTPUT = DIR_METHOD + "/output";
    final String DIR_OUTPUT_SPLIT = DIR_OUTPUT + "/split";
    final String DIR_OUTPUT_MERGED = DIR_OUTPUT + "/merged";
    final String DIR_EXPECT = DIR_METHOD + "/expect";
    final String DIR_EXPECT_SPLIT = DIR_EXPECT + "/split";
    final String specId = "petstore";

    // 分割除外パス 正規表現リスト
    final List<String> ignorePathList = Arrays.asList(new String[] {"/info/.*", "/tags/.*",
        "/schemas/.*", "/securityDefinitions/.*", "/paths/.*/consumes", "/paths/.*/parameters",
        "/paths/.*/produces", "/paths/.*/responses", "/paths/.*/tags", "/paths/.*/security"});

    // -----------------------------------------------------------------------------------------
    // 準備
    // -----------------------------------------------------------------------------------------
    // yamlのparse
    final String inputFilePath = DIR_INPUT + "/" + specId + "/swagger.yaml";
    final Map<String, Object> inputContent = mapper.readValue(new File(inputFilePath), Map.class);

    // 出力ディレクトリの初期化
    if (!FileUtils.rmdirs(DIR_OUTPUT)) {
      throw new RuntimeException("出力ディレクトリの初期化に失敗しました。ディレクトリ：" + DIR_OUTPUT);
    }

    // -----------------------------------------------------------------------------------------
    // 実行
    // -----------------------------------------------------------------------------------------
    // split
    SwaggerSpecUtils.writeSplit(inputContent, DIR_OUTPUT_SPLIT, specId, ignorePathList);
    // merge
    final String mergedFilePath =
        SwaggerSpecUtils.writeMerged(DIR_OUTPUT_SPLIT, DIR_OUTPUT_MERGED, specId);
    // parse
    final Map<String, Object> parsed = SwaggerSpecUtils.parse(DIR_OUTPUT_MERGED, specId);

    // -----------------------------------------------------------------------------------------
    // 確認
    // -----------------------------------------------------------------------------------------
    // --------------------------------------------------
    // merged
    // --------------------------------------------------
    // ファイル比較
    TestUtils.assertFileEquals(inputFilePath, mergedFilePath, "utf8");

    // Inputと同じ方法での、parse結果比較
    Map<String, Object> mergedContent = mapper.readValue(new File(mergedFilePath), Map.class);
    assertEquals(0, CompareUtils.deepCompare(inputContent, mergedContent));

    // 提供メソッドでの、parse結果比較
    assertEquals(0, CompareUtils.deepCompare(inputContent, parsed));

    // --------------------------------------------------
    // split
    // --------------------------------------------------
    // 期待値とファイル比較
    TestUtils.assertDirEquals(DIR_EXPECT_SPLIT, DIR_OUTPUT_SPLIT);

  }

  @Test
  public final void testDeleteMerged() {
    // -----------------------------------------------------------------------------------------
    // 異常系
    // -----------------------------------------------------------------------------------------
    // nullチェック
    try {
      SwaggerSpecUtils.deleteMerged(null, null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // nullチェック
    try {
      SwaggerSpecUtils.deleteMerged("/tmp", null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // ファイルチェック
    try {
      SwaggerSpecUtils.deleteMerged("/tmp", "dummy");
    } catch (SpecMgrException e) {
      assertEquals("file.notExist", e.getMessageId());
      log.debug(e.getMessage());
    }

    // -----------------------------------------------------------------------------------------
    // TODO 正常系
    // -----------------------------------------------------------------------------------------
  }

  @Test
  public final void testDeleteSplit() {
    // -----------------------------------------------------------------------------------------
    // 異常系
    // -----------------------------------------------------------------------------------------
    // nullチェック
    try {
      SwaggerSpecUtils.deleteSplit(null, null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // nullチェック
    try {
      SwaggerSpecUtils.deleteSplit("/tmp", null);
    } catch (SpecMgrException e) {
      assertEquals("check.notNull", e.getMessageId());
      log.debug(e.getMessage());
    }

    // ファイルチェック
    try {
      SwaggerSpecUtils.deleteSplit("/tmp", "dummy");
    } catch (SpecMgrException e) {
      assertEquals("file.notExist", e.getMessageId());
      log.debug(e.getMessage());
    }

    // -----------------------------------------------------------------------------------------
    // TODO 正常系
    // -----------------------------------------------------------------------------------------
  }

  // @Test
  // public void sample() {
  // Object value = JsonUtils.parseFile("/tmp/swagger.json", "utf8", Map.class);
  // SwaggerSpecUtils.writeSplit(value, "/tmp/split", "sample");
  // SwaggerSpecUtils.writeMerged("/tmp/split", "/tmp/merged", "sample");
  // }

}
