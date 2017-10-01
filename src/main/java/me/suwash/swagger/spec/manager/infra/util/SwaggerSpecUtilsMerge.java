package me.suwash.swagger.spec.manager.infra.util;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.FILENAME_MERGED;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.KEY_REF;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.mergedDir;
import static me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsCommon.parse;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.util.FileUtils;

public final class SwaggerSpecUtilsMerge {

  private SwaggerSpecUtilsMerge() {}

  /**
   * 指定分割ファイルを、再帰的に統合します。
   *
   * @param splitFilePath 分割ファイルパス
   * @return 統合結果
   */
  @SuppressWarnings("unchecked")
  protected static Object recursiveMerge(final String splitFilePath) {
    final File splitFile = new File(splitFilePath);
    final String splitDirPath = splitFile.getParent();

    // splitファイルをparse
    final Object parsed = parse(splitFile, Object.class);

    // parsed = Map の場合
    if (parsed instanceof Map) {
      return recursiveMergeOnMap((Map<String, Object>) parsed, splitFilePath, splitDirPath);
    }

    // parsed = List の場合
    if (parsed instanceof List) {
      return recursiveMergeOnList((List<Object>) parsed, splitFilePath, splitDirPath);
    }

    // parsed = その他の型 の場合、そのまま返却
    return parsed;
  }

  private static Object recursiveMergeOnMap(final Map<String, Object> parsedMap,
      final String splitFilePath, final String splitDirPath) {

    final Map<String, Object> replacedMap = new LinkedHashMap<>();
    for (final Map.Entry<String, Object> curParsedEntry : parsedMap.entrySet())
      recursiveMergeOnMapEntry(curParsedEntry, splitDirPath, splitFilePath, replacedMap);

    return replacedMap;
  }

  private static void recursiveMergeOnMapEntry(final Map.Entry<String, Object> curParsedEntry,
      final String splitDirPath, final String splitFilePath,
      final Map<String, Object> replacedMap) {
    final String curParsedKey = curParsedEntry.getKey();
    final Object curParsedValue = curParsedEntry.getValue();

    // valueの型を確認
    if (curParsedValue instanceof Map) {
      // value = Map の場合、再帰呼び出し
      recursiveMergeOnMapHasModel(splitFilePath, splitDirPath, replacedMap, curParsedKey,
          curParsedValue);
      return;
    }
    // value = その他の型 の場合、そのまま置換後Mapに設定
    replacedMap.put(curParsedKey, curParsedValue);
  }

  @SuppressWarnings("unchecked")
  private static void recursiveMergeOnMapHasModel(final String splitFilePath,
      final String splitDirPath, final Map<String, Object> replacedMap, final String curParsedKey,
      final Object curParsedValue) {

    // entryを確認して再帰呼び出しを判断
    boolean hasRef = false;
    final Map<String, Object> curParsedValueMap = (Map<String, Object>) curParsedValue;
    for (final Map.Entry<String, Object> curParsedValueMapEntry : curParsedValueMap.entrySet())
      hasRef = recursiveMergeOnMapHasModelEntry(curParsedValueMapEntry, curParsedKey, splitDirPath,
          splitFilePath, hasRef, replacedMap);

    // $ref以外の場合、そのまま置換後Mapに設定
    if (!hasRef)
      replacedMap.put(curParsedKey, curParsedValue);
  }

  private static boolean recursiveMergeOnMapHasModelEntry(
      final Map.Entry<String, Object> curParsedValueMapEntry, final String curParsedKey,
      final String splitDirPath, final String splitFilePath, final boolean hasRefBefore,
      final Map<String, Object> replacedMap) {
    final String curParsedValueMapKey = curParsedValueMapEntry.getKey();
    final Object curParsedValueMapValue = curParsedValueMapEntry.getValue();

    boolean hasRef = hasRefBefore;
    if (KEY_REF.equals(curParsedValueMapKey)) {
      // $refの場合、再帰呼び出し
      final String curSplitFilePath =
          FilePathUtils.relPathToAbsolute(curParsedValueMapValue.toString(), splitDirPath);
      canContnueMerge(splitFilePath, curSplitFilePath, curParsedValueMapValue);
      final Object curResult = recursiveMerge(curSplitFilePath);

      // 結果を置換後Mapにput ※一つ上の階層に展開結果を反映しています。
      replacedMap.put(curParsedKey, curResult);
      hasRef = true;
    }
    return hasRef;
  }

  private static void canContnueMerge(final String splitFilePath, final String curSplitFilePath,
      final Object curParsedValueMapValue) {
    if (curSplitFilePath.equals(splitFilePath))
      throw new SpecMgrException("spec.refLoop", array(splitFilePath, curParsedValueMapValue));
  }

  private static Object recursiveMergeOnList(final List<Object> parsedList,
      final String splitFilePath, final String splitDirPath) {

    final List<Object> replacedList = new ArrayList<>();
    for (final Object curParsedElem : parsedList)
      recursiveMergeOnListElem(curParsedElem, splitFilePath, splitDirPath, replacedList);

    return replacedList;
  }

  private static void recursiveMergeOnListElem(final Object curParsedElem,
      final String splitFilePath, final String splitDirPath, final List<Object> replacedList) {
    if (curParsedElem instanceof Map) {
      // elem = Map の場合
      recursiveMergeOnListHasModel(splitFilePath, splitDirPath, replacedList, curParsedElem);
      return;
    }
    // elem = その他の型 の場合、そのまま置換後Listにadd
    replacedList.add(curParsedElem);
  }

  @SuppressWarnings("unchecked")
  private static void recursiveMergeOnListHasModel(final String splitFilePath,
      final String splitDirPath, final List<Object> replacedList, final Object curParsedElem) {

    // entryを確認して再帰呼び出しを判断
    final Map<String, Object> curParsedElemMap = (Map<String, Object>) curParsedElem;
    for (final Map.Entry<String, Object> curParsedElemMapEntry : curParsedElemMap.entrySet())
      recursiveMergeOnListHasModelEntry(curParsedElemMapEntry, replacedList, splitFilePath,
          splitDirPath);
  }

  private static void recursiveMergeOnListHasModelEntry(
      final Map.Entry<String, Object> curParsedElemMapEntry, final List<Object> replacedList,
      final String splitFilePath, final String splitDirPath) {
    final String curParsedElemMapKey = curParsedElemMapEntry.getKey();
    final Object curParsedElemMapValue = curParsedElemMapEntry.getValue();
    // keyを確認
    if (KEY_REF.equals(curParsedElemMapKey)) {
      // $refの場合、再帰呼び出し
      final String curSplitFilePath =
          FilePathUtils.relPathToAbsolute(curParsedElemMapValue.toString(), splitDirPath);
      canContnueMerge(splitFilePath, curSplitFilePath, curParsedElemMapValue);
      final Object curResult = recursiveMerge(curSplitFilePath);

      // 結果を置換後Listにadd
      replacedList.add(curResult);
      return;
    }

    // その他の場合、エラー
    throw new SpecMgrException("spec.unsupportedStructure");
  }

  /**
   * 統合ファイルを削除します。
   *
   * @param inputDirPath 対象ディレクトリ
   * @param specId SpecificationID
   */
  protected static void deleteMerged(final String inputDirPath, final String specId) {
    final String specDirPath = mergedDir(inputDirPath, specId);
    final String mergedFilePath = FilePathUtils.filePath(specDirPath, FILENAME_MERGED);
    ValidationUtils.existFile(mergedFilePath);

    if (!FileUtils.rmdirs(specDirPath))
      ValidationUtils.dirCantDelete(specDirPath);
  }

}
