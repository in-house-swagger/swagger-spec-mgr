package me.suwash.swagger.spec.manager.infra.util;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.util.FileUtils;

public final class ValidationUtils {

  private ValidationUtils() {}

  /**
   * 項目が nullではない ことを確認します。
   *
   * @param name 項目名
   * @param target 値
   */
  public static void mustNotNull(final String name, final Object target) {
    if (target == null)
      throw new SpecMgrException("check.notNull", array(name));
  }

  /**
   * 項目が null、空文字ではない ことを確認します。
   *
   * @param name 項目名
   * @param target 値
   */
  public static void mustNotEmpty(final String name, final String target) {
    if (StringUtils.isEmpty(target))
      throw new SpecMgrException("check.notNull", array(name));
  }

  /**
   * 想定外の呼び出しを通知します。
   *
   * @param target 呼び出し対象
   */
  public static void illegalArgs(final String target) {
    throw new SpecMgrException("illegalArgs", array(target));
  }

  /**
   * ファイルが存在する ことを確認します。
   *
   * @param filePath ファイルパス
   */
  public static void mustExistFile(final String filePath) {
    if (!new File(filePath).exists())
      throw new SpecMgrException("file.notExist", array(filePath));
  }

  /**
   * ファイルの読み込みエラーを通知します。
   *
   * @param filePath ファイルパス
   * @param e 発生した例外
   */
  public static void fileCantRead(final String filePath, final Exception e) {
    throw new SpecMgrException("file.cantRead", array(filePath, e.getMessage()), e);
  }

  /**
   * ファイルの書き出しエラーを通知します。
   *
   * @param filePath ファイルパス
   * @param e 発生した例外
   */
  public static void fileCantWrite(final String filePath, final Exception e) {
    throw new SpecMgrException("file.cantWrite", array(filePath, e.getMessage()), e);
  }

  /**
   * ファイルの削除エラーを通知します。
   *
   * @param filePath ファイルパス
   * @param e 発生した例外
   */
  public static void fileCantDelete(final String filePath, final Exception e) {
    throw new SpecMgrException("file.cantDelete", array(filePath, e.getMessage()), e);
  }

  /**
   * ディレクトリが存在する ことを確認します。
   *
   * @param dirPath ディレクトリパス
   */
  public static void mustExistDir(final String dirPath) {
    if (!new File(dirPath).exists())
      throw new SpecMgrException("dir.notExist", array(dirPath));
  }

  /**
   * ディレクトリが存在する ことを強制します（存在しない場合、作成します）。
   *
   * @param dirPath ディレクトリパス
   */
  public static void mustExistDirForce(String dirPath) {
    final File dir = new File(dirPath);
    if (!dir.exists())
      FileUtils.mkdirs(dirPath);
  }

  /**
   * ディレクトリが存在しない ことを確認します。
   *
   * @param dirPath ディレクトリパス
   */
  public static void mustNotExistDir(String dirPath) {
    if (new File(dirPath).exists())
      throw new SpecMgrException("dir.alreadyExist", array(dirPath));
  }

  /**
   * ディレクトリが作成できなかったエラーを通知します。
   *
   * @param dirPath ディレクトリパス
   */
  public static void dirCantCreate(final String dirPath) {
    throw new SpecMgrException("dir.cantCreate", array(dirPath));
  }

  /**
   * ディレクトリが削除できなかったエラーを通知します。
   *
   * @param dirPath ディレクトリパス
   */
  public static void dirCantDelete(final String dirPath) {
    throw new SpecMgrException("dir.cantDelete", array(dirPath));
  }

  /**
   * データの検索結果が存在する ことを確認します。
   *
   * @param dataName データ名
   * @param key データの検索キー
   * @param value データの検索値
   * @param finded 検索結果
   */
  public static void mustExistData(final String dataName, final String key, final String value,
      final Object finded) {
    if (finded == null)
      throw new SpecMgrException("data.notExist", array(dataName, key, value));
  }

  /**
   * データの検索結果が存在しない ことを確認します。
   *
   * @param dataName データ名
   * @param key データの検索キー
   * @param value データの検索値
   * @param finded 検索結果
   */
  public static void mustNotExistData(final String dataName, final String key, final String value,
      final Object finded) {
    if (finded != null)
      throw new SpecMgrException("data.alreadyExist", array(dataName, key, value));
  }

}
