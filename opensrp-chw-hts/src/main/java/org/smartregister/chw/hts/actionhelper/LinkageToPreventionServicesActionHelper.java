package org.smartregister.chw.hts.actionhelper;

import android.content.Context;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hts.dao.HtsDao;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.domain.VisitDetail;
import org.smartregister.chw.hts.model.BaseHtsVisitAction;
import org.smartregister.chw.hts.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class LinkageToPreventionServicesActionHelper implements BaseHtsVisitAction.HtsVisitActionHelper {

    private static final String STEP_ONE = "step1";
    private static final String FIELDS = "fields";
    private static final String OPTIONS = "options";
    private static final String KEY = "key";
    private static final String GLOBAL = "global";
    private static final String FIELD_PREVENTIVE_SERVICES = "hts_preventive_services";
    private static final String FIELD_TB_SCREENING_OUTCOME = "hts_clients_tb_screening_outcome";
    private static final String TB_PRESUMPTIVE_RESULT = "tb_suspect";
    private static final String VISIBILITY_ALLOWED_SEX = "visibility_allowed_sex";
    private static final String VISIBILITY_MINIMUM_AGE = "visibility_minimum_age";
    private static final String VISIBILITY_MAXIMUM_AGE = "visibility_maximum_age";
    private static final String VISIBILITY_ONLY_TB_PRESUMPTIVE = "visibility_only_tb_presumptive";

    protected String referralToPreventionServicesProvided;

    protected String jsonPayload;

    protected Context context;

    protected MemberObject memberObject;

    protected Map<String, List<VisitDetail>> details;


    public LinkageToPreventionServicesActionHelper(Context context, MemberObject memberObject) {
        this.context = context;
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
        this.details = map;
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

            global.put("sex", memberObject != null ? memberObject.getGender() : "");
            global.put("age", getClientAge());

            updatePreventiveServiceOptions(jsonObject);

            return jsonObject.toString();
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    private void updatePreventiveServiceOptions(JSONObject jsonObject) {
        JSONObject stepOne = jsonObject.optJSONObject(STEP_ONE);
        if (stepOne == null) {
            return;
        }

        JSONArray fields = stepOne.optJSONArray(FIELDS);
        if (fields == null) {
            return;
        }

        JSONObject preventiveServicesField = findFieldByKey(fields, FIELD_PREVENTIVE_SERVICES);
        if (preventiveServicesField == null) {
            return;
        }

        JSONArray options = preventiveServicesField.optJSONArray(OPTIONS);
        if (options == null) {
            return;
        }

        String sex = memberObject != null ? memberObject.getGender() : null;
        int age = getClientAge();
        boolean tbPresumptive = isClientTbPresumptive();

        JSONArray filteredOptions = new JSONArray();
        for (int index = 0; index < options.length(); index++) {
            JSONObject option = options.optJSONObject(index);
            if (option != null && shouldDisplayOption(option, sex, age, tbPresumptive)) {
                filteredOptions.put(option);
            }
        }

        try {
            preventiveServicesField.put(OPTIONS, filteredOptions);
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

    private boolean shouldDisplayOption(JSONObject option, String sex, int age, boolean tbPresumptive) {
        if (option.optBoolean(VISIBILITY_ONLY_TB_PRESUMPTIVE, false) && !tbPresumptive) {
            return false;
        }

        String allowedSex = option.optString(VISIBILITY_ALLOWED_SEX);
        if (StringUtils.isNotBlank(allowedSex) && !isSexAllowed(sex, allowedSex)) {
            return false;
        }

        int minimumAge = option.optInt(VISIBILITY_MINIMUM_AGE, -1);
        if (minimumAge >= 0 && age < minimumAge) {
            return false;
        }

        int maximumAge = option.optInt(VISIBILITY_MAXIMUM_AGE, -1);
        return maximumAge < 0 || age <= maximumAge;
    }

    private boolean isSexAllowed(String sex, String allowedSexes) {
        String normalizedSex = StringUtils.defaultString(sex).trim().toLowerCase();
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

    private boolean isClientTbPresumptive() {
        String preTestOutcome = getPreTestTbOutcomeFromDetails();
        if (StringUtils.isNotBlank(preTestOutcome)) {
            return TB_PRESUMPTIVE_RESULT.equalsIgnoreCase(preTestOutcome);
        }

        String latestTbOutcome = getLatestTbScreeningOutcome();
        if (StringUtils.isNotBlank(latestTbOutcome)) {
            return TB_PRESUMPTIVE_RESULT.equalsIgnoreCase(latestTbOutcome);
        }

        String tbSymptomsAssessment = getTbSymptomsAssessment();
        return StringUtils.isNotBlank(tbSymptomsAssessment) && !tbSymptomsAssessment.contains("none");
    }

    private String getPreTestTbOutcomeFromDetails() {
        if (details == null || details.isEmpty()) {
            return null;
        }

        String outcomeFromExactKey = getVisitDetailValue(details.get(FIELD_TB_SCREENING_OUTCOME));
        if (StringUtils.isNotBlank(outcomeFromExactKey)) {
            return outcomeFromExactKey;
        }

        for (Map.Entry<String, List<VisitDetail>> entry : details.entrySet()) {
            if (entry.getKey() != null && entry.getKey().contains(FIELD_TB_SCREENING_OUTCOME)) {
                String outcome = getVisitDetailValue(entry.getValue());
                if (StringUtils.isNotBlank(outcome)) {
                    return outcome;
                }
            }
        }

        return null;
    }

    private String getVisitDetailValue(List<VisitDetail> visitDetails) {
        if (visitDetails == null || visitDetails.isEmpty()) {
            return null;
        }

        for (VisitDetail visitDetail : visitDetails) {
            String value = JsonFormUtils.getValue(visitDetail);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }

    private String getLatestTbScreeningOutcome() {
        try {
            if (memberObject == null || StringUtils.isBlank(memberObject.getBaseEntityId())) {
                return null;
            }
            return HtsDao.getLatestTbScreeningOutcome(memberObject.getBaseEntityId());
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
    }

    private String getTbSymptomsAssessment() {
        try {
            if (memberObject == null || StringUtils.isBlank(memberObject.getBaseEntityId())) {
                return null;
            }
            return HtsDao.getTbSymptomsAssessment(memberObject.getBaseEntityId());
        } catch (Exception e) {
            Timber.e(e);
            return null;
        }
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
            referralToPreventionServicesProvided = JsonFormUtils.getValue(jsonObject, FIELD_PREVENTIVE_SERVICES);
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
        if (StringUtils.isNotBlank(referralToPreventionServicesProvided)) {
            return BaseHtsVisitAction.Status.COMPLETED;
        }
        return BaseHtsVisitAction.Status.PENDING;
    }

    @Override
    public void onPayloadReceived(BaseHtsVisitAction baseHtsVisitAction) {
        //overridden
    }

}
