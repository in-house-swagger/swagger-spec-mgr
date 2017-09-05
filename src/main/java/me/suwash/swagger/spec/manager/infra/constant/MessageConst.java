package me.suwash.swagger.spec.manager.infra.constant;

import me.suwash.util.constant.UtilMessageConst;

public interface MessageConst extends UtilMessageConst {
    // common
    /** メッセージコード：{0} で想定外のエラーが発生しました。原因:{1} */
    String HANDLE_ERROR = "SpecMgr.00001";
    /** メッセージコード：この機能は利用できません。 */
    String UNSUPPORTED = "SpecMgr.00002";
    /** メッセージコード：対応していない呼び出し方法です。対象：{0} */
    String ILLEGAL_ARGS = "SpecMgr.00003";

    // data
    /** メッセージコード：{0} に {1} が {2} のデータは、すでに登録されています。 */
    String DATA_ALREADY_EXIST = "SpecMgr.01001";
    /** メッセージコード：{0} に {1} が {2} のデータは、存在しません。 */
    String DATA_NOT_EXIST = "SpecMgr.01002";
    /** メッセージコード：{0} の {1} が {2} のデータは、すでに更新されています。 */
    String DATA_ALREADY_UPDATED = "SpecMgr.01003";
    /** メッセージコード：{0} の {1} が {2} のデータは、更新されていません。 */
    String DATA_NOT_UPDATED = "SpecMgr.01004";

    // dir
    /** メッセージコード：ディレクトリが存在しません。対象:{0} */
    String DIR_NOT_EXIST = "SpecMgr.02001";
    /** メッセージコード：ディレクトリを作成できません。対象:{0} */
    String DIR_CANT_CREATE = "SpecMgr.02002";
    /** メッセージコード：ディレクトリを削除できません。対象:{0} */
    String DIR_CANT_DELETE = "SpecMgr.02003";

    // file
    /** メッセージコード：ファイルが存在しません。対象:{0} */
    String FILE_NOT_EXIST = "SpecMgr.03001";
    /** メッセージコード：ファイルを読込みできません。対象:{0}, 原因:{1} */
    String FILE_CANT_READ = "SpecMgr.03002";
    /** メッセージコード：ファイルを書出しできません。対象:{0}, 原因:{1} */
    String FILE_CANT_WRITE = "SpecMgr.03003";
    /** メッセージコード：ファイルを削除できません。対象:{0}, 原因:{1} */
    String FILE_CANT_DELETE = "SpecMgr.03004";

    // SwaggerSpecUtils
    /** メッセージコード：listの要素で jsonRef と その他の型の混在には対応していません。 */
    String MERGE_UNSUPPORTED_LIST = "SpecMgr.10001";
    /** メッセージコード：ファイルをパースできません。対象:{0}, 原因:{1} */
    String ERROR_FILE_TO_OBJECT = "SpecMgr.10002";
    /** メッセージコード：オブジェクトを文字列に変換できません。原因:{0} */
    String ERROR_OBJECT_TO_STRING = "SpecMgr.10003";
    /** メッセージコード：jsonRefの参照先がループしています。ファイル:{0}, 参照先:{1} */
    String MERGE_REFERENCE_LOOP = "SpecMgr.10004";
}
