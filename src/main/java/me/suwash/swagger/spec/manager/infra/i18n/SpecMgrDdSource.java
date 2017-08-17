package me.suwash.swagger.spec.manager.infra.i18n;

import me.suwash.util.i18n.DdSource;

/**
 * データディクショナリ用プロパティファイルの定義保持クラス。
 */
public class SpecMgrDdSource extends DdSource {
    private static SpecMgrDdSource instance = new SpecMgrDdSource();

    /**
     * Singletonパターンでオブジェクトを返します。
     *
     * @return DataDictionaryオブジェクト
     */
    public static SpecMgrDdSource getInstance() {
        return instance;
    }

    @Override
    protected DdSource getParent() {
        return DdSource.getInstance();
    }

}
