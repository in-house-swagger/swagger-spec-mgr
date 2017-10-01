package me.suwash.swagger.spec.manager.infra.util;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.util.Yaml;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;

final class SwaggerSpecUtilsCommon {

  /** JsonRef参照キー。 */
  protected static final String KEY_REF = "$ref";

  /** yamlファイル拡張子。 */
  protected static final String FILE_EXT = ".yaml";

  /** 統合ファイル名。 */
  protected static final String FILENAME_MERGED = "swagger" + FILE_EXT;
  /** 分割ルートファイル名。 */
  protected static final String FILENAME_SPLIT_ROOT = FILENAME_MERGED;
  /** 分割ファイル名。 */
  protected static final String FILENAME_SPLIT_MAP = "index" + FILE_EXT;

  /** swagger-coreのYaml Mapper。 */
  protected static final ObjectMapper yamlMapper = Yaml.mapper();

  private static final String DOC_START_MARKER = "^---";

  private SwaggerSpecUtilsCommon() {}

  protected static String mergedDir(final String dirPath, final String specId) {
    return FilePathUtils.dirPath(dirPath, specId);
  }

  protected static String splitDir(final String dirPath, final String specId) {
    return FilePathUtils.dirPath(dirPath, specId);
  }

  protected static String mergedFilePath(final String dirPath, final String specId) {
    return FilePathUtils.filePath(mergedDir(dirPath, specId), FILENAME_MERGED);
  }

  protected static String splitRootFilePath(final String inputDirPath, final String specId) {
    return FilePathUtils.filePath(splitDir(inputDirPath, specId), FILENAME_SPLIT_ROOT);
  }

  /**
   * yamlファイルを、Mapに変換します。
   *
   * @param inputDirPath 入力ディレクトリ
   * @param specId SpecificationID
   * @return parse結果
   */
  @SuppressWarnings("unchecked")
  protected static Map<String, Object> parse(final String inputDirPath, final String specId) {
    final File inputFile = new File(mergedFilePath(inputDirPath, specId));
    if (!inputFile.exists())
      return null;

    return parse(inputFile, Map.class);
  }

  protected static <T> T parse(final File targetFile, final Class<? extends T> clazz) {
    try {
      return yamlMapper.readValue(targetFile, clazz);
    } catch (Exception e) {
      throw new SpecMgrException("spec.cantParse", array(targetFile, e.getMessage()), e);
    }
  }

  /**
   * 指定オブジェクトを、Yaml形式の文字列に変換します。
   *
   * @param value オブジェクト
   * @return Yaml形式の文字列
   */
  protected static String writeString(final Object value) {
    try {
      // 文字列として書き出し
      String writed = yamlMapper.writeValueAsString(value);

      // DOC_START_MARKERを削除
      // TODO ファイル先頭に分割ファイルの --- が出力されている
      // YAMLGenerator.Feature.WRITE_DOC_START_MARKER を desableにすれば良さそうだが
      // SwaggerのUtilで作成された後のObjectMapperでは、YAMLGenerator.Featureを指定できない。。。
      return writed.replaceFirst(DOC_START_MARKER + "\n", StringUtils.EMPTY)
          .replaceFirst(DOC_START_MARKER + " ", StringUtils.EMPTY);

    } catch (Exception e) {
      throw new SpecMgrException("spec.cantWriteString", array(e.getMessage()), e);
    }
  }

  /**
   * 指定オブジェクトをYamlファイルとして書き出します。
   *
   * @param value オブジェクト
   * @param outputFilePath 出力ファイルパス
   */
  protected static void writeFile(final Object value, final String outputFilePath) {
    prepareOverwrite(outputFilePath);

    final File outputFile = new File(outputFilePath);
    final File parentDir = outputFile.getParentFile();
    try {
      // 文字列として書き出し
      final String parsed = writeString(value);
      // リストの全行を改行区切りで出力するので、文字列の最後の改行コードを削除
      final List<String> writeList = new ArrayList<>();
      writeList.add(parsed.replaceFirst("\n$", StringUtils.EMPTY));
      // ファイル出力
      Files.write(Paths.get(parentDir.getAbsolutePath(), outputFile.getName()), writeList,
          Charset.forName("utf8"), StandardOpenOption.CREATE);

    } catch (Exception e) {
      ValidationUtils.fileCantWrite(outputFile.toString(), e);
    }
  }

  private static void prepareOverwrite(final String outputFilePath) {
    // parentDirを作成
    final File outputFile = new File(outputFilePath);
    final File parentDir = outputFile.getParentFile();
    if (!parentDir.exists() && !parentDir.mkdirs())
      ValidationUtils.dirCantCreate(parentDir.toString());

    // 出力ファイルを上書き
    if (outputFile.exists())
      deleteFile(outputFilePath);
  }

  private static void deleteFile(final String outputFilePath) {
    try {
      Files.delete(Paths.get(outputFilePath));
    } catch (IOException e) {
      ValidationUtils.fileCantDelete(outputFilePath, e);
    }
  }

}
