package org.smartregister.chw.hts.activity;

import android.app.Activity;
import android.content.Intent;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONObject;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.presenter.BaseSkeletonVisitPresenter;
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.chw.hts.interactor.SkeletonServiceVisitInteractor;


public class SkeletonServiceActivity extends BaseSkeletonVisitActivity {
    public static void startSkeletonVisitActivity(Activity activity, String baseEntityId, Boolean editMode) {
        Intent intent = new Intent(activity, SkeletonServiceActivity.class);
        intent.putExtra(org.smartregister.chw.hts.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(org.smartregister.chw.hts.util.Constants.ACTIVITY_PAYLOAD.EDIT_MODE, editMode);
        intent.putExtra(org.smartregister.chw.hts.util.Constants.ACTIVITY_PAYLOAD.PROFILE_TYPE, Constants.PROFILE_TYPES.SKELETON_PROFILE);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected MemberObject getMemberObject(String baseEntityId) {
        return EntryActivity.getSampleMember();
    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseSkeletonVisitPresenter(memberObject, this, new SkeletonServiceVisitInteractor(Constants.EVENT_TYPE.SKELETON_SERVICES));
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, SampleJsonFormActivity.class);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        if (getFormConfig() != null) {
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, getFormConfig());
        }

        startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }


    @Override
    public void submittedAndClose(String results) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(Constants.JSON_FORM_EXTRA.JSON, results);
        setResult(Activity.RESULT_OK, returnIntent);
        close();
    }

}

