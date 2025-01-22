package org.smartregister.chw.hts.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.vijay.jsonwizard.activities.JsonWizardFormActivity;
import com.vijay.jsonwizard.domain.Form;
import com.vijay.jsonwizard.factory.FileSourceFactoryHelper;


import org.json.JSONObject;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.domain.Visit;
import org.smartregister.chw.hts.util.Constants;


import timber.log.Timber;


public class HtsMemberProfileActivity extends BaseHtsProfileActivity {
    private Visit enrollmentVisit = null;
    private Visit serviceVisit = null;

    public static void startMe(Activity activity, String baseEntityID) {
        Intent intent = new Intent(activity, HtsMemberProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        activity.startActivity(intent);
    }

    @Override
    protected MemberObject getMemberObject(String baseEntityId) {
        return EntryActivity.getSampleMember();
    }

    @Override
    public void openFollowupVisit() {
        try {
            HtsServiceActivity.startHtsVisitActivity(this,memberObject.getBaseEntityId(), false);

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public void startDnaPcrSampleCollection() {

    }

    @Override
    public void startServiceForm() {
        HtsServiceActivity.startHtsVisitActivity(this, memberObject.getBaseEntityId(), false);
    }


    @Override
    public void continueService() {

    }


    @Override
    public void continueDischarge() {

    }

    @Override
    protected Visit getServiceVisit() {
        return serviceVisit;
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        delayRefreshSetupViews();
    }


    private void delayRefreshSetupViews() {
        try {
            new Handler(Looper.getMainLooper()).postDelayed(this::setupViews, 300);
        } catch (Exception e) {
            Timber.e(e);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_GET_JSON && resultCode == Activity.RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                String encounterType = form.getString(Constants.JSON_FORM_EXTRA.EVENT_TYPE);
                switch (encounterType) {
                    case Constants.EVENT_TYPE.HTS_SERVICES:
                        serviceVisit = new Visit();
                        serviceVisit.setProcessed(true);
                        serviceVisit.setJson(jsonString);
                        break;

                }
            } catch (Exception e) {
                Timber.e(e);
            }

        }

    }
}
