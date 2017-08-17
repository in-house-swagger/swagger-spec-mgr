package me.suwash.swagger.spec.manager.sv.domain;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.suwash.swagger.spec.manager.infra.exception.SpecMgrException;
import me.suwash.swagger.spec.manager.infra.util.SwaggerSpecUtilsTest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

@lombok.extern.slf4j.Slf4j
public class SpecEntityTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {}

    @AfterClass
    public static void tearDownAfterClass() throws Exception {}

    @Before
    public void setUp() throws Exception {}

    @After
    public void tearDown() throws Exception {}

    private static final String SPEC_ID = SpecEntityTest.class.getSimpleName();

    @Test
    public final void test() {
        Map<String, Object> map = new HashMap<String, Object>();
        // -----------------------------------------------------------------------------------------
        // Find
        // -----------------------------------------------------------------------------------------
        // nullチェック
        SpecEntity entity = new SpecEntity();
        try {
            entity.findById();
        } catch (SpecMgrException e) {
            assertEquals("SpecMgr.04001", e.getMessageId());
            log.debug(e.getMessage());
        }

        // 0件
        entity = new SpecEntity();
        entity.setId(SPEC_ID);
        assertNull(entity.findById());

        // -----------------------------------------------------------------------------------------
        // Add
        // -----------------------------------------------------------------------------------------
        // nullチェック
        entity = new SpecEntity();
        try {
            entity.add();
        } catch (SpecMgrException e) {
            assertEquals("SpecMgr.04001", e.getMessageId());
            log.debug(e.getMessage());
        }

        // nullチェック
        entity = new SpecEntity();
        entity.setId("dummy");
        try {
            entity.add();
        } catch (SpecMgrException e) {
            assertEquals("SpecMgr.04001", e.getMessageId());
            log.debug(e.getMessage());
        }

        // 追加
        entity = new SpecEntity();
        entity.setId(SPEC_ID);
        entity.setPayload(map);
        SpecEntity result = entity.add();
        assertNotNull(result);
        log.debug(result.toString());

        // -----------------------------------------------------------------------------------------
        // Find - 1件
        // -----------------------------------------------------------------------------------------
        entity = new SpecEntity();
        entity.setId(SPEC_ID);
        assertEquals(result, entity.findById());

        // -----------------------------------------------------------------------------------------
        // Update
        // -----------------------------------------------------------------------------------------
        // nullチェック
        entity = new SpecEntity();
        try {
            entity.update();
        } catch (SpecMgrException e) {
            assertEquals("SpecMgr.04001", e.getMessageId());
            log.debug(e.getMessage());
        }

        // nullチェック
        entity = new SpecEntity();
        entity.setId("dummy");
        try {
            entity.update();
        } catch (SpecMgrException e) {
            assertEquals("SpecMgr.04001", e.getMessageId());
            log.debug(e.getMessage());
        }

        // 更新
        entity = new SpecEntity();
        entity.setId(SPEC_ID);
        SpecEntity finded = entity.findById();

        map.put("key-update", "value-update");
        entity = new SpecEntity();
        entity.setId(SPEC_ID);
        entity.setPayload(map);
        result = entity.update();
        assertNotEquals(finded, result);
        log.debug(result.toString());

        // -----------------------------------------------------------------------------------------
        // Delete
        // -----------------------------------------------------------------------------------------
        // nullチェック
        entity = new SpecEntity();
        try {
            entity.delete();
        } catch (SpecMgrException e) {
            assertEquals("SpecMgr.04001", e.getMessageId());
            log.debug(e.getMessage());
        }

        // 削除
        entity = new SpecEntity();
        entity.setId(SPEC_ID);
        entity.delete();

        // 0件
        entity = new SpecEntity();
        entity.setId(SPEC_ID);
        assertNull(entity.findById());
     }

}
