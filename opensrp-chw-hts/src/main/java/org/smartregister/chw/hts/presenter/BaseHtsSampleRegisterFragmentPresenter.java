package org.smartregister.chw.hts.presenter;

import org.smartregister.chw.hts.contract.HtsRegisterFragmentContract;
import org.smartregister.chw.hts.util.Constants;

public class BaseHtsSampleRegisterFragmentPresenter extends BaseHtsRegisterFragmentPresenter {

    public BaseHtsSampleRegisterFragmentPresenter(HtsRegisterFragmentContract.View view, HtsRegisterFragmentContract.Model model, String viewConfigurationIdentifier) {
        super(view, model, viewConfigurationIdentifier);
    }


    @Override
    public String getMainCondition() {
        return " " + getMainTable() + ".is_closed = 0 ";
    }


    @Override
    public String getMainTable() {
        return Constants.TABLES.HTS_SAMPLE_REGISTER;
    }

    @Override
    public String getDueFilterCondition() {
        return " (cast( julianday(STRFTIME('%Y-%m-%d', datetime('now'))) -  julianday(IFNULL(SUBSTR(hts_test_date,7,4)|| '-' || SUBSTR(hts_test_date,4,2) || '-' || SUBSTR(hts_test_date,1,2),'')) as integer) between 7 and 14) ";
    }
}
