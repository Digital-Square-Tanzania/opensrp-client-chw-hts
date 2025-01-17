package org.smartregister.chw.hts.util;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.chw.hts.HtsLibrary;
import org.smartregister.chw.hts.domain.Visit;
import org.smartregister.chw.hts.repository.VisitDetailsRepository;
import org.smartregister.chw.hts.repository.VisitRepository;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.repository.AllSharedPreferences;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import timber.log.Timber;

public class HtsVisitsUtil extends VisitUtils {

    public static String Complete = "complete";
    public static String Pending = "pending";
    public static String Ongoing = "ongoing";

    public static void processVisits() throws Exception {
        processVisits(HtsLibrary.getInstance().visitRepository(), HtsLibrary.getInstance().visitDetailsRepository());
    }

    private static void processVisits(VisitRepository visitRepository, VisitDetailsRepository visitDetailsRepository) throws Exception {
        List<Visit> visits = visitRepository.getAllUnSynced();
        List<Visit> prepFollowupVisit = new ArrayList<>();

        for (Visit v : visits) {
            Date updatedAtDate = new Date(v.getUpdatedAt().getTime());
            int daysDiff = TimeUtils.getElapsedDays(updatedAtDate);
            if (daysDiff > 1) {
                if (v.getVisitType().equalsIgnoreCase(Constants.EVENT_TYPE.HTS_SERVICES) && getHtsVisitStatus(v).equals(Complete)) {
                    prepFollowupVisit.add(v);

                    List<Visit> childVisits = HtsLibrary.getInstance().visitRepository().getChildEvents(v.getVisitId());
                    prepFollowupVisit.addAll(childVisits);
                }
            }
        }
        if (!prepFollowupVisit.isEmpty()) {
            processVisits(prepFollowupVisit, visitRepository, visitDetailsRepository);
            for (Visit v : prepFollowupVisit) {
//                if (shouldCreateCloseVisitEvent(v)) {
//                    createCancelledEvent(v.getJson());
//                }
            }
        }

    }

    private static void createCancelledEvent(String json) throws Exception {
        Event baseEvent = new Gson().fromJson(json, Event.class);
        baseEvent.setFormSubmissionId(UUID.randomUUID().toString());
        AllSharedPreferences allSharedPreferences = HtsLibrary.getInstance().context().allSharedPreferences();
        NCUtils.addEvent(allSharedPreferences, baseEvent);
        NCUtils.startClientProcessing();
    }

    //TODO refactor implementation for computing VISIT Status
    public static String getHtsVisitStatus(Visit lastVisit) {
        HashMap<String, Boolean> completionObject = new HashMap<>();
        try {
            JSONObject jsonObject = new JSONObject(lastVisit.getJson());
            JSONArray obs = jsonObject.getJSONArray("obs");

            completionObject.put("isPreTestServicesDone", computeCompletionStatusForAction(obs, "pre_test_services_completion_status"));

            List<Visit> childVisits = HtsLibrary.getInstance().visitRepository().getChildEvents(lastVisit.getVisitId());
            if (childVisits != null && !childVisits.isEmpty()) {
                for (Visit visit : childVisits) {
                    if (visit.getVisitType().contains(Constants.EVENT_TYPE.HTS_FIRST_HIV_TEST)) {
                        JSONObject childVisitJsonObject = new JSONObject(visit.getJson());
                        JSONArray childVisitObs = childVisitJsonObject.getJSONArray("obs");

                        if (computeCompletionStatusForAction(childVisitObs, "hts_first_hiv_test_completion_status")) {
                            completionObject.put("isFirstHivTestDone", true);
                            break;
                        }
                    }
                    completionObject.put("isFirstHivTestDone", false);
                }
            }

            if (checkIfSecondTestShouldBePerformed(obs)) {
                if (childVisits != null && !childVisits.isEmpty()) {
                    for (Visit visit : childVisits) {
                        if (visit.getVisitType().contains(Constants.EVENT_TYPE.HTS_SECOND_HIV_TEST)) {
                            JSONObject childVisitJsonObject = new JSONObject(visit.getJson());
                            JSONArray childVisitObs = childVisitJsonObject.getJSONArray("obs");
                            if (computeCompletionStatusForAction(childVisitObs, "hts_second_hiv_test_completion_status")) {
                                completionObject.put("isSecondHivTestDone", true);
                                break;
                            }
                        }
                        completionObject.put("isSecondHivTestDone", false);
                    }
                }
            }

        } catch (Exception e) {
            Timber.e(e);
        }
        return getActionStatus(completionObject);
    }


    public static String getActionStatus(Map<String, Boolean> checkObject) {
        for (Map.Entry<String, Boolean> entry : checkObject.entrySet()) {
            if (entry.getValue()) {
                if (checkObject.containsValue(false)) {
                    return Ongoing;
                }
                return Complete;
            }
        }
        return Pending;
    }

    public static boolean computeCompletionStatus(JSONArray obs, String checkString) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase(checkString)) {
                return true;
            }
        }
        return false;
    }

    public static boolean computeCompletionStatusForAction(JSONArray obs, String checkString) throws JSONException {
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase(checkString)) {
                String status = checkObj.getJSONArray("values").getString(0);
                return status.equalsIgnoreCase("complete");
            }
        }
        return false;
    }

    public static void manualProcessVisit(Visit visit) throws Exception {
        List<Visit> manualProcessedVisits = new ArrayList<>();
        VisitDetailsRepository visitDetailsRepository = HtsLibrary.getInstance().visitDetailsRepository();
        VisitRepository visitRepository = HtsLibrary.getInstance().visitRepository();
        manualProcessedVisits.add(visit);

        List<Visit> childVisits = HtsLibrary.getInstance().visitRepository().getChildEvents(visit.getVisitId());
        manualProcessedVisits.addAll(childVisits);

        processVisits(manualProcessedVisits, visitRepository, visitDetailsRepository);
    }

    public static boolean checkIfSecondTestShouldBePerformed(JSONArray obs) throws JSONException {
        String firstTestResults = "";
        int size = obs.length();
        for (int i = 0; i < size; i++) {
            JSONObject checkObj = obs.getJSONObject(i);
            if (checkObj.getString("fieldCode").equalsIgnoreCase("hts_first_hiv_test_result")) {
                JSONArray values = checkObj.getJSONArray("values");
                firstTestResults = values.getString(0);
                break;
            }
        }
        return firstTestResults.equalsIgnoreCase("reactive");
    }


}
