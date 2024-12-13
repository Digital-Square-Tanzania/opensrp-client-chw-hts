package org.smartregister.chw.hts.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.json.JSONObject;
import org.smartregister.chw.hts.R;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.interactor.HtsServiceVisitInteractor;
import org.smartregister.chw.hts.model.BaseHtsVisitAction;
import org.smartregister.chw.hts.presenter.BaseHtsVisitPresenter;
import org.smartregister.chw.hts.util.Constants;

import java.util.LinkedHashMap;
import java.util.Map;


public class HtsServiceActivity extends BaseHtsVisitActivity {
    public static void startHtsVisitActivity(Activity activity, String baseEntityId, Boolean editMode) {
        Intent intent = new Intent(activity, HtsServiceActivity.class);
        intent.putExtra(org.smartregister.chw.hts.util.Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        intent.putExtra(org.smartregister.chw.hts.util.Constants.ACTIVITY_PAYLOAD.EDIT_MODE, editMode);
        intent.putExtra(org.smartregister.chw.hts.util.Constants.ACTIVITY_PAYLOAD.PROFILE_TYPE, Constants.PROFILE_TYPES.Hts_PROFILE);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected MemberObject getMemberObject(String baseEntityId) {
        return EntryActivity.getSampleMember();
    }

    @Override
    protected void registerPresenter() {
        presenter = new BaseHtsVisitPresenter(memberObject, this, new HtsServiceVisitInteractor(Constants.EVENT_TYPE.HTS_SERVICES));
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

    @Override
    public void initializeActions(LinkedHashMap<String, BaseHtsVisitAction> map) {
        actionList.clear();

        //Necessary evil to rearrange the actions according to a specific arrangement
        if (map.containsKey(getString(R.string.hts_visit_type_action_title))) {
            actionList.put(getString(R.string.hts_visit_type_action_title), map.get(getString(R.string.hts_visit_type_action_title)));
        }

        if (map.containsKey(getString(R.string.hts_pre_test_services_action_title))) {
            actionList.put(getString(R.string.hts_pre_test_services_action_title), map.get(getString(R.string.hts_pre_test_services_action_title)));
        }

        if (map.containsKey(getString(R.string.hts_first_hiv_test_action_title))) {
            actionList.put(getString(R.string.hts_first_hiv_test_action_title), map.get(getString(R.string.hts_first_hiv_test_action_title)));
        }

        int i = 0;
        for (Map.Entry<String, BaseHtsVisitAction> entry : map.entrySet()) {
            i++;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && entry.getKey().contains(String.format(getString(R.string.hts_repeate_of_first_hiv_test_action_title), i))) {
                actionList.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        if (map.containsKey(getString(R.string.hts_second_hiv_test_action_title))) {
            actionList.put(getString(R.string.hts_second_hiv_test_action_title), map.get(getString(R.string.hts_second_hiv_test_action_title)));
        }

        int j = 0;
        for (Map.Entry<String, BaseHtsVisitAction> entry : map.entrySet()) {
            j++;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && entry.getKey().contains(String.format(getString(R.string.hts_repeate_of_second_hiv_test_action_title), j))) {
                actionList.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }

        if (map.containsKey(getString(R.string.hts_unigold_hiv_test_action_title))) {
            actionList.put(getString(R.string.hts_unigold_hiv_test_action_title), map.get(getString(R.string.hts_unigold_hiv_test_action_title)));
        }

        if (map.containsKey(getString(R.string.hts_first_hiv_test_action_title))) {
            actionList.put(getString(R.string.hts_first_hiv_test_action_title), map.get(getString(R.string.hts_first_hiv_test_action_title)));
        }

        if (map.containsKey(getString(R.string.hts_post_test_services_action_title))) {
            actionList.put(getString(R.string.hts_post_test_services_action_title), map.get(getString(R.string.hts_post_test_services_action_title)));
        }

        if (map.containsKey(getString(R.string.hts_linkage_to_prevention_services_action_title))) {
            actionList.put(getString(R.string.hts_linkage_to_prevention_services_action_title), map.get(getString(R.string.hts_linkage_to_prevention_services_action_title)));
        }
        //====================End of Necessary evil ====================================



        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        displayProgressBar(false);
    }

}

