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
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.chw.hts.util.JsonFormUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

public class PostTestServicesActionHelper implements BaseHtsVisitAction.HtsVisitActionHelper {

    private static final String STEP_ONE = "step1";
    private static final String FIELDS = "fields";
    private static final String OPTIONS = "options";
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String FIELD_HIV_RESULTS_DISCLOSURE = "hts_hiv_results_disclosure";
    private static final String VISIBILITY_ALLOWED_SEX = "visibility_allowed_sex";
    private static final String VISIBILITY_MINIMUM_AGE = "visibility_minimum_age";
    private static final String VISIBILITY_MINIMUM_AGE_FEMALE = "visibility_minimum_age_female";
    private static final String VISIBILITY_MINIMUM_AGE_MALE = "visibility_minimum_age_male";
    private static final String TEST_RESULT = "test_result";
    private static final String FINAL_HIV_TEST_RESULT = "final_hiv_test_result";
    private static final Pattern ACTION_NUMBER_PATTERN = Pattern.compile("(\\d+)");
    private static final int HIV_FIRST_TEST_PRIORITY = 1;
    private static final int HIV_REPEAT_FIRST_TEST_PRIORITY = 2;
    private static final int HIV_UNIGOLD_TEST_PRIORITY = 3;

    protected String hasPostTestCounsellingBeenProvided;

    protected String jsonPayload;

    protected Context context;

    protected MemberObject memberObject;

    protected String hivTestResults;
    private final Map<String, BaseHtsVisitAction> priorActions;


    public PostTestServicesActionHelper(Context context, MemberObject memberObject, String hivTestResults) {
        this(context, memberObject, hivTestResults, null);
    }

    public PostTestServicesActionHelper(Context context,
                                        MemberObject memberObject,
                                        String hivTestResults,
                                        Map<String, BaseHtsVisitAction> priorActions) {
        this.context = context;
        this.memberObject = memberObject;
        this.hivTestResults = hivTestResults;
        this.priorActions = priorActions == null ? null : new LinkedHashMap<>(priorActions);
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
            String finalHivTestResult = resolveFinalHivTestResult();
            updateFinalHivTestResultField(jsonObject, finalHivTestResult);
            global.put("sex", memberObject != null ? memberObject.getGender() : "");
            global.put("age", getClientAge());

            updateHivDisclosureOptions(jsonObject);

            return jsonObject.toString();
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    private String resolveFinalHivTestResult() {
        if (priorActions == null || priorActions.isEmpty()) {
            return null;
        }

        HivTestResultCandidate selectedCandidate = null;
        for (Map.Entry<String, BaseHtsVisitAction> entry : priorActions.entrySet()) {
            BaseHtsVisitAction action = entry.getValue();
            if (action == null) {
                continue;
            }

            int actionPriority = getActionPriority(action.getFormName());
            if (actionPriority < 0) {
                continue;
            }

            String mappedFinalResult = mapFinalResult(action.getFormName(), getActionTestResult(action.getJsonPayload()));
            if (StringUtils.isBlank(mappedFinalResult)) {
                continue;
            }

            int actionNumber = extractActionNumber(action.getTitle(), entry.getKey());
            HivTestResultCandidate newCandidate = new HivTestResultCandidate(mappedFinalResult, actionPriority, actionNumber);
            if (shouldOverrideCandidate(selectedCandidate, newCandidate)) {
                selectedCandidate = newCandidate;
            }
        }

        return selectedCandidate != null ? selectedCandidate.result : null;
    }

    private void updateFinalHivTestResultField(JSONObject jsonObject, String finalHivTestResult) {
        if (StringUtils.isBlank(finalHivTestResult)) {
            return;
        }

        JSONObject stepOne = jsonObject.optJSONObject(STEP_ONE);
        if (stepOne == null) {
            return;
        }

        JSONArray fields = stepOne.optJSONArray(FIELDS);
        if (fields == null) {
            return;
        }

        JSONObject finalHivTestResultField = findFieldByKey(fields, FINAL_HIV_TEST_RESULT);
        if (finalHivTestResultField == null) {
            return;
        }

        try {
            finalHivTestResultField.put(VALUE, finalHivTestResult);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    private boolean shouldOverrideCandidate(HivTestResultCandidate currentCandidate, HivTestResultCandidate newCandidate) {
        if (currentCandidate == null) {
            return true;
        }

        if (newCandidate.priority > currentCandidate.priority) {
            return true;
        }

        return newCandidate.priority == currentCandidate.priority
                && newCandidate.actionNumber >= currentCandidate.actionNumber;
    }

    private int getActionPriority(String formName) {
        if (Constants.FORMS.HTS_UNIGOLD_HIV_TEST.equals(formName)) {
            return HIV_UNIGOLD_TEST_PRIORITY;
        }

        if (Constants.FORMS.HTS_REPEAT_FIRST_HIV_TEST.equals(formName)) {
            return HIV_REPEAT_FIRST_TEST_PRIORITY;
        }

        if (Constants.FORMS.HTS_FIRST_HIV_TEST.equals(formName)) {
            return HIV_FIRST_TEST_PRIORITY;
        }

        return -1;
    }

    private String getActionTestResult(String actionPayload) {
        if (StringUtils.isBlank(actionPayload)) {
            return null;
        }

        try {
            JSONObject actionJson = new JSONObject(actionPayload);
            return StringUtils.trimToNull(JsonFormUtils.getValue(actionJson, TEST_RESULT));
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    private String mapFinalResult(String formName, String testResult) {
        if (StringUtils.isBlank(formName) || StringUtils.isBlank(testResult)) {
            return null;
        }

        if (Constants.FORMS.HTS_FIRST_HIV_TEST.equals(formName)
                && Constants.HIV_TEST_RESULTS.NON_REACTIVE.equalsIgnoreCase(testResult)) {
            return Constants.HIV_TEST_RESULTS.NEGATIVE;
        }

        if (Constants.FORMS.HTS_REPEAT_FIRST_HIV_TEST.equals(formName)) {
            if (Constants.HIV_TEST_RESULTS.NON_REACTIVE.equalsIgnoreCase(testResult)) {
                return Constants.HIV_TEST_RESULTS.NEGATIVE;
            }
            if (Constants.HIV_TEST_RESULTS.REACTIVE.equalsIgnoreCase(testResult)) {
                return Constants.HIV_TEST_RESULTS.INCONCLUSIVE;
            }
        }

        if (Constants.FORMS.HTS_UNIGOLD_HIV_TEST.equals(formName)) {
            if (Constants.HIV_TEST_RESULTS.REACTIVE.equalsIgnoreCase(testResult)) {
                return Constants.HIV_TEST_RESULTS.POSITIVE;
            }
            if (Constants.HIV_TEST_RESULTS.NON_REACTIVE.equalsIgnoreCase(testResult)) {
                return Constants.HIV_TEST_RESULTS.INCONCLUSIVE;
            }
        }

        return null;
    }

    private int extractActionNumber(String actionTitle, String actionKey) {
        String actionIdentifier = StringUtils.defaultIfBlank(actionTitle, actionKey);
        if (StringUtils.isBlank(actionIdentifier)) {
            return 1;
        }

        int actionNumber = 1;
        Matcher matcher = ACTION_NUMBER_PATTERN.matcher(actionIdentifier);
        while (matcher.find()) {
            try {
                actionNumber = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                Timber.e(e);
            }
        }
        return actionNumber;
    }

    private static class HivTestResultCandidate {
        private final String result;
        private final int priority;
        private final int actionNumber;

        private HivTestResultCandidate(String result, int priority, int actionNumber) {
            this.result = result;
            this.priority = priority;
            this.actionNumber = actionNumber;
        }
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
