package me.suwash.swagger.spec.manager.infra.config;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import me.suwash.swagger.spec.manager.infra.error.SpecMgrException;

public class CommitInfoTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {}

  @AfterClass
  public static void tearDownAfterClass() throws Exception {}

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public final void test() {
    try {
      new CommitInfo(null, null).canInit();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("check.notNull"));
    }
    try {
      new CommitInfo("user", null).canInit();
    } catch (SpecMgrException e) {
      assertThat(e.getMessageId(), is("check.notNull"));
    }

    final CommitInfo commitInfo = new CommitInfo("user", "user@domain.local", "commit message.");
    commitInfo.canInit();
    assertThat(commitInfo.getUser(), is("user"));
    assertThat(commitInfo.getEmail(), is("user@domain.local"));
    assertThat(commitInfo.getMessage(), is("commit message."));
  }

}
