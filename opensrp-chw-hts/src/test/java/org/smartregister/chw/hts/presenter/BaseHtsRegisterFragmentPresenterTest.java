package org.smartregister.chw.hts.presenter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.smartregister.chw.hts.contract.HtsRegisterFragmentContract;
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.chw.hts.util.DBConstants;
import org.smartregister.configurableviews.model.View;

import java.util.Set;
import java.util.TreeSet;

public class BaseHtsRegisterFragmentPresenterTest {
    @Mock
    protected HtsRegisterFragmentContract.View view;

    @Mock
    protected HtsRegisterFragmentContract.Model model;

    private BaseHtsRegisterFragmentPresenter baseHtsRegisterFragmentPresenter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        baseHtsRegisterFragmentPresenter = new BaseHtsRegisterFragmentPresenter(view, model, "");
    }

    @Test
    public void assertNotNull() {
        Assert.assertNotNull(baseHtsRegisterFragmentPresenter);
    }

    @Test
    public void getMainCondition() {
        Assert.assertEquals(" ec_hts_enrollment.is_closed = 0 ", baseHtsRegisterFragmentPresenter.getMainCondition());
    }

    @Test
    public void getDueFilterCondition() {
        Assert.assertEquals(" (cast( julianday(STRFTIME('%Y-%m-%d', datetime('now'))) -  julianday(IFNULL(SUBSTR(hts_test_date,7,4)|| '-' || SUBSTR(hts_test_date,4,2) || '-' || SUBSTR(hts_test_date,1,2),'')) as integer) between 7 and 14) ", baseHtsRegisterFragmentPresenter.getDueFilterCondition());
    }

    @Test
    public void getDefaultSortQuery() {
        Assert.assertEquals(Constants.TABLES.HTS_ENROLLMENT + "." + DBConstants.KEY.LAST_INTERACTED_WITH + " DESC ", baseHtsRegisterFragmentPresenter.getDefaultSortQuery());
    }

    @Test
    public void getMainTable() {
        Assert.assertEquals(Constants.TABLES.HTS_ENROLLMENT, baseHtsRegisterFragmentPresenter.getMainTable());
    }

    @Test
    public void initializeQueries() {
        Set<View> visibleColumns = new TreeSet<>();
        baseHtsRegisterFragmentPresenter.initializeQueries(null);
        Mockito.doNothing().when(view).initializeQueryParams(Constants.TABLES.HTS_ENROLLMENT, null, null);
        Mockito.verify(view).initializeQueryParams(Constants.TABLES.HTS_ENROLLMENT, null, null);
        Mockito.verify(view).initializeAdapter(visibleColumns);
        Mockito.verify(view).countExecute();
        Mockito.verify(view).filterandSortInInitializeQueries();
    }

}