package org.smartregister.chw.hts.actionhelper;

import static org.smartregister.client.utils.constants.JsonFormConstants.EDITABLE;
import static org.smartregister.client.utils.constants.JsonFormConstants.FIELDS;
import static org.smartregister.client.utils.constants.JsonFormConstants.JSON_FORM_KEY.GLOBAL;
import static org.smartregister.client.utils.constants.JsonFormConstants.READ_ONLY;
import static org.smartregister.client.utils.constants.JsonFormConstants.STEP1;
import static org.smartregister.client.utils.constants.JsonFormConstants.VALUE;

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

/**
 * Abstract helper class for processing different types of HTS (HIV Testing Services) visits.
 * Provides default implementations for `BaseHtsVisitAction.HtsVisitActionHelper` methods and
 * an abstract method for processing specific visit types.
 * <p>
 * This class handles:
 * - Managing of client visit type
 * - Processing and evaluating payloads
 * <p>
 * Subclasses must implement the abstract `processVisitType` method to handle specific visit types.
 */
public abstract class VisitTypeActionHelper implements BaseHtsVisitAction.HtsVisitActionHelper {

    protected String clientType;

    protected String visitType;

    protected String jsonPayload;

    protected Context context;

    protected MemberObject memberObject;


    /**
     * Initializes a new instance of `VisitTypeActionHelper`.
     *
     * @param context      The Android context for accessing resources and system services.
     * @param memberObject The member object containing client-specific details for the visit.
     */
    public VisitTypeActionHelper(Context context, MemberObject memberObject) {
        this.context = context;
        this.memberObject = memberObject;
    }

    /**
     * Handles the JSON form payload when it is loaded.
     *
     * @param jsonPayload The JSON payload string for the form.
     * @param context     The Android context.
     * @param map         A map containing visit details for the current context.
     */
    @Override
    public void onJsonFormLoaded(String jsonPayload, Context context, Map<String, List<VisitDetail>> map) {
        this.jsonPayload = jsonPayload;
    }

    /**
     * Retrieves the JSON payload before processing.
     *
     * @return The unprocessed JSON payload string.
     */
    @Override
    public String getPreProcessed() {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            JSONObject global = jsonObject.optJSONObject(GLOBAL);
            if (global == null) {
                global = new JSONObject();
                jsonObject.put(GLOBAL, global);
            }
            global.put("sex", memberObject.getGender());
            global.put("age", memberObject.getAge());
            jsonPayload = jsonObject.toString();
        } catch (JSONException e) {
            Timber.e(e);
        }

        /**
         * Handle visit type
         */
        String hasTheClientEverTestedForHiv = HtsDao.getHasTheClientEverTestedForHiv(memberObject.getBaseEntityId());

        if (HtsDao.hasAnyVisit(memberObject.getBaseEntityId())) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                JSONArray fieldsArrayStep1 = jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS);
                JSONObject visitType = JsonFormUtils.getFieldJSONObject(fieldsArrayStep1, "hts_visit_type");
                visitType.put(VALUE, "returning");
                visitType.put(READ_ONLY, true);
                visitType.put(EDITABLE, false);

                jsonPayload = jsonObject.toString();
            } catch (JSONException e) {
                Timber.e(e);
            }
        } else if ("yes".equalsIgnoreCase(hasTheClientEverTestedForHiv)) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                JSONArray fieldsArrayStep1 = jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS);
                JSONObject visitType = JsonFormUtils.getFieldJSONObject(fieldsArrayStep1, "hts_visit_type");
                visitType.put(VALUE, "returning");
                visitType.put(READ_ONLY, true);
                visitType.put(EDITABLE, false);

                jsonPayload = jsonObject.toString();
            } catch (JSONException e) {
                Timber.e(e);
            }
        } else if ("no".equalsIgnoreCase(hasTheClientEverTestedForHiv)) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                JSONArray fieldsArrayStep1 = jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS);
                JSONObject visitType = JsonFormUtils.getFieldJSONObject(fieldsArrayStep1, "hts_visit_type");
                visitType.put(VALUE, "new_client");
                visitType.put(READ_ONLY, true);
                visitType.put(EDITABLE, false);

                jsonPayload = jsonObject.toString();
            } catch (JSONException e) {
                Timber.e(e);
            }
        }


        /**
         * Handle has client recently tested with HIVST
         */
        if (HtsDao.hasRecentlyTestedWithHivst(memberObject.getBaseEntityId())) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                JSONArray fieldsArrayStep1 = jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS);
                JSONObject hasTheClientRecentlyTestedWithHivst = JsonFormUtils.getFieldJSONObject(fieldsArrayStep1, "hts_has_the_client_recently_tested_with_hivst");
                hasTheClientRecentlyTestedWithHivst.put(VALUE, "yes");
                hasTheClientRecentlyTestedWithHivst.put(READ_ONLY, true);
                hasTheClientRecentlyTestedWithHivst.put(EDITABLE, false);

                JSONObject htsPreviousHivstClientType = JsonFormUtils.getFieldJSONObject(fieldsArrayStep1, "hts_previous_hivst_client_type");
                htsPreviousHivstClientType.put(VALUE, "self");
                htsPreviousHivstClientType.put(READ_ONLY, true);
                htsPreviousHivstClientType.put(EDITABLE, false);

                //Populate kit type on form
                String kitType = HtsDao.fetchKitType(memberObject.getBaseEntityId());
                if (kitType != null) {
                    JSONObject clientKitType = JsonFormUtils.getFieldJSONObject(fieldsArrayStep1, "hts_previous_hivst_test_type");
                    clientKitType.put(VALUE, kitType);
                    clientKitType.put(READ_ONLY, true);
                    clientKitType.put(EDITABLE, false);
                }

                jsonPayload = jsonObject.toString();
            } catch (JSONException e) {
                Timber.e(e);
            }
        }

        /**
         * Handle has client type field
         * If the client is an index contact and does not have any visit then the client is an index contact
         */
        if (HtsDao.isClientAnIndexContact(memberObject.getBaseEntityId()) && !HtsDao.hasAnyVisit(memberObject.getBaseEntityId())) {
            try {
                JSONObject jsonObject = new JSONObject(jsonPayload);
                JSONArray fieldsArrayStep1 = jsonObject.getJSONObject(STEP1).getJSONArray(FIELDS);
                JSONObject hasTheClientRecentlyTestedWithHivst = JsonFormUtils.getFieldJSONObject(fieldsArrayStep1, "hts_client_type");
                hasTheClientRecentlyTestedWithHivst.put(VALUE, "index_contact");
                hasTheClientRecentlyTestedWithHivst.put(READ_ONLY, true);
                hasTheClientRecentlyTestedWithHivst.put(EDITABLE, false);

                jsonPayload = jsonObject.toString();
            } catch (JSONException e) {
                Timber.e(e);
            }
        }


        return jsonPayload;
    }

    /**
     * Processes the JSON payload after it has been received.
     * Extracts the `hts_visit_type` value and delegates processing to the abstract `processVisitType` method.
     *
     * @param jsonPayload The received JSON payload string.
     */
    @Override
    public void onPayloadReceived(String jsonPayload) {
        try {
            JSONObject jsonObject = new JSONObject(jsonPayload);
            clientType = JsonFormUtils.getValue(jsonObject, "hts_client_type");
            visitType = JsonFormUtils.getValue(jsonObject, "hts_visit_type");
            processVisitAndClientTypes(visitType, clientType);
        } catch (JSONException e) {
            Timber.e(e);
        }
    }

    /**
     * Retrieves the pre-processed schedule status.
     * Default behavior returns `DUE`.
     *
     * @return The schedule status, which is `BaseHtsVisitAction.ScheduleStatus.DUE`.
     */
    @Override
    public BaseHtsVisitAction.ScheduleStatus getPreProcessedStatus() {
        return BaseHtsVisitAction.ScheduleStatus.DUE;
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

    /**
     * Evaluates the status of the visit based on the payload.
     * If the `visitType` is not blank, returns `COMPLETED`; otherwise, returns `PENDING`.
     *
     * @return The evaluation status, either `COMPLETED` or `PENDING`.
     */
    @Override
    public BaseHtsVisitAction.Status evaluateStatusOnPayload() {
        if (StringUtils.isNotBlank(clientType)) {
            return BaseHtsVisitAction.Status.COMPLETED;
        }
        return BaseHtsVisitAction.Status.PENDING;
    }

    @Override
    public void onPayloadReceived(BaseHtsVisitAction baseHtsVisitAction) {
        //overridden
    }

    /**
     * Processes the specific HTS visit type.
     * This method is abstract and must be implemented by subclasses to handle visit type-specific logic.
     *
     * @param visitType  The type of the visit to process.
     * @param clientType The type of client.
     */
    public abstract void processVisitAndClientTypes(String visitType, String clientType);

}
