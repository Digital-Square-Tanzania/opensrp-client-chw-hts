package org.smartregister.chw.hts.interactor;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import com.google.gson.Gson;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.smartregister.chw.hts.HtsLibrary;
import org.smartregister.chw.hts.contract.BaseSkeletonVisitContract;
import org.smartregister.chw.hts.dao.SkeletonDao;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.domain.Visit;
import org.smartregister.chw.hts.domain.VisitDetail;
import org.smartregister.chw.hts.model.BaseSkeletonVisitAction;
import org.smartregister.chw.hts.repository.VisitRepository;
import org.smartregister.chw.hts.util.AppExecutors;
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.chw.hts.util.NCUtils;
import org.smartregister.chw.hts.util.SkeletonJsonFormUtils;
import org.smartregister.chw.hts.util.VisitUtils;
import org.smartregister.clientandeventmodel.Event;
import org.smartregister.clientandeventmodel.Obs;
import org.smartregister.clientandeventmodel.User;
import org.smartregister.domain.SyncStatus;
import org.smartregister.repository.AllSharedPreferences;
import org.smartregister.sync.helper.ECSyncHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import timber.log.Timber;

public class BaseSkeletonVisitInteractor implements BaseSkeletonVisitContract.Interactor {

    protected final LinkedHashMap<String, BaseSkeletonVisitAction> actionList = new LinkedHashMap<>();
    private final ECSyncHelper syncHelper;
    protected AppExecutors appExecutors;
    protected Map<String, List<VisitDetail>> details = null;
    protected String visitType;
    protected Context context;
    protected MemberObject memberObject;

    @VisibleForTesting
    public BaseSkeletonVisitInteractor(AppExecutors appExecutors, ECSyncHelper syncHelper) {
        this.appExecutors = appExecutors;
        this.syncHelper = syncHelper;

    }

    public BaseSkeletonVisitInteractor() {
        this(new AppExecutors(), HtsLibrary.getInstance().getEcSyncHelper());
    }
    public BaseSkeletonVisitInteractor(String visitType) {
        this(new AppExecutors(), HtsLibrary.getInstance().getEcSyncHelper());
        this.visitType = visitType;
    }

    protected String getCurrentVisitType() {
        if(StringUtils.isNotBlank(visitType)){
            return visitType;
        }
        return Constants.EVENT_TYPE.SKELETON_ENROLLMENT;
    }

    @Override
    public void reloadMemberDetails(String memberID, String profileType, BaseSkeletonVisitContract.InteractorCallBack callBack) {
        memberObject = getMemberClient(memberID, profileType);
        if (memberObject != null) {
            final Runnable runnable = () -> {
                appExecutors.mainThread().execute(() -> callBack.onMemberDetailsReloaded(memberObject));
            };
            appExecutors.diskIO().execute(runnable);
        }
    }

    /**
     * Default if profile type is not provided is SKELETON/PrEP member
     *
     * @param memberID    unique identifier for the user
     * @param profileType profile type being used
     * @return MemberObject wrapper for the user's data
     */
    @Override
    public MemberObject getMemberClient(String memberID, String profileType) {
        return SkeletonDao.getMember(memberID);
    }

    @Override
    public void saveRegistration(String jsonString, boolean isEditMode, BaseSkeletonVisitContract.InteractorCallBack callBack) {
        Timber.v("saveRegistration");
    }

    @Override
    public void calculateActions(final BaseSkeletonVisitContract.View view, MemberObject memberObject, final BaseSkeletonVisitContract.InteractorCallBack callBack) {
        context = view.getContext();
        getDetailsOnEdit(view, memberObject);

        populateActionList(callBack);
    }

    protected void getDetailsOnEdit(BaseSkeletonVisitContract.View view, MemberObject memberObject) {
        if (view.getEditMode()) {
            Visit lastVisit = HtsLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), getCurrentVisitType());

            if (lastVisit != null) {
                details = VisitUtils.getVisitGroups(HtsLibrary.getInstance().visitDetailsRepository().getVisits(lastVisit.getVisitId()));
            }
        }
    }

    protected BaseSkeletonVisitAction.Builder getBuilder(String title) {
        return new BaseSkeletonVisitAction.Builder(context, title);
    }

    protected void populateActionList(BaseSkeletonVisitContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {
            try {
                evaluateSampleAction(details);
            } catch (BaseSkeletonVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    private void evaluateSampleAction(Map<String, List<VisitDetail>> details) throws BaseSkeletonVisitAction.ValidationException {

        BaseSkeletonVisitAction ba = getBuilder("Sample Action")
                .withSubtitle("")
                .withOptional(false)
                .withFormName("anc")
                .build();
        actionList.put("Sample Action", ba);

    }


    @Override
    public void submitVisit(final boolean editMode, final String memberID, final Map<String, BaseSkeletonVisitAction> map, final BaseSkeletonVisitContract.InteractorCallBack callBack) {
        final Runnable runnable = () -> {
            String results = null;
            try {
                results = submitVisit(editMode, memberID, map, "");
            } catch (Exception e) {
                Timber.e(e);
            }

            String finalResults = results;
            appExecutors.mainThread().execute(() ->
                    callBack.onSubmitted(finalResults));
        };

        appExecutors.diskIO().execute(runnable);
    }

    protected String submitVisit(final boolean editMode,
                                 final String memberID,
                                 final Map<String,
                                 BaseSkeletonVisitAction> map,
                                 String parentEventType) throws Exception {
        // create a map of the different types

        Map<String, BaseSkeletonVisitAction> externalVisits = new HashMap<>();
        Map<String, String> combinedJsons = new HashMap<>();
        String payloadType = null;
        String payloadDetails = null;

        // aggregate forms to be processed
        for (Map.Entry<String, BaseSkeletonVisitAction> entry : map.entrySet()) {
            String json = entry.getValue().getJsonPayload();
            if (StringUtils.isNotBlank(json)) {
                // do not process events that are meant to be in detached mode
                // in a similar manner to the the aggregated events

                BaseSkeletonVisitAction action = entry.getValue();
                BaseSkeletonVisitAction.ProcessingMode mode = action.getProcessingMode();

                if (mode == BaseSkeletonVisitAction.ProcessingMode.SEPARATE && StringUtils.isBlank(parentEventType)) {
                    externalVisits.put(entry.getKey(), entry.getValue());
                } else {
                    combinedJsons.put(entry.getKey(), json);
                }

                payloadType = action.getPayloadType().name();
                payloadDetails = action.getPayloadDetails();
            }
        }

        String type = StringUtils.isBlank(parentEventType) ? getEncounterType() : getEncounterType();

        // persist to database
        Visit visit = saveVisit(editMode, memberID, type, combinedJsons, parentEventType);
        if (visit != null) {
            saveVisitDetails(visit, payloadType, payloadDetails);
            processExternalVisits(visit, externalVisits, memberID);
        }

        if (HtsLibrary.isSubmitOnSave()) {
            List<Visit> visits = new ArrayList<>(1);
            visits.add(visit);
            VisitUtils.processVisits(visits, HtsLibrary.getInstance().visitRepository(), HtsLibrary.getInstance().visitDetailsRepository());

            Context context = HtsLibrary.getInstance().context().applicationContext();

        }
        return visit.getJson();
    }

    /**
     * recursively persist visits to the db
     *
     * @param visit
     * @param externalVisits
     * @param memberID
     * @throws Exception
     */
    protected void processExternalVisits(Visit visit, Map<String, BaseSkeletonVisitAction> externalVisits, String memberID) throws Exception {
        if (visit != null && !externalVisits.isEmpty()) {
            for (Map.Entry<String, BaseSkeletonVisitAction> entry : externalVisits.entrySet()) {
                Map<String, BaseSkeletonVisitAction> subEvent = new HashMap<>();
                subEvent.put(entry.getKey(), entry.getValue());

                String subMemberID = entry.getValue().getBaseEntityID();
                if (StringUtils.isBlank(subMemberID))
                    subMemberID = memberID;

                submitVisit(false, subMemberID, subEvent, visit.getVisitType());
            }
        }
    }

    protected @Nullable Visit saveVisit(boolean editMode, String memberID, String encounterType,
                                        final Map<String, String> jsonString,
                                        String parentEventType
    ) throws Exception {

        AllSharedPreferences allSharedPreferences = HtsLibrary.getInstance().context().allSharedPreferences();

        String derivedEncounterType = StringUtils.isBlank(parentEventType) ? encounterType : "";
        Event baseEvent = SkeletonJsonFormUtils.processVisitJsonForm(allSharedPreferences, memberID, derivedEncounterType, jsonString, getTableName());

        // only tag the first event with the date
        if (StringUtils.isBlank(parentEventType)) {
            prepareEvent(baseEvent);
        } else {
            prepareSubEvent(baseEvent);
        }

        if (baseEvent != null) {
            baseEvent.setFormSubmissionId(SkeletonJsonFormUtils.generateRandomUUIDString());
            SkeletonJsonFormUtils.tagEvent(allSharedPreferences, baseEvent);

            String visitID = (editMode) ?
                    visitRepository().getLatestVisit(memberID, getEncounterType()).getVisitId() :
                    SkeletonJsonFormUtils.generateRandomUUIDString();

            // reset database
            if (editMode) {
                deleteProcessedVisit(visitID, memberID);
                deleteOldVisit(visitID);
            }

            Visit visit = NCUtils.eventToVisit(baseEvent, visitID);
            visit.setPreProcessedJson(new Gson().toJson(baseEvent));
            visit.setParentVisitID(getParentVisitEventID(visit, parentEventType));

            visitRepository().addVisit(visit);
            return visit;
        }
        return null;
    }

    protected String getParentVisitEventID(Visit visit, String parentEventType) {
        return visitRepository().getParentVisitEventID(visit.getBaseEntityId(), parentEventType, visit.getDate());
    }

    @VisibleForTesting
    public VisitRepository visitRepository() {
        return HtsLibrary.getInstance().visitRepository();
    }

    protected void deleteOldVisit(String visitID) {
        visitRepository().deleteVisit(visitID);
        HtsLibrary.getInstance().visitDetailsRepository().deleteVisitDetails(visitID);

        List<Visit> childVisits = visitRepository().getChildEvents(visitID);
        for (Visit v : childVisits) {
            visitRepository().deleteVisit(v.getVisitId());
            HtsLibrary.getInstance().visitDetailsRepository().deleteVisitDetails(v.getVisitId());
        }
    }


    protected void deleteProcessedVisit(String visitID, String baseEntityId) {
        // check if the event
        AllSharedPreferences allSharedPreferences = HtsLibrary.getInstance().context().allSharedPreferences();
        Visit visit = visitRepository().getVisitByVisitId(visitID);
        if (visit == null || !visit.getProcessed()) return;
        //TODO: implement if needed
    }

    protected void deleteSavedEvent(AllSharedPreferences allSharedPreferences, String baseEntityId, String eventId, String formSubmissionId, String type) {
        Event event = (Event) new Event()
                .withBaseEntityId(baseEntityId)
                .withEventDate(new Date())
                .withEventType(Constants.EVENT_TYPE.VOID_EVENT)
                .withLocationId(SkeletonJsonFormUtils.locationId(allSharedPreferences))
                .withProviderId(allSharedPreferences.fetchRegisteredANM())
                .withEntityType(type)
                .withFormSubmissionId(formSubmissionId)
                .withVoided(true)
                .withVoider(new User(null, allSharedPreferences.fetchRegisteredANM(), null, null))
                .withVoidReason("Edited Event")
                .withDateVoided(new Date());

        event.setSyncStatus(SyncStatus.PENDING.value());

        try {
            syncHelper.addEvent(event.getBaseEntityId(), new JSONObject(SkeletonJsonFormUtils.gson.toJson(event)));
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    protected void saveVisitDetails(Visit visit, String payloadType, String payloadDetails) {
        if (visit.getVisitDetails() == null) return;

        for (Map.Entry<String, List<VisitDetail>> entry : visit.getVisitDetails().entrySet()) {
            if (entry.getValue() != null) {
                for (VisitDetail d : entry.getValue()) {
                    d.setPreProcessedJson(payloadDetails);
                    d.setPreProcessedType(payloadType);
                    HtsLibrary.getInstance().visitDetailsRepository().addVisitDetails(d);
                }
            }
        }
    }

    /**
     * Injects implementation specific changes to the event
     *
     * @param baseEvent
     */
    protected void prepareEvent(Event baseEvent) {
        if (baseEvent != null) {
            // add hts_visit_date date obs and last
            List<Object> list = new ArrayList<>();
            list.add(new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date()));
            baseEvent.addObs(new Obs("concept", "text", "hts_visit_date", "",
                    list, new ArrayList<>(), null, "hts_visit_date"));
        }
    }

    /**
     * injects additional meta data to the event
     *
     * @param baseEvent
     */
    protected void prepareSubEvent(Event baseEvent) {
        Timber.v("You can add information to sub events");
    }

    protected String getEncounterType() {
        return Constants.EVENT_TYPE.SKELETON_ENROLLMENT;
    }

    protected String getTableName() {
        return Constants.TABLES.SKELETON_ENROLLMENT;
    }
}
