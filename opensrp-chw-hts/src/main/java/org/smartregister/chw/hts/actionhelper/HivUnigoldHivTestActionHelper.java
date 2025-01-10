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
import org.smartregister.chw.hts.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public abstract class HivUnigoldHivTestActionHelper implements BaseHtsVisitAction.HtsVisitActionHelper {

    protected String unigoldHivTestResults;

    protected String jsonPayload;

    protected Context context;

    protected MemberObject memberObject;

    private final String clientType;

    private final String visitType;


    public HivUnigoldHivTestActionHelper(Context context, MemberObject memberObject, String clientType, String visitType) {
        this.context = context;
        this.memberObject = memberObject;
        this.clientType = clientType;
        this.visitType = visitType;
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
            global.put("visit_type", visitType);
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
            unigoldHivTestResults = JsonFormUtils.getValue(jsonObject, "hts_unigold_hiv_test_result");
            processUnigoldHivTestResults(unigoldHivTestResults);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    public abstract void processUnigoldHivTestResults(String unigoldHivTestResults);

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
        if (StringUtils.isNotBlank(unigoldHivTestResults)) {
            try {
                JSONObject form = new JSONObject(jsonPayload);
                JSONArray fields = form.getJSONObject(JsonFormConstants.STEP1).getJSONArray(org.smartregister.util.JsonFormUtils.FIELDS);
                if (unigoldHivTestResults.equalsIgnoreCase(Constants.HIV_TEST_RESULTS.REACTIVE)) {
                    fields.put(JsonFormUtils.generateFinalHivTestResults(Constants.HIV_TEST_RESULTS.POSITIVE));
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
        if (StringUtils.isNotBlank(unigoldHivTestResults)) {
            return BaseHtsVisitAction.Status.COMPLETED;
        }
        return BaseHtsVisitAction.Status.PENDING;
    }

    @Override
    public void onPayloadReceived(BaseHtsVisitAction baseHtsVisitAction) {
        //overridden
    }

}
