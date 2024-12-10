package org.smartregister.chw.hts.fragment;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;
import org.smartregister.chw.hts.activity.BaseSkeletonProfileActivity;
import org.smartregister.commonregistry.CommonPersonObjectClient;

import static org.mockito.Mockito.times;

public class BaseSkeletonRegisterFragmentTest {
    @Mock
    public BaseSkeletonRegisterFragment baseTestRegisterFragment;

    @Mock
    public CommonPersonObjectClient client;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = Exception.class)
    public void openProfile() throws Exception {
        Whitebox.invokeMethod(baseTestRegisterFragment, "openProfile", client);
        PowerMockito.mockStatic(BaseSkeletonProfileActivity.class);
        BaseSkeletonProfileActivity.startProfileActivity(null, null);
        PowerMockito.verifyStatic(times(1));

    }
}
