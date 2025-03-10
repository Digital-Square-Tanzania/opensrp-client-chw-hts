package org.smartregister.chw.hts.activity;

import static org.smartregister.client.utils.constants.JsonFormConstants.JSON_FORM_KEY.GLOBAL;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.MenuRes;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.Context;
import org.smartregister.chw.hts.R;
import org.smartregister.chw.hts.contract.HtsRegisterContract;
import org.smartregister.chw.hts.fragment.BaseHtsRegisterFragment;
import org.smartregister.chw.hts.interactor.BaseHtsRegisterInteractor;
import org.smartregister.chw.hts.listener.HtsBottomNavigationListener;
import org.smartregister.chw.hts.model.BaseHtsRegisterModel;
import org.smartregister.chw.hts.presenter.BaseHtsRegisterPresenter;
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.chw.hts.util.DBConstants;
import org.smartregister.chw.hts.util.HtsJsonFormUtils;
import org.smartregister.chw.hts.util.HtsUtil;
import org.smartregister.helper.BottomNavigationHelper;
import org.smartregister.listener.BottomNavigationListener;
import org.smartregister.repository.BaseRepository;
import org.smartregister.util.Utils;
import org.smartregister.view.activity.BaseRegisterActivity;
import org.smartregister.view.fragment.BaseRegisterFragment;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class BaseHtsRegisterActivity extends BaseRegisterActivity implements HtsRegisterContract.View {

    protected String BASE_ENTITY_ID;
    protected String FAMILY_BASE_ENTITY_ID;
    protected String FORM_NAME;
    protected String sex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BASE_ENTITY_ID = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        FAMILY_BASE_ENTITY_ID = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.FAMILY_BASE_ENTITY_ID);
        FORM_NAME = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.HTS_FORM_NAME);
        sex = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.SEX);
        onStartActivityWithAction();
    }

    /**
     * Process a payload when an activity is started with an action
     */
    protected void onStartActivityWithAction() {
        if (FORM_NAME != null) {
            startFormActivity(FORM_NAME, BASE_ENTITY_ID, null);
        }
    }

    @Override
    public void startRegistration() {
        startFormActivity(FORM_NAME, null, null);
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        try {
            if (mBaseFragment instanceof BaseHtsRegisterFragment) {
                String locationId = Context.getInstance().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
                presenter().startForm(formName, entityId, metaData, locationId);
            }
        } catch (Exception e) {
            Timber.e(e);
            displayToast(getString(R.string.error_unable_to_start_form));
        }
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        if(FORM_NAME.equals(Constants.FORMS.HTS_SCREENING_15_AND_ABOVE)){
            try {
                jsonForm.getJSONObject(GLOBAL).put("sex",sex);
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        Intent intent = new Intent(this, BaseHtsRegisterActivity.class);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        if (getFormConfig() != null) {
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, getFormConfig());
        }
        startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    public Form getFormConfig() {
        return null;
    }

    @Override
    protected void onActivityResultExtended(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            presenter().saveForm(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
//            finish();
        }
    }

    @Override
    public List<String> getViewIdentifiers() {
        return Arrays.asList(Constants.CONFIGURATION.Hts_ENROLLMENT);
    }

    /**
     * Override this to subscribe to bottom navigation
     */
    @Override
    protected void registerBottomNavigation() {
        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(org.smartregister.R.id.bottom_navigation);

        if (bottomNavigationView != null) {
            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_clients);
            bottomNavigationView.getMenu().removeItem(R.id.action_register);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_search);
            bottomNavigationView.getMenu().removeItem(org.smartregister.R.id.action_library);
            bottomNavigationView.inflateMenu(getMenuResource());
            bottomNavigationHelper.disableShiftMode(bottomNavigationView);
            BottomNavigationListener familyBottomNavigationListener = new HtsBottomNavigationListener(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(familyBottomNavigationListener);
        }
    }

    @MenuRes
    public int getMenuResource() {
        return R.menu.bottom_nav_hts;
    }

    @Override
    protected void initializePresenter() {
        presenter = new BaseHtsRegisterPresenter(this, new BaseHtsRegisterModel(), new BaseHtsRegisterInteractor());
    }

    @Override
    protected BaseRegisterFragment getRegisterFragment() {
        return new BaseHtsRegisterFragment();
    }

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[0];
    }

    @Override
    public HtsRegisterContract.Presenter presenter() {
        return (HtsRegisterContract.Presenter) presenter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.REQUEST_CODE_GET_JSON) {

            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                JSONObject form = new JSONObject(jsonString);
                JSONArray fieldsOne = HtsJsonFormUtils.fields(form, Constants.STEP_ONE);
                updateFormField(fieldsOne, DBConstants.KEY.RELATIONAL_ID, FAMILY_BASE_ENTITY_ID);
                presenter().saveForm(form.toString());
            } catch (JSONException e) {
                Timber.e(e);
                displayToast(getString(R.string.error_unable_to_save_form));
            }
            startClientProcessing();
        }
    }

    private void updateFormField(JSONArray formFieldArrays, String formFieldKey, String updateValue) {
        if (updateValue != null) {
            JSONObject formObject = org.smartregister.util.JsonFormUtils.getFieldJSONObject(formFieldArrays, formFieldKey);
            if (formObject != null) {
                try {
                    formObject.remove(org.smartregister.util.JsonFormUtils.VALUE);
                    formObject.put(org.smartregister.util.JsonFormUtils.VALUE, updateValue);
                } catch (JSONException e) {
                    Timber.e(e);
                }
            }
        }
    }

    public void startClientProcessing() {
        try {
            long lastSyncTimeStamp = Utils.getAllSharedPreferences().fetchLastUpdatedAtDate(0);
            Date lastSyncDate = new Date(lastSyncTimeStamp);
            HtsUtil.getClientProcessorForJava().processClient(HtsUtil.getSyncHelper().getEvents(lastSyncDate, BaseRepository.TYPE_Unprocessed));
            Utils.getAllSharedPreferences().saveLastUpdatedAtDate(lastSyncDate.getTime());
        } catch (Exception e) {
            Timber.d(e);
        }

    }
}
