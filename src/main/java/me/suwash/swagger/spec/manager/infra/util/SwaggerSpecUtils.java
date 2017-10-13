package me.suwash.swagger.spec.manager.infra.util;

import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.FILENAME_MERGED;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.mergedFilePath;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.splitDir;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.splitRootFilePath;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.writeFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Swagger Specification File操作ユーティリティ。
 */
public final class SwaggerSpecUtils {

  private SwaggerSpecUtils() {}

  private static void checkSpecId(final String specId) {
    ValidationUtils.mustNotEmpty("specId", specId);
  }

  private static void checkInputDirPath(final String inputDirPath) {
    ValidationUtils.mustNotEmpty("inputDirPath", inputDirPath);
    ValidationUtils.mustExistDir(inputDirPath);
  }

  private static void checkOutputDirPath(final String outputDirPath) {
    ValidationUtils.mustNotEmpty("outputDirPath", outputDirPath);
    ValidationUtils.mustExistDirForce(outputDirPath);
  }

  private static void checkValue(final Object value) {
    ValidationUtils.mustNotNull("value", value);
  }

  public static String getSplitDir(final String dirPath, final String specId) {
    return splitDir(dirPath, specId);
  }

  /**
   * yamlファイルをMapに変換します。
   *
   * @param inputDirPath 入力ディレクトリ
   * @param specId SpecificationID
   * @return parse結果
   */
  public static Map<String, Object> parse(final String inputDirPath, final String specId) {
    checkInputDirPath(inputDirPath);
    checkSpecId(specId);

    return SwaggerSpecUtilsCommon.parse(inputDirPath, specId);
  }

  /**
   * 指定オブジェクトを、Yaml形式の文字列に変換します。
   *
   * @param value オブジェクト
   * @return Yaml形式の文字列
   */
  public static String writeString(final Object value) {
    checkValue(value);
    return SwaggerSpecUtilsCommon.writeString(value);
  }

  /**
   * 指定されたObjectを、jsonRefで分割したファイルとして保存します。
   *
   * @param value 入力オブジェクト
   * @param outputDirPath 出力ディレクトリ
   * @param specId SpecificationID
   * @return 分割結果のルートファイルパス
   */
  public static String writeSplit(final Object value, final String outputDirPath,
      final String specId) {
    return writeSplit(value, outputDirPath, specId, null);
  }

  /**
   * 指定されたObjectを、jsonRefで分割したファイルとして保存します。
   *
   * @param value 入力オブジェクト
   * @param outputDirPath 出力ディレクトリ
   * @param specId SpecificationID
   * @param ignoreRegexList 分割除外パスの正規表現リスト
   * @return 分割結果のルートファイルパス
   */
  public static String writeSplit(final Object value, final String outputDirPath,
      final String specId, final List<String> ignoreRegexList) {

    checkValue(value);
    checkOutputDirPath(outputDirPath);
    checkSpecId(specId);

    List<Pattern> ignorePatternList = new ArrayList<>();
    if (ignoreRegexList != null)
      convertPatternList(ignoreRegexList, outputDirPath, specId, ignorePatternList);

    final String outputRootDirPath = splitDir(outputDirPath, specId);
    return SwaggerSpecUtilsSplit.recursiveSplit(value, outputRootDirPath, FILENAME_MERGED,
        ignorePatternList);
  }

  private static void convertPatternList(final List<String> ignoreRegexList,
      final String outputDirPath, final String specId, List<Pattern> ignorePatternList) {
    for (final String curIgnoreRegex : ignoreRegexList) {
      Pattern curPattern = convertPattern(curIgnoreRegex, outputDirPath, specId);
      ignorePatternList.add(curPattern);
    }
  }

  private static Pattern convertPattern(final String ignoreRegex, final String outputDirPath,
      final String specId) {
    // 出力ディレクトリを付与
    String effectiveIgnoreRegex = ignoreRegex;
    if (!ignoreRegex.contains(outputDirPath))
      effectiveIgnoreRegex = splitDir(outputDirPath, specId) + ignoreRegex;

    return Pattern.compile(effectiveIgnoreRegex);
  }

  /**
   * 分割ファイルを削除します。
   *
   * @param targetDirPath 対象ディレクトリ
   * @param specId SpecificationID
   */
  public static void deleteSplit(final String targetDirPath, final String specId) {
    checkInputDirPath(targetDirPath);
    checkSpecId(specId);

    SwaggerSpecUtilsSplit.deleteSplit(targetDirPath, specId);
  }

  /**
   * 指定ディレクトリ、SpecificationID配下の分割ファイルを、統合したファイルを出力します。
   *
   * @param inputDirPath 入力ディレクトリ
   * @param outputDirPath 出力ディレクトリ
   * @param specId SpecificationID
   * @return 統合結果のファイルパス
   */
  public static String writeMerged(final String inputDirPath, final String outputDirPath,
      final String specId) {
    checkInputDirPath(inputDirPath);
    checkOutputDirPath(outputDirPath);
    checkSpecId(specId);

    final String inputRootFilePath = splitRootFilePath(inputDirPath, specId);
    ValidationUtils.mustExistFile(inputRootFilePath);

    // 分割ファイルをmerge
    final Object merged = SwaggerSpecUtilsMerge.recursiveMerge(inputRootFilePath);

    // merge結果を書き出し
    final String outputFilePath = mergedFilePath(outputDirPath, specId);
    writeFile(merged, outputFilePath);
    return outputFilePath;
  }

  /**
   * 統合ファイルを削除します。
   *
   * @param inputDirPath 対象ディレクトリ
   * @param specId SpecificationID
   */
  public static void deleteMerged(final String inputDirPath, final String specId) {
    checkInputDirPath(inputDirPath);
    checkSpecId(specId);

    SwaggerSpecUtilsMerge.deleteMerged(inputDirPath, specId);
  }

}
