package edu.hawaii.its.api.wrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValue;
import edu.internet2.middleware.grouperClient.ws.beans.WsAttributeAssignValueResult;

public class AttributeAssignValueResultTest {

    @Test
    public void constructorTest() {
        AttributeAssignValueResult result = new AttributeAssignValueResult();
        assertNotNull(result.getValue());
    }

    @Test
    public void constructorNullTest() {
        AttributeAssignValueResult result = new AttributeAssignValueResult(null);
        assertFalse(result.isValueChanged());
        assertFalse(result.isValueRemoved());
        assertEquals("", result.getValue());
    }

    @Test
    public void constructorEmptyTest() {
        WsAttributeAssignValueResult wsResult = new WsAttributeAssignValueResult();
        AttributeAssignValueResult result = new AttributeAssignValueResult(wsResult);
        assertFalse(result.isValueChanged());
        assertFalse(result.isValueRemoved());
        assertEquals("", result.getValue());
    }

    @Test
    public void isValueChangedTrueTest() {
        WsAttributeAssignValueResult wsResult = new WsAttributeAssignValueResult();
        wsResult.setChanged("T");
        AttributeAssignValueResult result = new AttributeAssignValueResult(wsResult);
        assertTrue(result.isValueChanged());
    }

    @Test
    public void isValueChangedFalseTest() {
        WsAttributeAssignValueResult wsResult = new WsAttributeAssignValueResult();
        wsResult.setChanged("F");
        AttributeAssignValueResult result = new AttributeAssignValueResult(wsResult);
        assertFalse(result.isValueChanged());
    }

    @Test
    public void isValueRemovedTrueTest() {
        WsAttributeAssignValueResult wsResult = new WsAttributeAssignValueResult();
        wsResult.setDeleted("T");
        AttributeAssignValueResult result = new AttributeAssignValueResult(wsResult);
        assertTrue(result.isValueRemoved());
    }

    @Test
    public void isValueRemovedFalseTest() {
        WsAttributeAssignValueResult wsResult = new WsAttributeAssignValueResult();
        wsResult.setDeleted("F");
        AttributeAssignValueResult result = new AttributeAssignValueResult(wsResult);
        assertFalse(result.isValueRemoved());
    }

    @Test
    public void getValueTest() {
        WsAttributeAssignValue wsValue = new WsAttributeAssignValue();
        wsValue.setValueSystem("value");
        WsAttributeAssignValueResult wsResult = new WsAttributeAssignValueResult();
        wsResult.setWsAttributeAssignValue(wsValue);
        AttributeAssignValueResult result = new AttributeAssignValueResult(wsResult);
        assertEquals("value", result.getValue());
    }

    @Test
    public void getValueNullTest() {
        WsAttributeAssignValue wsValue = new WsAttributeAssignValue();
        wsValue.setValueSystem(null);
        WsAttributeAssignValueResult wsResult = new WsAttributeAssignValueResult();
        wsResult.setWsAttributeAssignValue(wsValue);
        AttributeAssignValueResult result = new AttributeAssignValueResult(wsResult);
        assertEquals("", result.getValue());
    }

}
