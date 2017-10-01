package me.suwash.swagger.spec.manager.infra.util;

import org.apache.commons.lang3.StringUtils;

public final class FilePathUtils {
  private FilePathUtils() {}

  public static String filePath(final String dirPath, final String fileName) {
    return dirPath + "/" + fileName;
  }

  public static String dirPath(final String dirPath, final String dirName) {
    return filePath(dirPath, dirName);
  }

  public static String absolutePathToCurRel(final String absPath) {
    return absolutePathToRel(absPath, StringUtils.EMPTY);
  }

  public static String absolutePathToRel(final String absPath, final String baseDir) {
    return absPath.replaceFirst("^/", baseDir);
  }

  public static String relPathToAbsolute(final String relPath, final String baseDirPath) {
    return relPath.replaceFirst("^./", baseDirPath + "/");
  }

}
