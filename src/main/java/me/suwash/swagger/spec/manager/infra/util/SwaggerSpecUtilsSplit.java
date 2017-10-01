package me.suwash.swagger.spec.manager.infra.util;

import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.FILENAME_SPLIT_MAP;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.FILENAME_SPLIT_ROOT;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.FILE_EXT;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.KEY_REF;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.splitDir;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.writeFile;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.suwash.util.FileUtils;

public final class SwaggerSpecUtilsSplit {

  private SwaggerSpecUtilsSplit() {}

  /**
   * 指定されたObjectを、jsonRefで再帰的に分割します。
   *
   * @param target 入力オブジェクト
   * @param outputDirPath 出力ディレクトリ
   * @param outputFileName 出力ファイル名
   * @param ignorePatternList 分割除外パスリスト
   * @return 出力ファイルパス
   */
  @SuppressWarnings("unchecked")
  protected static String recursiveSplit(final Object target, final String outputDirPath,
      final String outputFileName, final List<Pattern> ignorePatternList) {

    // 出力ファイルパス
    final String outputFilePath = FilePathUtils.filePath(outputDirPath, outputFileName);

    if (target instanceof Map) {
      recursiveSplitOnMap((Map<String, Object>) target, ignorePatternList, outputDirPath,
          outputFilePath);
      return outputFilePath;
    }

    if (target instanceof List) {
      recursiveSplitOnList((List<Object>) target, ignorePatternList, outputDirPath, outputFilePath);
      return outputFilePath;
    }

    // target = その他の型 の場合、エラー
    ValidationUtils.illegalArgs(target.getClass().getName());
    return null;
  }

  private static void recursiveSplitOnMap(final Map<String, Object> targetMap,
      final List<Pattern> ignorePatternList, final String outputDirPath,
      final String outputFilePath) {

    Map<String, Object> replacedMap = new LinkedHashMap<>();
    for (final Map.Entry<String, Object> curTargetEntry : targetMap.entrySet())
      recursiveSplitOnMapEntry(curTargetEntry, ignorePatternList, outputDirPath, replacedMap);

    // targetの書き出し
    writeFile(replacedMap, outputFilePath);
  }

  private static void recursiveSplitOnMapEntry(final Map.Entry<String, Object> curTargetEntry,
      final List<Pattern> ignorePatternList, final String outputDirPath,
      Map<String, Object> replacedMap) {
    String curTargetKey = curTargetEntry.getKey();
    Object curTargetValue = curTargetEntry.getValue();
    // value = Map or List の場合、再帰呼び出し
    if (curTargetValue instanceof Map || curTargetValue instanceof List) {
      recursiveSplitOnMapHasModel(curTargetKey, curTargetValue, ignorePatternList, outputDirPath,
          replacedMap);
      return;
    }

    // elem = その他の型 の場合、そのまま置換後mapに登録
    replacedMap.put(curTargetKey, curTargetValue);
  }

  private static void recursiveSplitOnMapHasModel(final String curTargetKey,
      final Object curTargetValue, final List<Pattern> ignorePatternList,
      final String outputDirPath, final Map<String, Object> replacedMap) {

    String curWriteDirPath =
        splitDir(outputDirPath, FilePathUtils.absolutePathToCurRel(curTargetKey));
    if (isSplit(ignorePatternList, curWriteDirPath)) {
      // valueで再帰呼び出し
      String curWrittenFilePath =
          recursiveSplit(curTargetValue, curWriteDirPath, FILENAME_SPLIT_MAP, ignorePatternList);
      // 置換後mapに、jsonRefのentryを追加
      replacedMap.put(curTargetKey, getRefMap(curWrittenFilePath, outputDirPath));
      return;
    }
    replacedMap.put(curTargetKey, curTargetValue);
  }

  private static void recursiveSplitOnList(final List<Object> targetList,
      final List<Pattern> ignorePatternList, final String outputDirPath,
      final String outputFilePath) {

    List<Object> replacedList = new ArrayList<>();
    for (int idx = 0; idx < targetList.size(); idx++)
      recursiveSplitOnListElem(idx, targetList, ignorePatternList, outputDirPath, replacedList);

    writeFile(replacedList, outputFilePath);
  }

  private static void recursiveSplitOnListElem(final int idx, final List<Object> targetList,
      final List<Pattern> ignorePatternList, final String outputDirPath,
      final List<Object> replacedList) {
    final Object curTargetElem = targetList.get(idx);

    // elem = Map の場合、再帰呼び出し
    if (curTargetElem instanceof Map) {
      recursiveSplitOnListHasModel(idx, curTargetElem, ignorePatternList, outputDirPath,
          replacedList);
      return;
    }

    // elem = その他の型 の場合、そのまま置換後listに登録
    replacedList.add(curTargetElem);
  }

  private static void recursiveSplitOnListHasModel(final int idx, final Object curTargetElem,
      final List<Pattern> ignorePatternList, final String outputDirPath,
      final List<Object> replacedList) {

    final String curWriteFileName = (idx + 1) + FILE_EXT;
    if (isSplit(ignorePatternList, outputDirPath)) {
      // elemで再帰呼び出し ※ファイル名は、連番を利用
      final String curWrittenFilePath =
          recursiveSplit(curTargetElem, outputDirPath, curWriteFileName, ignorePatternList);

      // 置換後listに、jsonRefのentryを追加
      replacedList.add(getRefMap(curWrittenFilePath, outputDirPath));
      return;
    }
    replacedList.add(curTargetElem);
  }

  /**
   * 除外パスリストから、分割の実施要否を判断します。
   *
   * @param ignorePatternList 除外パス正規表現リスト
   * @param targetPath チェック対象
   * @return 分割する場合、true
   */
  private static boolean isSplit(List<Pattern> ignorePatternList, String targetPath) {
    // 除外パス正規表現リストのパスで始まる場合、除外
    for (final Pattern curPattern : ignorePatternList) {
      final Matcher matcher = curPattern.matcher(targetPath);
      if (matcher.matches())
        return false;
    }
    return true;
  }

  /**
   * jsonRef形式のMapを作成します。
   *
   * @param fullPath 参照先のフルパス
   * @param parentPath 親ディレクトリ
   * @return jsonRef形式のMap
   */
  private static Object getRefMap(final String fullPath, final String parentPath) {
    // $ref:相対出力ファイルパス のmapを作成
    final Map<String, String> refMap = new ConcurrentHashMap<>();
    final String curWrittenFileRelpath = fullPath.replace(parentPath, ".");
    refMap.put(KEY_REF, curWrittenFileRelpath);

    return refMap;
  }

  /**
   * 分割ファイルを削除します。
   *
   * @param targetDirPath 対象ディレクトリ
   * @param specId SpecificationID
   */
  protected static void deleteSplit(final String targetDirPath, final String specId) {
    final String specDirPath = splitDir(targetDirPath, specId);
    final String splitRootFilePath = FilePathUtils.filePath(specDirPath, FILENAME_SPLIT_ROOT);
    ValidationUtils.existFile(splitRootFilePath);

    if (!FileUtils.rmdirs(specDirPath))
      ValidationUtils.dirCantDelete(specDirPath);
  }
}
