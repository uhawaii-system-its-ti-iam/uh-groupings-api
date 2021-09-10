package edu.hawaii.its.api.util;

import org.junit.Test;
import edu.hawaii.its.api.type.SyncDestination;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonUtilTest {

    @Test
    public void basics() {
        SyncDestination sd0 = new SyncDestination("name", "description");
        String sdJson = JsonUtil.asJson(sd0);

        SyncDestination sd1 = JsonUtil.asObject(sdJson, SyncDestination.class);

        assertEquals(sd0.getName(), sd1.getName());
        assertEquals(sd0.getDescription(), sd1.getDescription());
        assertEquals(sd0.isSynced(), sd1.isSynced());
        assertEquals(sd0.isHidden(), sd1.isHidden());
        assertEquals(sd0.getTooltip(), sd1.getTooltip());
    }

    @Test
    public void problems() {
        String json = JsonUtil.asJson(null);
        assertEquals(json, "null");

        json = JsonUtil.asJson("{}");
        assertEquals(json, "\"{}\"");

        json = JsonUtil.asJson("mistake");
        assertEquals(json, "\"mistake\"");
    }

    @Test
    public void constructorIsPrivate() throws Exception {
        Constructor<JsonUtil> constructor = JsonUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance();
    }
}