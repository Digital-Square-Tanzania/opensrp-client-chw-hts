package org.smartregister.chw.hts.actionhelper;

import static org.smartregister.client.utils.constants.JsonFormConstants.FIELDS;
import static org.smartregister.client.utils.constants.JsonFormConstants.STEP1;
import static org.smartregister.client.utils.constants.JsonFormConstants.VALUE;

import android.content.Context;

import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hts.dao.HtsDao;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.domain.VisitDetail;
import org.smartregister.chw.hts.model.BaseHtsVisitAction;
import org.smartregister.chw.hts.util.HtsVisitsUtil;
import org.smartregister.chw.hts.util.JsonFormUtils;

import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class PreTestServicesActionHelper implements BaseHtsVisitAction.HtsVisitActionHelper {

    protected String hasPreTestCounsellingBeenProvided;

    protected String jsonPayload;

    protected Context context;

    protected MemberObject memberObject;


    public PreTestServicesActionHelper(Context context, MemberObject memberObject) {
        this.context = context;
        this.memberObject = memberObject;
    }

    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
    }

    @Override
    public String getPreProcessed() {
        try {
            String tbScreening = HtsDao.getTbSymptomsAssessment(memberObject.getBaseEntityId());
            if (StringUtils.isNotBlank(tbScreening) && !tbScreening.contains("none")) {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                JSONArray fieldsArrayStep1 = jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS);
                JSONObject tbScreeningOutcome = JsonFormUtils.getFieldJSONObject(fieldsArrayStep1, "hts_clients_tb_screening_outcome");
                tbScreeningOutcome.put(VALUE, "tb_suspect");
                return jsonObject.toString();
            }
        } catch (Exception e) {
            Timber.e(e);
        }
        return null;
    }

    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            hasPreTestCounsellingBeenProvided = JsonFormUtils.getValue(jsonObject, "hts_has_pre_test_counselling_been_provided");
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
        if (StringUtils.isNotBlank(hasPreTestCounsellingBeenProvided)) {
            try {
                JSONObject form = new JSONObject(jsonPayload);
                JSONObject preTestServicesCompletionStatus = JsonFormUtils.getFieldJSONObject(form.getJSONObject(JsonFormConstants.STEP1).getJSONArray(org.smartregister.util.JsonFormUtils.FIELDS), "pre_test_services_completion_status");
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
        if (StringUtils.isNotBlank(hasPreTestCounsellingBeenProvided)) {
            return BaseHtsVisitAction.Status.COMPLETED;
        }
        return BaseHtsVisitAction.Status.PENDING;
    }

    @Override
    public void onPayloadReceived(BaseHtsVisitAction baseHtsVisitAction) {
        //overridden
    }

}
