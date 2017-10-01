package me.suwash.swagger.spec.manager.sv.da;

public interface GitRepoRepository {

  /**
   * gitリポジトリを初期化します。
   * <ul>
   * <li>remoteが設定されている場合、git clone</li>
   * <li>remoteが設定されていない場合、git init</li>
   * </ul>
   */
  void init();

  /**
   * gitリポジトリにremoteでの変更を取り込みます。
   * <ul>
   * <li>remoteが設定されていない場合、何も実施しません。</li>
   * </ul>
   */
  void pull();

  /**
   * gitリポジトリに変更を登録します。
   * <ul>
   * <li>remoteが設定されている場合、git commit + push</li>
   * <li>remoteが設定されていない場合、git commit</li>
   * </ul>
   */
  void push();

}
