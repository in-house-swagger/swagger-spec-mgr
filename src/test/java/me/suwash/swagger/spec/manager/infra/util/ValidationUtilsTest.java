package me.suwash.swagger.spec.manager.infra.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;

public class ValidationUtilsTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public final void test() throws IOException {
    String name = "name";
    Object objNull = null;
    Object objNotNull = new Object();
    String strEmpty = "";
    String strNotEmpty = "not-empty";
    String fileNotExist = "/path/to/NotExist";
    String fileExist = "/tmp/test.txt";
    String dirNotExist = "/path/to/NotExist";
    String dirExist = "/tmp";

    new File(fileExist).createNewFile();

    ValidationUtils.mustNotNull(name, objNotNull);
    try {
      ValidationUtils.mustNotNull(name, objNull);
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("check.notNull"));
    }

    ValidationUtils.mustNotEmpty(name, strNotEmpty);
    try {
      ValidationUtils.mustNotEmpty(name, strEmpty);
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("check.notNull"));
    }

    try {
      ValidationUtils.illegalArgs(name);
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("illegalArgs"));
    }

    ValidationUtils.mustExistFile(fileExist);
    try {
      ValidationUtils.mustExistFile(fileNotExist);
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("file.notExist"));
    }

    try {
      ValidationUtils.fileCantRead(fileNotExist, new RuntimeException("dummy"));
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("file.cantRead"));
    }

    try {
      ValidationUtils.fileCantWrite(fileNotExist, new RuntimeException("dummy"));
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("file.cantWrite"));
    }

    try {
      ValidationUtils.fileCantDelete(fileNotExist, new RuntimeException("dummy"));
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("file.cantDelete"));
    }

    ValidationUtils.mustExistDir(dirExist);
    try {
      ValidationUtils.mustExistDir(dirNotExist);
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("dir.notExist"));
    }

    ValidationUtils.mustExistDirForce("/tmp/force_create");

    ValidationUtils.mustNotExistDir(dirNotExist);
    try {
      ValidationUtils.mustNotExistDir(dirExist);
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("dir.alreadyExist"));
    }

    try {
      ValidationUtils.dirCantCreate(dirExist);
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("dir.cantCreate"));
    }

    try {
      ValidationUtils.dirCantDelete(dirExist);
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("dir.cantDelete"));
    }

    ValidationUtils.mustExistData(name, "key", "value", objNotNull);
    try {
      ValidationUtils.mustExistData(name, "key", "value", objNull);
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("data.notExist"));
    }
  }

}
