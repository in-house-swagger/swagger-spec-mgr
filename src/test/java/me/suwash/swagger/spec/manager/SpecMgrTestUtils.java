package me.suwash.swagger.spec.manager;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.suwash.swagger.spec.manager.infra.config.SpecMgrContext;
import me.suwash.swagger.spec.manager.infra.error.CheckErrors;

@lombok.extern.slf4j.Slf4j
public class SpecMgrTestUtils {
  public static Map<String, Object> getTestPayload() {
    Map<String, Object> depth1Map = new HashMap<>();
    depth1Map.put("depth1_map.key1", LocalDate.now());
    depth1Map.put("depth1_map.key2", LocalDateTime.now());

    List<Object> depth1List = new ArrayList<>();
    depth1List.add(LocalDateTime.now().toString());
    depth1List.add(LocalDateTime.now().toString());
    depth1List.add(LocalDateTime.now().toString());

    Map<String, Object> payload = new HashMap<>();
    payload.put("depth1_map", depth1Map);
    payload.put("depth1_list", depth1List);

    return payload;
  }

  public static void assertCheckErrors(final SpecMgrContext context, final String[] expectStrings) {
    final CheckErrors errors = context.getErrors();
    assertCheckErrors(errors, expectStrings);
  }

  public static void assertCheckErrors(final CheckErrors errors, final String[] expectStrings) {
    final List<String> actualMsgList = parseErrorMessage(errors);
    log.debug("error message list: " + actualMsgList);

    assertErrorsContainsStrings(actualMsgList, expectStrings);
    assertThat(errors.size(), is(expectStrings.length));
  }

  private static List<String> parseErrorMessage(final CheckErrors errors) {
    // errorsから propertyKey + message の実績値リストを作成
    List<String> actualMsgList = new ArrayList<>();
    errors.forEach(error -> {
      final String checkMessage = error.getPropertyKey() + ": [" + error.getMessageId() + "]" + error.getMessage();
      actualMsgList.add(checkMessage);
    });
    return actualMsgList;
  }

  private static void assertErrorsContainsStrings(final List<String> actualMsgList,
      final String[] expectStrings) {
    // 期待値メッセージをループして、実績値リストに部分文字列が含まれているか、確認
    for (int idx = 0; idx < expectStrings.length; idx++) {
      String curExpectString = expectStrings[idx];
      if (!containsString(actualMsgList, curExpectString))
        fail("errors に " + curExpectString + " が含まれていません。actual:" + actualMsgList);
    }
  }

  private static boolean containsString(final List<String> actualMsgList, final String expectMsg) {
    for (int idx = 0; idx < actualMsgList.size(); idx++) {
      final String curActualMsg = actualMsgList.get(idx);
      if (curActualMsg.contains(expectMsg))
        return true;
    }
    return false;
  }

}
