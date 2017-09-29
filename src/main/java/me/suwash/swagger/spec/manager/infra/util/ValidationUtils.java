package me.suwash.swagger.spec.manager.infra.util;

import static me.suwash.swagger.spec.manager.infra.error.SpecMgrException.array;

import java.io.File;

import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;
import me.suwash.util.FileUtils;

import org.apache.commons.lang3.StringUtils;

public final class ValidationUtils {

    private ValidationUtils() {}

    public static void notNull(final String name, final Object target) {
        if (target == null) throw new SpecMgrException("check.notNull", array(name));
    }

    public static void notEmpty(final String name, final String target) {
        if (StringUtils.isEmpty(target)) throw new SpecMgrException("check.notNull", array(name));
    }

    public static void illegalArgs(final String target) {
        throw new SpecMgrException("illegalArgs", array(target));
    }

    public static void existFile(final String filePath) {
        if (!new File(filePath).exists()) throw new SpecMgrException("file.notExist", array(filePath));
    }

    public static void fileCantRead(final String filePath, final Exception e) {
        throw new SpecMgrException("file.cantRead", array(filePath, e.getMessage()), e);
    }

    public static void fileCantWrite(final String filePath, final Exception e) {
        throw new SpecMgrException("file.cantWrite", array(filePath, e.getMessage()), e);
    }

    public static void fileCantDelete(final String filePath, final Exception e) {
        throw new SpecMgrException("file.cantDelete", array(filePath, e.getMessage()), e);
    }

    public static void existDir(final String dirPath) {
        if (!new File(dirPath).exists()) throw new SpecMgrException("dir.notExist", array(dirPath));
    }

    public static void existDirForce(String dirPath) {
        final File dir = new File(dirPath);
        if (!dir.exists()) FileUtils.mkdirs(dirPath);
    }

    public static void notExistDir(String dirPath) {
        if (new File(dirPath).exists()) throw new SpecMgrException("dir.alreadyExist", array(dirPath));
    }

    public static void dirCantCreate(final String dirPath) {
        throw new SpecMgrException("dir.cantCreate", array(dirPath));
    }

    public static void dirCantDelete(final String dirPath) {
        throw new SpecMgrException("dir.cantDelete", array(dirPath));
    }

    public static void existData(String dataName, String key, String value, Object finded) {
        if (finded == null) throw new SpecMgrException("data.notExist", array(dataName, key, value));
    }

}
