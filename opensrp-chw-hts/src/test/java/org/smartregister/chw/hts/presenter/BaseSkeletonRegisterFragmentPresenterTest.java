package org.smartregister.chw.hts.presenter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.smartregister.chw.hts.contract.SkeletonRegisterFragmentContract;
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.chw.hts.util.DBConstants;
import org.smartregister.configurableviews.model.View;

import java.util.Set;
import java.util.TreeSet;

public class BaseSkeletonRegisterFragmentPresenterTest {
    @Mock
    protected SkeletonRegisterFragmentContract.View view;

    @Mock
    protected SkeletonRegisterFragmentContract.Model model;

    private BaseSkeletonRegisterFragmentPresenter baseSkeletonRegisterFragmentPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        baseSkeletonRegisterFragmentPresenter = new BaseSkeletonRegisterFragmentPresenter(view, model, "");
    }

    @Test
    public void assertNotNull() {
        Assert.assertNotNull(baseSkeletonRegisterFragmentPresenter);
    }

    @Test
    public void getMainCondition() {
        Assert.assertEquals(" ec_hts_enrollment.is_closed = 0 ", baseSkeletonRegisterFragmentPresenter.getMainCondition());
    }

    @Test
    public void getDueFilterCondition() {
        Assert.assertEquals(" (cast( julianday(STRFTIME('%Y-%m-%d', datetime('now'))) -  julianday(IFNULL(SUBSTR(hts_test_date,7,4)|| '-' || SUBSTR(hts_test_date,4,2) || '-' || SUBSTR(hts_test_date,1,2),'')) as integer) between 7 and 14) ", baseSkeletonRegisterFragmentPresenter.getDueFilterCondition());
    }

    @Test
    public void getDefaultSortQuery() {
        Assert.assertEquals(Constants.TABLES.SKELETON_ENROLLMENT + "." + DBConstants.KEY.LAST_INTERACTED_WITH + " DESC ", baseSkeletonRegisterFragmentPresenter.getDefaultSortQuery());
    }

    @Test
    public void getMainTable() {
        Assert.assertEquals(Constants.TABLES.SKELETON_ENROLLMENT, baseSkeletonRegisterFragmentPresenter.getMainTable());
    }

    @Test
    public void initializeQueries() {
        Set<View> visibleColumns = new TreeSet<>();
        baseSkeletonRegisterFragmentPresenter.initializeQueries(null);
        Mockito.doNothing().when(view).initializeQueryParams(Constants.TABLES.SKELETON_ENROLLMENT, null, null);
        Mockito.verify(view).initializeQueryParams(Constants.TABLES.SKELETON_ENROLLMENT, null, null);
        Mockito.verify(view).initializeAdapter(visibleColumns);
        Mockito.verify(view).countExecute();
        Mockito.verify(view).filterandSortInInitializeQueries();
    }

}