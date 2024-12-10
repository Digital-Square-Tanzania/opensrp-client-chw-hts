package org.smartregister.chw.hts.activity;

import android.content.Intent;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.reflect.Whitebox;

public class BaseSkeletonRegisterActivityTest {
    @Mock
    public Intent data;
    @Mock
    private BaseSkeletonRegisterActivity baseTestRegisterActivity = new BaseSkeletonRegisterActivity();

    @Test
    public void assertNotNull() {
        Assert.assertNotNull(baseTestRegisterActivity);
    }

    @Test
    public void testFormConfig() {
        Assert.assertNull(baseTestRegisterActivity.getFormConfig());
    }

    @Test
    public void checkIdentifier() {
        Assert.assertNotNull(baseTestRegisterActivity.getViewIdentifiers());
    }

    @Test(expected = Exception.class)
    public void onActivityResult() throws Exception {
        Whitebox.invokeMethod(baseTestRegisterActivity, "onActivityResult", 2244, -1, data);
        Mockito.verify(baseTestRegisterActivity.presenter()).saveForm(null);
    }

}
