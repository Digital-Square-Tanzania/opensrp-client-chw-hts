package org.smartregister.chw.hts.activity;

import static org.smartregister.chw.hts.dao.HtsDao.getIndexContactMember;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vijay.jsonwizard.activities.JsonFormActivity;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.chw.hts.HtsLibrary;
import org.smartregister.chw.hts.R;
import org.smartregister.chw.hts.adapter.BaseHtsVisitAdapter;
import org.smartregister.chw.hts.contract.BaseHtsVisitContract;
import org.smartregister.chw.hts.dao.HtsDao;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.interactor.BaseHtsVisitInteractor;
import org.smartregister.chw.hts.model.BaseHtsVisitAction;
import org.smartregister.chw.hts.presenter.BaseHtsVisitPresenter;
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.view.activity.SecuredActivity;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import timber.log.Timber;

public class BaseHtsVisitActivity extends SecuredActivity implements BaseHtsVisitContract.View, View.OnClickListener {
    protected static String profileType;
    protected Map<String, BaseHtsVisitAction> actionList = new LinkedHashMap<>();
    protected BaseHtsVisitContract.Presenter presenter;
    protected MemberObject memberObject;
    protected String baseEntityID;
    protected Boolean isEditMode = false;
    protected RecyclerView.Adapter mAdapter;
    protected ProgressBar progressBar;
    protected TextView tvSubmit;
    protected TextView tvTitle;
    protected String current_action;
    protected String confirmCloseTitle;
    protected String confirmCloseMessage;

    public static void startMe(Activity activity, String baseEntityID, Boolean isEditMode) {
        Intent intent = new Intent(activity, BaseHtsVisitActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityID);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, isEditMode);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.PROFILE_TYPE, profileType);
        activity.startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_hts_visit_activity);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            memberObject = (MemberObject) getIntent().getSerializableExtra(Constants.ACTIVITY_PAYLOAD.MEMBER_PROFILE_OBJECT);
            isEditMode = getIntent().getBooleanExtra(Constants.ACTIVITY_PAYLOAD.EDIT_MODE, false);
            baseEntityID = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
            profileType = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.PROFILE_TYPE);
            if (profileType != null && profileType.equalsIgnoreCase(Constants.INDEX_CLIENT_PROFILE_TYPE)) {
                memberObject = getIndexContactMember(baseEntityID);
            } else {
                memberObject = getMemberObject(baseEntityID);
            }

        }

        confirmCloseTitle = getString(R.string.confirm_form_close);
        confirmCloseMessage = getString(R.string.confirm_form_close_explanation);
        setUpView();
        displayProgressBar(true);
        registerPresenter();
        if (presenter != null) {
            if (StringUtils.isNotBlank(baseEntityID)) {
                presenter.reloadMemberDetails(baseEntityID, profileType);
            } else {
                presenter.initialize();
            }
        }
    }

    protected MemberObject getMemberObject(String baseEntityId) {
        return HtsDao.getMember(baseEntityId);
    }

    public void setUpView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(false);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.GONE);

        findViewById(R.id.close).setOnClickListener(this);
        tvSubmit = findViewById(R.id.customFontTextViewSubmit);
        tvSubmit.setOnClickListener(this);
        tvTitle = findViewById(R.id.customFontTextViewName);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new BaseHtsVisitAdapter(this, this, (LinkedHashMap) actionList);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));
        redrawVisitUI();
    }

    protected void registerPresenter() {
        presenter = new BaseHtsVisitPresenter(memberObject, this, new BaseHtsVisitInteractor());
    }


    @Override
    public void initializeActions(LinkedHashMap<String, BaseHtsVisitAction> actionsMap) {
        final LinkedHashMap<String, BaseHtsVisitAction> map = actionsMap;
        actionList.clear();

        // Add actions in a specific order
        addActionToMap(map, R.string.hts_visit_type_action_title);
        addActionToMap(map, R.string.hts_pre_test_services_action_title);
        addActionToMap(map, R.string.hts_first_hiv_test_action_title);

        // Process repeated tests
        addSortedRepeatedActions(map,
                R.string.hts_repeate_of_first_hiv_test_action_title,
                actionList);

        addActionToMap(map, R.string.hts_second_hiv_test_action_title);

        addSortedRepeatedActions(map,
                R.string.hts_repeate_of_second_hiv_test_action_title,
                actionList);

        // Add remaining actions
        addActionToMap(map, R.string.hts_repeate_of_first_hiv_test_title);
        addActionToMap(map, R.string.hts_unigold_hiv_test_action_title);

        addSortedRepeatedActions(map,
                R.string.hts_repeate_of_unigold_hiv_test_action_title,
                actionList);

        addActionToMap(map, R.string.hts_dna_pcr_sample_collection_action_title);
        addActionToMap(map, R.string.hts_post_test_services_action_title);
        addActionToMap(map, R.string.hts_linkage_to_prevention_services_action_title);

        // Notify adapter and update UI
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        displayProgressBar(false);
    }

    // Helper to add a specific action to the actionList
    private void addActionToMap(Map<String, BaseHtsVisitAction> map, int stringResId) {
        String key = getString(stringResId);
        if (map.containsKey(key)) {
            actionList.put(key, map.get(key));
        }
    }

    // Helper to filter, sort, and add repeated actions to the actionList
    private void addSortedRepeatedActions(Map<String, BaseHtsVisitAction> map,
                                          int stringResId,
                                          Map<String, BaseHtsVisitAction> targetMap) {
        String regex = getString(stringResId).replace("%d", "\\d+");
        Pattern pattern = Pattern.compile(regex);

        // Temporary TreeMap for sorting
        Map<String, BaseHtsVisitAction> tempMap = new TreeMap<>((key1, key2) -> {
            int number1 = extractNumber(key1);
            int number2 = extractNumber(key2);
            return Integer.compare(number1, number2);
        });

        // Filter and collect matching actions
        for (Map.Entry<String, BaseHtsVisitAction> entry : map.entrySet()) {
            if (pattern.matcher(entry.getKey()).matches()) {
                tempMap.put(entry.getKey(), entry.getValue());
            }
        }

        // Add sorted entries to the target map
        for (Map.Entry<String, BaseHtsVisitAction> entry : tempMap.entrySet()) {
            targetMap.put(entry.getKey(), entry.getValue());
        }
    }

    // Helper to extract numbers from a string
    private int extractNumber(String input) {
        String numberStr = input.replaceAll("\\D+", "");
        return numberStr.isEmpty() ? 0 : Integer.parseInt(numberStr);
    }


    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void displayToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Boolean getEditMode() {
        return isEditMode;
    }

    @Override
    public void onMemberDetailsReloaded(MemberObject memberObject) {
        this.memberObject = memberObject;
        presenter.initialize();
        redrawHeader(memberObject);
    }

    @Override
    protected void onCreation() {
        Timber.v("Empty onCreation");
    }

    @Override
    protected void onResumption() {
        Timber.v("Empty onResumption");
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close) {
            displayExitDialog(() -> close());
        } else if (v.getId() == R.id.customFontTextViewSubmit) {
            submitVisit();
        }
    }

    @Override
    public BaseHtsVisitContract.Presenter presenter() {
        return presenter;
    }

    @Override
    public Form getFormConfig() {
        return null;
    }

    @Override
    public void startForm(BaseHtsVisitAction htsHomeVisitAction) {
        current_action = htsHomeVisitAction.getTitle();

        if (StringUtils.isNotBlank(htsHomeVisitAction.getJsonPayload())) {
            try {
                JSONObject jsonObject = new JSONObject(htsHomeVisitAction.getJsonPayload());
                startFormActivity(jsonObject);
            } catch (Exception e) {
                Timber.e(e);
                String locationId = HtsLibrary.getInstance().context().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
                presenter().startForm(htsHomeVisitAction.getFormName(), memberObject.getBaseEntityId(), locationId);
            }
        } else {
            String locationId = HtsLibrary.getInstance().context().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
            presenter().startForm(htsHomeVisitAction.getFormName(), memberObject.getBaseEntityId(), locationId);
        }
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, JsonFormActivity.class);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());

        if (getFormConfig() != null) {
            intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, getFormConfig());
        }

        startActivityForResult(intent, Constants.REQUEST_CODE_GET_JSON);
    }

    @Override
    public void startFragment(BaseHtsVisitAction htsHomeVisitAction) {
        current_action = htsHomeVisitAction.getTitle();

        if (htsHomeVisitAction.getDestinationFragment() != null)
            htsHomeVisitAction.getDestinationFragment().show(getSupportFragmentManager(), current_action);
    }

    @Override
    public void redrawHeader(MemberObject memberObject) {
        int age = new Period(new DateTime(memberObject.getAge()),
                new DateTime()).getYears();
        tvTitle.setText(MessageFormat.format("{0}, {1}",
                memberObject.getFullName(),
                String.valueOf(age)));
    }

    @Override
    public void redrawVisitUI() {
        boolean valid = !actionList.isEmpty();
        for (Map.Entry<String, BaseHtsVisitAction> entry : actionList.entrySet()) {
            BaseHtsVisitAction action = entry.getValue();
            if (
                    (!action.isOptional() && (action.getActionStatus() == BaseHtsVisitAction.Status.PENDING && action.isValid()))
                            || !action.isEnabled()
            ) {
                valid = false;
                break;
            }
        }

        int res_color = valid ? R.color.white : R.color.light_grey;
        tvSubmit.setTextColor(getResources().getColor(res_color));
        tvSubmit.setOnClickListener(valid ? this : null); // update listener to null

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void displayProgressBar(boolean state) {
        progressBar.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    @Override
    public Map<String, BaseHtsVisitAction> getHtsVisitActions() {
        return actionList;
    }

    @Override
    public void close() {
        finish();
    }

    @Override
    public void submittedAndClose(String results) {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        close();
    }

    @Override
    public BaseHtsVisitContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void submitVisit() {
        getPresenter().submitVisit();
    }

    @Override
    public void onDialogOptionUpdated(String jsonString) {
        BaseHtsVisitAction htsVisitAction = actionList.get(current_action);
        if (htsVisitAction != null) {
            htsVisitAction.setJsonPayload(jsonString);
        }

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            redrawVisitUI();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_GET_JSON) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                    BaseHtsVisitAction baseHtsVisitAction = actionList.get(current_action);
                    if (baseHtsVisitAction != null) {
                        baseHtsVisitAction.setJsonPayload(jsonString);
                    }
                } catch (Exception e) {
                    Timber.e(e);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {

                BaseHtsVisitAction baseHtsVisitAction = actionList.get(current_action);
                if (baseHtsVisitAction != null)
                    baseHtsVisitAction.evaluateStatus();
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        // update the adapter after every payload
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
            redrawVisitUI();
        }
    }

    @Override
    public Context getMyContext() {
        return this;
    }

    @Override
    public void onBackPressed() {
        displayExitDialog(BaseHtsVisitActivity.this::finish);
    }

    protected void displayExitDialog(final Runnable onConfirm) {
        AlertDialog dialog = new AlertDialog.Builder(this, com.vijay.jsonwizard.R.style.AppThemeAlertDialog).setTitle(confirmCloseTitle)
                .setMessage(confirmCloseMessage).setNegativeButton(com.vijay.jsonwizard.R.string.yes, (dialog1, which) -> {
                    if (onConfirm != null) {
                        onConfirm.run();
                    }
                }).setPositiveButton(com.vijay.jsonwizard.R.string.no, (dialog2, which) -> Timber.d("No button on dialog in %s", JsonFormActivity.class.getCanonicalName())).create();

        dialog.show();
    }
}
