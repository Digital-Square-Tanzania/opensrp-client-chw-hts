package org.smartregister.chw.hts.actionhelper;

import static org.smartregister.client.utils.constants.JsonFormConstants.JSON_FORM_KEY.GLOBAL;
import static org.smartregister.client.utils.constants.JsonFormConstants.VALUE;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.domain.VisitDetail;
import org.smartregister.chw.hts.model.BaseHtsVisitAction;
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.chw.hts.util.HtsVisitsUtil;
import org.smartregister.chw.hts.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public abstract class HivFirstHivTestActionHelper implements BaseHtsVisitAction.HtsVisitActionHelper {

    private final String clientType;
    protected String firstHivTestResults;
    protected String syphilisTestResults;
    protected String typeOfTestUsed;
    protected String jsonPayload;
    protected Context context;
    protected MemberObject memberObject;

    public HivFirstHivTestActionHelper(Context context, MemberObject memberObject, String clientType) {
        this.context = context;
        this.memberObject = memberObject;
        this.clientType = clientType;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
    }

    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            JSONObject global = jsonObject.getJSONObject(GLOBAL);
            global.put("client_type", clientType);
            return jsonObject.toString();
        } catch (Exception e) {
            Timber.e(e);
        }

        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            firstHivTestResults = JsonFormUtils.getValue(jsonObject, "test_result");
            syphilisTestResults = JsonFormUtils.getValue(jsonObject, "syphilis_test_results");
            typeOfTestUsed = JsonFormUtils.getValue(jsonObject, "type_of_test_kit_used");
            processFirstHivTestResults(firstHivTestResults);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    public abstract void processFirstHivTestResults(String firstHivTestResults);

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
        if (StringUtils.isNotBlank(firstHivTestResults) && (StringUtils.isNotBlank(syphilisTestResults) || (StringUtils.isBlank(syphilisTestResults) && typeOfTestUsed.equalsIgnoreCase("bioline")))) {
            try {
                JSONObject form = new JSONObject(jsonPayload);
                JSONArray fields = form.getJSONObject(JsonFormConstants.STEP1).getJSONArray(org.smartregister.util.JsonFormUtils.FIELDS);
                JSONObject preTestServicesCompletionStatus = JsonFormUtils.getFieldJSONObject(fields, "hts_first_hiv_test_completion_status");
                preTestServicesCompletionStatus.put(VALUE, HtsVisitsUtil.Complete);

                if (firstHivTestResults.equalsIgnoreCase(Constants.HIV_TEST_RESULTS.NON_REACTIVE)) {
                    fields.put(JsonFormUtils.generateFinalHivTestResults(Constants.HIV_TEST_RESULTS.NEGATIVE));
                }
                return form.toString();
            } catch (Exception e) {
                Timber.e(e);
            }
        }
        return null;
    }

    @Override
    public String evaluateSubTitle() {
        return null;
    }

    @Override
    public BaseHtsVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isNotBlank(firstHivTestResults)) {
            return BaseHtsVisitAction.Status.COMPLETED;
        } else if (StringUtils.isNotBlank(typeOfTestUsed)) {
            return BaseHtsVisitAction.Status.PARTIALLY_COMPLETED;
        }
        return BaseHtsVisitAction.Status.PENDING;
    }

    @Override
    public void onPayloadReceived(BaseHtsVisitAction baseHtsVisitAction) {
        //overridden
    }

}
