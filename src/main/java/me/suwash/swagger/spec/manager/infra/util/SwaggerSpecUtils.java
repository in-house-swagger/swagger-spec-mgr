package me.suwash.swagger.spec.manager.infra.util;

import io.swagger.util.Yaml;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.suwash.swagger.spec.manager.infra.exception.SpecMgrException;
import me.suwash.util.FileUtils;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * Swagger Specification File操作ユーティリティ。
 *
 */
@lombok.extern.slf4j.Slf4j
public final class SwaggerSpecUtils {

    /** JsonRef参照キー。 */
    private static final String KEY_REF = "$ref";
    /** yamlファイル拡張子。 */
    private static final String FILE_EXT = ".yaml";

    /** 統合ファイル名。 */
    private static final String FILENAME_MERGED = "swagger" + FILE_EXT;
    /** 分割ルートファイル名。 */
    private static final String FILENAME_SPLIT_ROOT = FILENAME_MERGED;
    /** 分割ファイル名。 */
    private static final String FILENAME_SPLIT_MAP = "index" + FILE_EXT;

    /** swagger-coreのYaml Mapper */
    private static final ObjectMapper yamlMapper = Yaml.mapper();

    /** コンストラクタ非公開。 */
    private SwaggerSpecUtils() {}

    /**
     *
     * Specificationファイルを、Mapにparseします。
     *
     * @param inputDirPath 入力ディレクトリ
     * @param specId SpecificationID
     * @return parse結果
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parse(final String inputDirPath, final String specId) {
        // -----------------------------------------------------------------------------------------
        // 事前処理
        // -----------------------------------------------------------------------------------------
        if (StringUtils.isEmpty(inputDirPath)) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "inputDirPath"
            });
        }
        if (StringUtils.isEmpty(specId)) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "specId"
            });
        }

        final File inputFile = new File(inputDirPath + "/" + specId + "/" + FILENAME_MERGED);
        if (!inputFile.exists()) {
            return null;
        }

        // -----------------------------------------------------------------------------------------
        // 本処理
        // -----------------------------------------------------------------------------------------
        try {
            return yamlMapper.readValue(inputFile, Map.class);
        } catch (Exception e) {
            throw new SpecMgrException("SpecMgr.10002", new Object[] {
                inputFile, e.getMessage()
            }, e);
        }
    }

    /**
     *
     * 指定オブジェクトを、Yaml形式の文字列に変換します。
     *
     * FIXME ファイル先頭に分割ファイルの --- が出力されている。
     * YAMLGenerator.Feature.WRITE_DOC_START_MARKER を desableにすれば良さそうだが
     * SwaggerのUtilで作成された後のObjectMapperでは、YAMLGenerator.Featureを指定できない。。。
     *
     * @param value オブジェクト
     * @return Yaml形式の文字列
     */
    public static String writeString(final Object value) {
        // -----------------------------------------------------------------------------------------
        // 事前処理
        // -----------------------------------------------------------------------------------------
        if (value == null) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "value"
            });
        }

        // -----------------------------------------------------------------------------------------
        // 本処理
        // -----------------------------------------------------------------------------------------
        try {
            // 文字列として書き出し
            final String parsed = yamlMapper.writeValueAsString(value);
            // DOC_START_MARKERを削除
            final String DOC_START_MARKER = "---\n";
            return parsed.replaceFirst(DOC_START_MARKER, "");

        } catch (Exception e) {
            throw new SpecMgrException("SpecMgr.10003", new Object[] {
                e.getMessage()
            }, e);
        }
    }

    /**
     *
     * 指定オブジェクトをYamlファイルとして書き出します。
     *
     * @param value オブジェクト
     * @param outputFilePath 出力ファイルパス
     */
    private static void writeFile(final Object value, final String outputFilePath) {
        // -----------------------------------------------------------------------------------------
        // 事前処理
        // -----------------------------------------------------------------------------------------
        // parentDirを作成
        final File outputFile = new File(outputFilePath);
        final File parentDir = outputFile.getParentFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                throw new SpecMgrException("SpecMgr.02002", new Object[] {
                    parentDir
                });
            }
        }

        // 出力ファイルを上書き
        if (outputFile.exists()) {
            if (!outputFile.delete()) {
                throw new SpecMgrException("SpecMgr.03004", new Object[] {
                    outputFile
                });
            }
        }

        // -----------------------------------------------------------------------------------------
        // 本処理
        // -----------------------------------------------------------------------------------------
        try {
            // 文字列として書き出し
            final String parsed = writeString(value);
            // リストの全行を改行区切りで出力するので、文字列の最後の改行コードを削除
            final List<String> writeList = new ArrayList<>();
            writeList.add(parsed.replaceFirst("\n$", ""));
            // ファイル出力
            Files.write(
                Paths.get(parentDir.getAbsolutePath(), outputFile.getName()),
                writeList,
                Charset.forName("utf8"),
                StandardOpenOption.CREATE);

        } catch (Exception e) {
            throw new SpecMgrException("SpecMgr.03003", new Object[] {
                outputFile
            }, e);
        }
    }

    /**
     *
     * 指定されたObjectを、jsonRefで分割したファイルとして保存します。
     *
     * @param value 入力オブジェクト
     * @param outputDirPath 出力ディレクトリ
     * @param specId SpecificationID
     * @return 分割結果のルートファイルパス
     */
    public static String writeSplit(final Object value, final String outputDirPath, final String specId) {
        return writeSplit(value, outputDirPath, specId, null);
    }

    /**
     *
     * 指定されたObjectを、jsonRefで分割したファイルとして保存します。
     *
     * @param value 入力オブジェクト
     * @param outputDirPath 出力ディレクトリ
     * @param specId SpecificationID
     * @param ignoreRegexList 分割除外パスの正規表現リスト
     * @return 分割結果のルートファイルパス
     */
    public static String writeSplit(
        final Object value,
        final String outputDirPath,
        final String specId,
        final List<String> ignoreRegexList) {
        // -----------------------------------------------------------------------------------------
        // 事前処理
        // -----------------------------------------------------------------------------------------
        log.debug("writeSplit: " + outputDirPath + "/" + specId);

        if (value == null) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "value"
            });
        }
        if (StringUtils.isEmpty(outputDirPath)) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "outputDirPath"
            });
        }
        if (StringUtils.isEmpty(specId)) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "specId"
            });
        }
        List<Pattern> effectiveIgnorePatternList = new ArrayList<>();
        if (ignoreRegexList != null) {
            for (final String curIgnoreRegex : ignoreRegexList) {
                final String curEffectiveIgnoreRegex;
                // 出力ディレクトリを付与
                if (!curIgnoreRegex.contains(outputDirPath)) {
                    curEffectiveIgnoreRegex = outputDirPath + "/" + specId + curIgnoreRegex;
                } else {
                    curEffectiveIgnoreRegex = curIgnoreRegex;
                }

                Pattern curPattern = Pattern.compile(curEffectiveIgnoreRegex);
                effectiveIgnorePatternList.add(curPattern);

            }
        }

        if (log.isTraceEnabled()) {
            log.trace("- ignoreRegexList: ");
            if (ignoreRegexList == null) {
                log.trace("-- null");
            } else {
                for (final String curIgnoreRegex : ignoreRegexList) {
                    log.trace("-- " + curIgnoreRegex);
                }
            }
            log.trace("- effectiveIgnoreRegexList: ");
            if (effectiveIgnorePatternList.isEmpty()) {
                log.trace("-- null");
            } else {
                for (final Pattern curEffectiveIgnorePattern : effectiveIgnorePatternList) {
                    log.trace("-- " + curEffectiveIgnorePattern);
                }
            }
        }

        // -----------------------------------------------------------------------------------------
        // 主処理
        // -----------------------------------------------------------------------------------------
        final String outputRootDirPath = outputDirPath + "/" + specId;
        return recursiveSplit(value, outputRootDirPath, FILENAME_MERGED, effectiveIgnorePatternList);
    }

    /**
     *
     * 指定されたObjectを、jsonRefで再帰的に分割します。
     *
     * @param target 入力オブジェクト
     * @param outputDirPath 出力ディレクトリ
     * @param outputFileName 出力ファイル名
     * @param ignorePatternList 分割除外パスリスト
     * @return 出力ファイルパス
     */
    @SuppressWarnings("unchecked")
    private static String recursiveSplit(
        final Object target,
        final String outputDirPath,
        final String outputFileName,
        final List<Pattern> ignorePatternList
        ) {

        // 出力ファイルパス
        final String outputFilePath = outputDirPath + "/" + outputFileName;

        // targetの型を確認
        if (target instanceof Map) {
            // -------------------------------------------------------------------------------------
            // target = Map の場合
            // -------------------------------------------------------------------------------------
            Map<String, Object> targetMap = (Map<String, Object>) target;
            Map<String, Object> replacedMap = new LinkedHashMap<>();

            // keyをループして再帰分割
            for (final Map.Entry<String, Object> curTargetEntry : targetMap.entrySet()) {
                String curTargetKey = curTargetEntry.getKey();
                Object curTargetValue = curTargetEntry.getValue();
                // valueの型を確認
                if (curTargetValue instanceof Map || curTargetValue instanceof List) {
                    // --------------------------------------------------
                    // value = Map or List の場合
                    // --------------------------------------------------
                    final String curWriteDirPath = outputDirPath + "/" + curTargetKey.replaceFirst("^/", "");
                    if (isSplit(ignorePatternList, curWriteDirPath)) {
                        // valueで再帰呼び出し
                        final String curWrittenFilePath = recursiveSplit(curTargetValue, curWriteDirPath, FILENAME_SPLIT_MAP, ignorePatternList);
                        // 置換後mapに、jsonRefのentryを追加
                        replacedMap.put(curTargetKey, getRefMap(curWrittenFilePath, outputDirPath));
                    } else {
                        replacedMap.put(curTargetKey, curTargetValue);
                    }

                } else {
                    // --------------------------------------------------
                    // elem = その他の型 の場合
                    // --------------------------------------------------
                    // そのまま置換後mapに登録
                    replacedMap.put(curTargetKey, curTargetValue);
                }
            }

            // targetの書き出し
            writeFile(replacedMap, outputFilePath);

        } else if (target instanceof List) {
            // -------------------------------------------------------------------------------------
            // target = List の場合
            // -------------------------------------------------------------------------------------
            List<Object> targetList = (List<Object>) target;
            List<Object> replacedList = new ArrayList<>();

            // elemをループして再帰分割
            for (int idx = 0; idx < targetList.size(); idx++) {
                final Object curTargetElem = targetList.get(idx);

                // elemの型を確認
                if (curTargetElem instanceof Map) {
                    // --------------------------------------------------
                    // elem = Map の場合
                    // --------------------------------------------------
                    final String curWriteFileName = (idx + 1) + FILE_EXT;
                    if (isSplit(ignorePatternList, outputDirPath)) {
                        // elemで再帰呼び出し ※ファイル名は、連番を利用
                        final String curWrittenFilePath = recursiveSplit(curTargetElem, outputDirPath, curWriteFileName, ignorePatternList);

                        // 置換後listに、jsonRefのentryを追加
                        replacedList.add(getRefMap(curWrittenFilePath, outputDirPath));
                    } else {
                        replacedList.add(curTargetElem);
                    }

                } else {
                    // --------------------------------------------------
                    // elem = その他の型 の場合
                    // --------------------------------------------------
                    // そのまま置換後listに登録
                    replacedList.add(curTargetElem);
                }
            }

            // targetの書き出し
            writeFile(replacedList, outputFilePath);

        } else {
            // -------------------------------------------------------------------------------------
            // target = その他の型 の場合
            // -------------------------------------------------------------------------------------
            // エラー
            throw new SpecMgrException("SpecMgr.00003", new Object[] {
                target.getClass().getName()
            });

        }

        return outputFilePath;
    }

    /**
     *
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
            if (matcher.matches()) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * 指定ディレクトリ、SpecificationID配下の分割ファイルを、統合したファイルを出力します。
     *
     * @param inputDirPath 入力ディレクトリ
     * @param outputDirPath 出力ディレクトリ
     * @param specId SpecificationID
     * @return 統合結果のファイルパス
     */
    public static String writeMerged(final String inputDirPath, final String outputDirPath, final String specId) {
        // -----------------------------------------------------------------------------------------
        // 事前処理
        // -----------------------------------------------------------------------------------------
        log.debug("writeMerged: " + outputDirPath + "/" + specId);

        if (StringUtils.isEmpty(inputDirPath)) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "inputDirPath"
            });
        }
        if (StringUtils.isEmpty(outputDirPath)) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "outputDirPath"
            });
        }
        if (StringUtils.isEmpty(specId)) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "specId"
            });
        }

        final File inputDir = new File(inputDirPath);
        if (!inputDir.exists()) {
            throw new SpecMgrException("SpecMgr.02001", new Object[] {
                inputDir
            });
        }

        final String inputRootFilePath = inputDirPath + "/" + specId + "/" + FILENAME_SPLIT_ROOT;
        final File inputRootFile = new File(inputRootFilePath);
        if (!inputRootFile.exists()) {
            throw new SpecMgrException("SpecMgr.03001", new Object[] {
                inputRootFile
            });
        }

        // -----------------------------------------------------------------------------------------
        // 本処理
        // -----------------------------------------------------------------------------------------
        // 分割ファイルをmerge
        final Object merged = recursiveMerge(inputRootFilePath);

        // merge結果を書き出し
        final String outputFilePath = outputDirPath + "/" + specId + "/" + FILENAME_MERGED;
        writeFile(merged, outputFilePath);
        return outputFilePath;
    }

    /**
     *
     * 指定分割ファイルを、再帰的に統合します。
     *
     * @param splitFilePath 分割ファイルパス
     * @return 統合結果
     */
    @SuppressWarnings("unchecked")
    private static Object recursiveMerge(final String splitFilePath) {
        final File splitFile = new File(splitFilePath);
        final String splitDirPath = splitFile.getParent();

        // -----------------------------------------------------------------------------------------
        // splitファイルをparse
        // -----------------------------------------------------------------------------------------
        final Object parsed;
        try {
            parsed = yamlMapper.readValue(splitFile, Object.class);
        } catch (Exception e) {
            throw new SpecMgrException("SpecMgr.10002", new Object[] {
                splitFilePath, e.getMessage()
            }, e);
        }

        // parsedの型を確認
        if (parsed instanceof Map) {
            // -------------------------------------------------------------------------------------
            // parsed = Map の場合
            // -------------------------------------------------------------------------------------
            final Map<String, Object> parsedMap = (Map<String, Object>) parsed;
            final Map<String, Object> replacedMap = new LinkedHashMap<>();

            // entryをループ
            for (final Map.Entry<String, Object> curParsedEntry : parsedMap.entrySet()) {
                final String curParsedKey = curParsedEntry.getKey();
                final Object curParsedValue = curParsedEntry.getValue();

                // valueの型を確認
                if (curParsedValue instanceof Map) {
                    // --------------------------------------------------
                    // value = Map の場合
                    // --------------------------------------------------
                    // entryを確認して再帰呼び出しを判断
                    boolean hasRef = false;
                    final Map<String, Object> curParsedValueMap = (Map<String, Object>) curParsedValue;
                    for (final Map.Entry<String, Object> curParsedValueMapEntry : curParsedValueMap.entrySet()) {
                        final String curParsedValueMapKey = curParsedValueMapEntry.getKey();
                        final Object curParsedValueMapValue = curParsedValueMapEntry.getValue();
                        // keyを確認
                        if (KEY_REF.equals(curParsedValueMapKey)) {
                            // $refの場合、再帰呼び出し
                            final String curSplitFilePath = curParsedValueMapValue.toString().replaceFirst("^./", splitDirPath + "/");
                            final Object curResult = recursiveMerge(curSplitFilePath);
                            // 結果を置換後Mapにput ※一つ上の階層に展開結果を反映しています。
                            replacedMap.put(curParsedKey, curResult);
                            hasRef = true;
                        }
                    }
                    if (!hasRef) {
                        // $ref以外の場合、そのまま置換後Mapに設定
                        replacedMap.put(curParsedKey, curParsedValue);
                    }

                } else {
                    // --------------------------------------------------
                    // value = その他の型 の場合
                    // --------------------------------------------------
                    // そのまま置換後Mapに設定
                    replacedMap.put(curParsedKey, curParsedValue);
                }
            }
            return replacedMap;

        } else if (parsed instanceof List) {
            // -------------------------------------------------------------------------------------
            // parsed = List の場合
            // -------------------------------------------------------------------------------------
            final List<Object> parsedList = (List<Object>) parsed;
            final List<Object> replacedList = new ArrayList<>();

            // elemをループ
            for (final Object curParsedElem : parsedList) {
                if (curParsedElem instanceof Map) {
                    // --------------------------------------------------
                    // elem = Map の場合
                    // --------------------------------------------------
                    // entryを確認して再帰呼び出しを判断
                    final Map<String, Object> curParsedElemMap = (Map<String, Object>) curParsedElem;
                    for (final Map.Entry<String, Object> curParsedElemMapEntry : curParsedElemMap.entrySet()) {
                        final String curParsedElemMapKey = curParsedElemMapEntry.getKey();
                        final Object curParsedElemMapValue = curParsedElemMapEntry.getValue();
                        // keyを確認
                        if (KEY_REF.equals(curParsedElemMapKey)) {
                            // $refの場合、再帰呼び出し
                            final String curSplitFilePath = curParsedElemMapValue.toString().replaceFirst("^./", splitDirPath + "/");
                            if (curSplitFilePath.equals(splitFilePath)) {
                                throw new SpecMgrException("SpecMgr.10004", new Object[] {
                                    splitFilePath, curParsedElemMapValue
                                });
                            }
                            final Object curResult = recursiveMerge(curSplitFilePath);
                            // 結果を置換後Listにadd
                            replacedList.add(curResult);

                        } else {
                            // その他の場合、エラー
                            throw new SpecMgrException("SpecMgr.10001");
                        }
                    }

                } else {
                    // --------------------------------------------------
                    // elem = その他の型 の場合
                    // --------------------------------------------------
                    // そのまま置換後Listにadd
                    replacedList.add(curParsedElem);
                }
            }
            return replacedList;

        } else {
            // -------------------------------------------------------------------------------------
            // parsed = その他の型 の場合、そのまま返却
            // -------------------------------------------------------------------------------------
            return parsed;
        }
    }

    /**
     *
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
     *
     * 統合ファイルを削除します。
     *
     * @param targetDirPath 対象ディレクトリ
     * @param specId SpecificationID
     */
    public static void deleteMerged(final String targetDirPath, final String specId) {
        // -----------------------------------------------------------------------------------------
        // 事前処理
        // -----------------------------------------------------------------------------------------
        log.debug("deleteMerged: " + targetDirPath + "/" + specId);

        if (StringUtils.isEmpty(targetDirPath)) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "targetDirPath"
            });
        }
        if (StringUtils.isEmpty(specId)) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "specId"
            });
        }

        final String specDirPath = targetDirPath + "/" + specId;
        final File targetFile = new File(specDirPath + "/" + FILENAME_MERGED);
        if (!targetFile.exists()) {
            throw new SpecMgrException("SpecMgr.03001", new Object[] {
                targetFile
            });
        }

        // -----------------------------------------------------------------------------------------
        // 本処理
        // -----------------------------------------------------------------------------------------
        if (!FileUtils.rmdirs(specDirPath)) {
            throw new SpecMgrException("SpecMgr.0203", new Object[] {
                specDirPath
            });
        }
    }

    /**
     *
     * 分割ファイルを削除します。
     *
     * @param targetDirPath 対象ディレクトリ
     * @param specId SpecificationID
     */
    public static void deleteSplit(final String targetDirPath, final String specId) {
        // -----------------------------------------------------------------------------------------
        // 事前処理
        // -----------------------------------------------------------------------------------------
        log.debug("deleteSplit: " + targetDirPath + "/" + specId);

        if (StringUtils.isEmpty(targetDirPath)) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "targetDirPath"
            });
        }
        if (StringUtils.isEmpty(specId)) {
            throw new SpecMgrException("SpecMgr.04001", new Object[] {
                "Args", "specId"
            });
        }

        final String specDirPath = targetDirPath + "/" + specId;
        final File targetFile = new File(specDirPath + "/" + FILENAME_SPLIT_ROOT);
        if (!targetFile.exists()) {
            throw new SpecMgrException("SpecMgr.03001", new Object[] {
                targetFile
            });
        }

        // -----------------------------------------------------------------------------------------
        // 本処理
        // -----------------------------------------------------------------------------------------
        if (!FileUtils.rmdirs(specDirPath)) {
            throw new SpecMgrException("SpecMgr.0203", new Object[] {
                specDirPath
            });
        }
    }
}
