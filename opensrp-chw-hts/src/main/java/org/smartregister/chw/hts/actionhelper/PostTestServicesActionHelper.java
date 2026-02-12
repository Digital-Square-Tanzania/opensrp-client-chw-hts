package org.smartregister.chw.hts.actionhelper;

import static org.smartregister.client.utils.constants.JsonFormConstants.JSON_FORM_KEY.GLOBAL;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.domain.VisitDetail;
import org.smartregister.chw.hts.model.BaseHtsVisitAction;
import org.smartregister.chw.hts.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PostTestServicesActionHelper implements BaseHtsVisitAction.HtsVisitActionHelper {

    private static final String STEP_ONE = "step1";
    private static final String FIELDS = "fields";
    private static final String OPTIONS = "options";
    private static final String KEY = "key";
    private static final String FIELD_HIV_RESULTS_DISCLOSURE = "hts_hiv_results_disclosure";
    private static final String VISIBILITY_ALLOWED_SEX = "visibility_allowed_sex";
    private static final String VISIBILITY_MINIMUM_AGE = "visibility_minimum_age";
    private static final String VISIBILITY_MINIMUM_AGE_FEMALE = "visibility_minimum_age_female";
    private static final String VISIBILITY_MINIMUM_AGE_MALE = "visibility_minimum_age_male";

    protected String hasPostTestCounsellingBeenProvided;

    protected String jsonPayload;

    protected Context context;

    protected MemberObject memberObject;

    protected String hivTestResults;


    public PostTestServicesActionHelper(Context context, MemberObject memberObject, String hivTestResults) {
        this.context = context;
        this.memberObject = memberObject;
        this.hivTestResults = hivTestResults;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
    }

    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            JSONObject global = jsonObject.optJSONObject(GLOBAL);
            if (global == null) {
                global = new JSONObject();
                jsonObject.put(GLOBAL, global);
            }

            global.put("hiv_test_results", hivTestResults);
            global.put("sex", memberObject != null ? memberObject.getGender() : "");
            global.put("age", getClientAge());

            updateHivDisclosureOptions(jsonObject);

            return jsonObject.toString();
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    private void updateHivDisclosureOptions(JSONObject jsonObject) {
        JSONObject stepOne = jsonObject.optJSONObject(STEP_ONE);
        if (stepOne == null) {
            return;
        }

        JSONArray fields = stepOne.optJSONArray(FIELDS);
        if (fields == null) {
            return;
        }

        JSONObject hivResultsDisclosureField = findFieldByKey(fields, FIELD_HIV_RESULTS_DISCLOSURE);
        if (hivResultsDisclosureField == null) {
            return;
        }

        JSONArray options = hivResultsDisclosureField.optJSONArray(OPTIONS);
        if (options == null) {
            return;
        }

        String sex = memberObject != null ? memberObject.getGender() : null;
        int age = getClientAge();

        JSONArray filteredOptions = new JSONArray();
        for (int index = 0; index < options.length(); index++) {
            JSONObject option = options.optJSONObject(index);
            if (option != null && shouldDisplayOption(option, sex, age)) {
                filteredOptions.put(option);
            }
        }

        try {
            hivResultsDisclosureField.put(OPTIONS, filteredOptions);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    private JSONObject findFieldByKey(JSONArray fields, String fieldKey) {
        for (int index = 0; index < fields.length(); index++) {
            JSONObject field = fields.optJSONObject(index);
            if (field != null && fieldKey.equals(field.optString(KEY))) {
                return field;
            }
        }
        return null;
    }

    private boolean shouldDisplayOption(JSONObject option, String sex, int age) {
        String allowedSex = option.optString(VISIBILITY_ALLOWED_SEX);
        if (StringUtils.isBlank(allowedSex)) {
            return true;
        }

        String normalizedSex = StringUtils.defaultString(sex).trim().toLowerCase();
        if (!isSexAllowed(normalizedSex, allowedSex)) {
            return false;
        }

        int minimumAge = getMinimumAge(option, normalizedSex);
        return minimumAge < 0 || age >= minimumAge;
    }

    private boolean isSexAllowed(String normalizedSex, String allowedSexes) {
        if (StringUtils.isBlank(normalizedSex)) {
            return false;
        }

        String[] allowedSexList = allowedSexes.toLowerCase().split(",");
        for (String allowedSex : allowedSexList) {
            if (normalizedSex.equals(allowedSex.trim())) {
                return true;
            }
        }
        return false;
    }

    private int getMinimumAge(JSONObject option, String normalizedSex) {
        if ("female".equals(normalizedSex) && option.has(VISIBILITY_MINIMUM_AGE_FEMALE)) {
            return option.optInt(VISIBILITY_MINIMUM_AGE_FEMALE, -1);
        }

        if ("male".equals(normalizedSex) && option.has(VISIBILITY_MINIMUM_AGE_MALE)) {
            return option.optInt(VISIBILITY_MINIMUM_AGE_MALE, -1);
        }

        return option.optInt(VISIBILITY_MINIMUM_AGE, -1);
    }

    private int getClientAge() {
        if (memberObject == null) {
            return -1;
        }

        try {
            return memberObject.getAge();
        } catch (Exception e) {
            Timber.e(e);
            return -1;
        }
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            hasPostTestCounsellingBeenProvided = JsonFormUtils.getValue(jsonObject, "hts_has_post_test_counselling_been_provided");
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    @Override
    public BaseHtsVisitAction.ScheduleStatus getPreProcessedStatus() {
        return null;
    }

    @Override
    public String getPreProcessedSubTitle() {
        return null;
    }

    @Override
    public String postProcess(String jsonPayload) {
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        return null;
    }

    @Override
    public BaseHtsVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isNotBlank(hasPostTestCounsellingBeenProvided)) {
            return BaseHtsVisitAction.Status.COMPLETED;
        }
        return BaseHtsVisitAction.Status.PENDING;
    }

    @Override
    public void onPayloadReceived(BaseHtsVisitAction baseHtsVisitAction) {
        //overridden
    }

}
