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
import org.smartregister.chw.hts.util.HtsVisitsUtil;
import org.smartregister.chw.hts.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public abstract class HivSecondHivTestActionHelper implements BaseHtsVisitAction.HtsVisitActionHelper {

    private final String clientType;
    protected String secondHivTestResults;
    protected String secondTestKitBatchNumber;
    protected String jsonPayload;
    protected Context context;
    protected MemberObject memberObject;


    public HivSecondHivTestActionHelper(Context context, MemberObject memberObject, String clientType) {
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
            secondHivTestResults = JsonFormUtils.getValue(jsonObject, "test_result");
            secondTestKitBatchNumber = JsonFormUtils.getValue(jsonObject, "test_kit_batch_number");
            processSecondHivTestResults(secondHivTestResults);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    public abstract void processSecondHivTestResults(String secondHivTestResults);

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
        if (StringUtils.isNotBlank(secondHivTestResults)) {
            try {
                JSONObject form = new JSONObject(jsonPayload);
                JSONObject preTestServicesCompletionStatus = JsonFormUtils.getFieldJSONObject(form.getJSONObject(JsonFormConstants.STEP1).getJSONArray(org.smartregister.util.JsonFormUtils.FIELDS), "hts_second_hiv_test_completion_status");
                preTestServicesCompletionStatus.put(VALUE, HtsVisitsUtil.Complete);
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
        if (StringUtils.isNotBlank(secondHivTestResults)) {
            return BaseHtsVisitAction.Status.COMPLETED;
        } else if (StringUtils.isNotBlank(secondTestKitBatchNumber)) {
            return BaseHtsVisitAction.Status.PARTIALLY_COMPLETED;
        }
        return BaseHtsVisitAction.Status.PENDING;
    }

    @Override
    public void onPayloadReceived(BaseHtsVisitAction baseHtsVisitAction) {
        //overridden
    }

}
