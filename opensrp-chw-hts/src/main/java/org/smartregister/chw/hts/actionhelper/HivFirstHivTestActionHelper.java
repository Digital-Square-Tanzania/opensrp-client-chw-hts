package org.smartregister.chw.hts.actionhelper;

import static org.smartregister.client.utils.constants.JsonFormConstants.JSON_FORM_KEY.GLOBAL;
import static org.smartregister.client.utils.constants.JsonFormConstants.VALUE;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.domain.VisitDetail;
import org.smartregister.chw.hts.model.BaseHtsVisitAction;
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
            firstHivTestResults = JsonFormUtils.getValue(jsonObject, "hts_first_hiv_test_result");
            syphilisTestResults = JsonFormUtils.getValue(jsonObject, "hts_syphilis_test_results");
            typeOfTestUsed = JsonFormUtils.getValue(jsonObject, "hts_type_of_test_used");
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
        if (StringUtils.isNotBlank(firstHivTestResults) && (StringUtils.isNotBlank(syphilisTestResults) || (StringUtils.isNotBlank(syphilisTestResults) && typeOfTestUsed.equalsIgnoreCase("bioline")))) {
            try {
                JSONObject form = new JSONObject(jsonPayload);
                JSONObject preTestServicesCompletionStatus = JsonFormUtils.getFieldJSONObject(form.getJSONObject(JsonFormConstants.STEP1).getJSONArray(org.smartregister.util.JsonFormUtils.FIELDS), "hts_first_hiv_test_completion_status");
                preTestServicesCompletionStatus.put(VALUE, true);
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
