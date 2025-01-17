package org.smartregister.chw.hts.interactor;


import static org.smartregister.chw.hts.util.Constants.JSON_FORM_EXTRA.ENCOUNTER_TYPE;

import android.content.Context;

import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.smartregister.chw.hts.HtsLibrary;
import org.smartregister.chw.hts.R;
import org.smartregister.chw.hts.actionhelper.DnaPcrSampleCollectionActionHelper;
import org.smartregister.chw.hts.actionhelper.HivFirstHivTestActionHelper;
import org.smartregister.chw.hts.actionhelper.HivRepeatFirstHivTestActionHelper;
import org.smartregister.chw.hts.actionhelper.HivSecondHivTestActionHelper;
import org.smartregister.chw.hts.actionhelper.HivUnigoldHivTestActionHelper;
import org.smartregister.chw.hts.actionhelper.LinkageToPreventionServicesActionHelper;
import org.smartregister.chw.hts.actionhelper.PostTestServicesActionHelper;
import org.smartregister.chw.hts.actionhelper.PreTestServicesActionHelper;
import org.smartregister.chw.hts.actionhelper.VisitTypeActionHelper;
import org.smartregister.chw.hts.contract.BaseHtsVisitContract;
import org.smartregister.chw.hts.domain.Visit;
import org.smartregister.chw.hts.domain.VisitDetail;
import org.smartregister.chw.hts.model.BaseHtsVisitAction;
import org.smartregister.chw.hts.util.AppExecutors;
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.chw.hts.util.JsonFormUtils;
import org.smartregister.chw.hts.util.VisitUtils;
import org.smartregister.sync.helper.ECSyncHelper;
import org.smartregister.util.FormUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

/**
 * Handles the logic for HIV Testing Services (HTS) service visits.
 * This interactor manages the creation and population of HTS visit actions based on visit details.
 */
public class BaseHtsServiceVisitInteractor extends BaseHtsVisitInteractor {

    private final LinkedHashMap<String, BaseHtsVisitAction> actionList; // Stores all HTS actions for the visit
    protected BaseHtsVisitContract.InteractorCallBack callBack; // Callback interface to communicate with the presenter
    protected AppExecutors appExecutors; // Executor for managing asynchronous tasks
    private String mVisitType; // Type of the current HTS visit
    private Context mContext; // Context for accessing resources and helpers
    private String mClientType;

    /**
     * Constructor for testing, allowing injection of dependencies.
     *
     * @param appExecutors Executor for handling background and main thread tasks
     * @param HtsLibrary   Reference to the HTS Library
     * @param syncHelper   Helper for syncing data
     */
    @VisibleForTesting
    public BaseHtsServiceVisitInteractor(AppExecutors appExecutors, HtsLibrary HtsLibrary, ECSyncHelper syncHelper) {
        this.appExecutors = appExecutors;
        this.actionList = new LinkedHashMap<>();
        this.mContext = HtsLibrary.getInstance().context().applicationContext();
    }

    /**
     * Main constructor that initializes the interactor with a visit type.
     *
     * @param visitType The type of HTS visit
     */
    public BaseHtsServiceVisitInteractor(String visitType) {
        this(new AppExecutors(), HtsLibrary.getInstance(), HtsLibrary.getInstance().getEcSyncHelper());
        this.visitType = visitType;
    }

    /**
     * Gets the current visit type. If none is specified, it falls back to the parent implementation.
     *
     * @return The visit type as a string
     */
    @Override
    protected String getCurrentVisitType() {
        if (StringUtils.isNotBlank(visitType)) {
            return visitType;
        }
        return super.getCurrentVisitType();
    }

    /**
     * Populates the action list for the HTS visit.
     * This method determines the required actions for the visit based on visit details.
     *
     * @param callBack Callback to communicate with the presenter
     */
    @Override
    protected void populateActionList(BaseHtsVisitContract.InteractorCallBack callBack) {
        this.callBack = callBack;
        final Runnable runnable = () -> {
            try {
                evaluateVisitType(details);
            } catch (BaseHtsVisitAction.ValidationException e) {
                Timber.e(e);
            }

            appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
        };

        appExecutors.diskIO().execute(runnable);
    }

    /**
     * Evaluates the type of visit and determines the corresponding actions.
     *
     * @param details Map of visit details
     * @throws BaseHtsVisitAction.ValidationException If validation of actions fails
     */
    private void evaluateVisitType(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        VisitTypeActionHelper actionHelper = new VisitTypeActionHelper(mContext, memberObject) {
            @Override
            public void processVisitAndClientTypes(String visitType, String clientType) {
                try {
                    mVisitType = visitType;
                    mClientType = clientType;
                    evaluatePreTestServices(details);
                    evaluateFirstHivTest(details, 1);
                } catch (Exception e) {
                    Timber.e(e);
                }
                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };

        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_visit_type_action_title))
                .withOptional(false)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_VISIT_TYPE)
                .build();
        actionList.put(context.getString(R.string.hts_visit_type_action_title), action);

        JSONObject form = null;
        try {
            form = FormUtils.getInstance(mContext).getFormJson(Constants.FORMS.HTS_VISIT_TYPE);
        } catch (Exception e) {
            Timber.e(e);
        }
        if (details != null && form != null) {
            JsonFormUtils.populateForm(form, details);
            action.setJsonPayload(form.toString());
        }
    }

    /**
     * Evaluates and initializes the pre-test services action for the HTS visit.
     * This includes providing pre-test counseling and other preparatory steps for HIV testing.
     *
     * @param details A map of visit details, containing key-value pairs of information about the visit.
     * @throws BaseHtsVisitAction.ValidationException If the action validation fails during initialization.
     */
    private void evaluatePreTestServices(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        PreTestServicesActionHelper actionHelper = new PreTestServicesActionHelper(mContext, memberObject);
        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_pre_test_services_action_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_PRE_TEST_SERVICES)
                .build();
        actionList.put(context.getString(R.string.hts_pre_test_services_action_title), action);
    }

    /**
     * Evaluates and initializes the first HIV test action for the HTS visit.
     * The method handles both the first test and repeat tests if necessary based on test results
     * (in-case there was wastage or invalid results in the previous test).
     *
     * @param details      A map of visit details, containing key-value pairs of information about the visit.
     * @param repeatNumber The current repeat test number; defaults to 1 for the initial test.
     * @throws BaseHtsVisitAction.ValidationException If the action validation fails during initialization.
     */
    private void evaluateFirstHivTest(Map<String, List<VisitDetail>> details, final int repeatNumber) throws BaseHtsVisitAction.ValidationException {
        HivFirstHivTestActionHelper actionHelper = new HivFirstHivTestActionHelper(mContext, memberObject, mClientType) {
            @Override
            public void processFirstHivTestResults(String firstHivTestResults) {
                try {
                    if (firstHivTestResults.equalsIgnoreCase(Constants.HIV_TEST_RESULTS.REACTIVE)) {
                        //removing extra actions that are not required in this scenario
                        removeCommonActions();
                        removeExtraRepeatActions(R.string.hts_repeate_of_first_hiv_test_action_title, repeatNumber);
                        actionList.remove(mContext.getString(R.string.hts_dna_pcr_sample_collection_action_title));

                        evaluateSecondHivTest(details, 1);
                    } else if (firstHivTestResults.equalsIgnoreCase(Constants.HIV_TEST_RESULTS.NON_REACTIVE)) {
                        //removing extra actions that are not required in this scenario
                        actionList.remove(mContext.getString(R.string.hts_second_hiv_test_action_title));
                        actionList.remove(mContext.getString(R.string.hts_unigold_hiv_test_action_title));
                        removeExtraRepeatActions(R.string.hts_repeate_of_first_hiv_test_action_title, repeatNumber);

                        if (StringUtils.isNotBlank(mClientType) && mClientType.equalsIgnoreCase("verification")) {
                            evaluateDnaPcrSampleCollection(details);
                        } else {
                            evaluatePostTestServices(details);
                            evaluateLinkageToPreventionServices(details);
                        }
                    } else {
                        //removing extra actions that are not required in this scenario
                        removeCommonActions();
                        actionList.remove(mContext.getString(R.string.hts_second_hiv_test_action_title));
                        actionList.remove(mContext.getString(R.string.hts_unigold_hiv_test_action_title));
                        actionList.remove(mContext.getString(R.string.hts_dna_pcr_sample_collection_action_title));

                        if (StringUtils.isNotBlank(firstHivTestResults))
                            evaluateFirstHivTest(details, repeatNumber + 1);


                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };

        String actionTitle = context.getString(R.string.hts_first_hiv_test_action_title);
        if (repeatNumber != 1) {
            actionTitle = String.format(context.getString(R.string.hts_repeate_of_first_hiv_test_action_title), repeatNumber);
        }

        Map<String, List<VisitDetail>> mDetails = null;
        for (Visit visit : childVisits) {
            if (visit.getVisitType().equalsIgnoreCase(String.format(context.getString(R.string.hts_first_hiv_test_action_entity_type), repeatNumber))) {
                mDetails = VisitUtils.getVisitGroups(HtsLibrary.getInstance().visitDetailsRepository().getVisits(visit.getVisitId()));
            }
        }

        try {
            JSONObject firstHivTest = FormUtils.getInstance(mContext).getFormJson(Constants.FORMS.HTS_FIRST_HIV_TEST);
            firstHivTest.put(ENCOUNTER_TYPE, String.format(context.getString(R.string.hts_first_hiv_test_action_entity_type), repeatNumber));

            BaseHtsVisitAction action = getBuilder(actionTitle)
                    .withOptional(true)
                    .withJsonPayload(firstHivTest.toString())
                    .withHelper(actionHelper)
                    .withProcessingMode(BaseHtsVisitAction.ProcessingMode.SEPARATE)
                    .withFormName(Constants.FORMS.HTS_FIRST_HIV_TEST)
                    .build();
            actionList.put(actionTitle, action);


            if (mDetails != null) {
                JsonFormUtils.populateForm(firstHivTest, mDetails);
                action.setJsonPayload(firstHivTest.toString());
            }
        } catch (Exception e) {
            Timber.e(e);
        }


    }

    /**
     * Evaluates and initializes the second HIV test action for the HTS visit.
     * The second test is conducted if the first test yields reactive results.
     *
     * @param details      A map of visit details, containing key-value pairs of information about the visit.
     * @param repeatNumber The current repeat test number; defaults to 1 for the initial second test.
     * @throws BaseHtsVisitAction.ValidationException If the action validation fails during initialization.
     */
    private void evaluateSecondHivTest(Map<String, List<VisitDetail>> details, int repeatNumber) throws BaseHtsVisitAction.ValidationException {
        HivSecondHivTestActionHelper actionHelper = new HivSecondHivTestActionHelper(mContext, memberObject, mClientType) {
            @Override
            public void processSecondHivTestResults(String secondHivTestResults) {
                try {
                    if (secondHivTestResults.equalsIgnoreCase(Constants.HIV_TEST_RESULTS.REACTIVE)) {
                        evaluateUnigoldHivTest(details, 1);

                        //removing extra actions that are not required in this scenario
                        removeExtraRepeatActions(R.string.hts_repeate_of_second_hiv_test_action_title, repeatNumber);
                        actionList.remove(mContext.getString(R.string.hts_repeate_of_first_hiv_test_title));
                        actionList.remove(mContext.getString(R.string.hts_dna_pcr_sample_collection_action_title));
                    } else if (secondHivTestResults.equalsIgnoreCase(Constants.HIV_TEST_RESULTS.NON_REACTIVE)) {
                        if (StringUtils.isNotBlank(mClientType) && mClientType.equalsIgnoreCase("verification")) {
                            evaluateDnaPcrSampleCollection(details);
                        } else {
                            evaluateRepeatOfFirstHivTest(details);
                        }

                        //removing extra actions that are not required in this scenario
                        actionList.remove(mContext.getString(R.string.hts_unigold_hiv_test_action_title));
                        removeExtraRepeatActions(R.string.hts_repeate_of_second_hiv_test_action_title, repeatNumber);
                    } else {
                        if (StringUtils.isNotBlank(secondHivTestResults))
                            evaluateSecondHivTest(details, repeatNumber + 1);

                        //removing extra actions that are not required in this scenario
                        actionList.remove(mContext.getString(R.string.hts_dna_pcr_sample_collection_action_title));
                        actionList.remove(mContext.getString(R.string.hts_unigold_hiv_test_action_title));
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };

        String actionTitle = context.getString(R.string.hts_second_hiv_test_action_title);
        if (repeatNumber != 1) {
            actionTitle = String.format(context.getString(R.string.hts_repeate_of_second_hiv_test_action_title), repeatNumber);
        }


        Map<String, List<VisitDetail>> mDetails = null;
        for (Visit visit : childVisits) {
            if (visit.getVisitType().equalsIgnoreCase(String.format(context.getString(R.string.hts_second_hiv_test_action_entity_type), repeatNumber))) {
                mDetails = VisitUtils.getVisitGroups(HtsLibrary.getInstance().visitDetailsRepository().getVisits(visit.getVisitId()));
            }
        }

        try {
            JSONObject secondHivTest = FormUtils.getInstance(mContext).getFormJson(Constants.FORMS.HTS_SECOND_HIV_TEST);
            secondHivTest.put(ENCOUNTER_TYPE, String.format(context.getString(R.string.hts_second_hiv_test_action_entity_type), repeatNumber));

            BaseHtsVisitAction action = getBuilder(actionTitle)
                    .withOptional(true)
                    .withJsonPayload(secondHivTest.toString())
                    .withHelper(actionHelper)
                    .withProcessingMode(BaseHtsVisitAction.ProcessingMode.SEPARATE)
                    .withFormName(Constants.FORMS.HTS_SECOND_HIV_TEST)
                    .build();
            actionList.put(actionTitle, action);


            if (mDetails != null) {
                JsonFormUtils.populateForm(secondHivTest, mDetails);
                action.setJsonPayload(secondHivTest.toString());
            }

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    /**
     * Evaluates and initializes the Unigold HIV test action for the HTS visit.
     * This test is conducted as a confirmatory test if the second test is reactive.
     *
     * @param details A map of visit details, containing key-value pairs of information about the visit.
     * @throws BaseHtsVisitAction.ValidationException If the action validation fails during initialization.
     */
    private void evaluateUnigoldHivTest(Map<String, List<VisitDetail>> details, int repeatNumber) throws BaseHtsVisitAction.ValidationException {
        HivUnigoldHivTestActionHelper actionHelper = new HivUnigoldHivTestActionHelper(mContext, memberObject, mClientType, mVisitType) {
            @Override
            public void processUnigoldHivTestResults(String unigoldHivTestResults) {
                try {
                    if (unigoldHivTestResults.equalsIgnoreCase(Constants.HIV_TEST_RESULTS.REACTIVE)) {
                        evaluatePostTestServices(details);
                    }else if (unigoldHivTestResults.equalsIgnoreCase(Constants.HIV_TEST_RESULTS.INVALID) || unigoldHivTestResults.equalsIgnoreCase(Constants.HIV_TEST_RESULTS.WASTAGE)) {
                        actionList.remove(mContext.getString(R.string.hts_dna_pcr_sample_collection_action_title));
                        removeCommonActions();
                        if (StringUtils.isNotBlank(unigoldHivTestResults))
                            evaluateSecondHivTest(details, repeatNumber + 1);
                    } else {
                        if (StringUtils.isNotBlank(mClientType) && mClientType.equalsIgnoreCase("verification")) {
                            removeCommonActions();
                            evaluateDnaPcrSampleCollection(details);
                        } else {
                            actionList.remove(mContext.getString(R.string.hts_dna_pcr_sample_collection_action_title));
                            evaluatePostTestServices(details);
                            evaluateLinkageToPreventionServices(details);
                        }
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };


        String actionTitle = context.getString(R.string.hts_unigold_hiv_test_action_title);
        if (repeatNumber != 1) {
            actionTitle = String.format(context.getString(R.string.hts_repeate_of_unigold_hiv_test_action_title), repeatNumber);
        }


        Map<String, List<VisitDetail>> mDetails = null;
        for (Visit visit : childVisits) {
            if (visit.getVisitType().equalsIgnoreCase(String.format(context.getString(R.string.hts_unigold_hiv_test_action_entity_type), repeatNumber))) {
                mDetails = VisitUtils.getVisitGroups(HtsLibrary.getInstance().visitDetailsRepository().getVisits(visit.getVisitId()));
            }
        }

        try {
            JSONObject unigoldHivTest = FormUtils.getInstance(mContext).getFormJson(Constants.FORMS.HTS_UNIGOLD_HIV_TEST);
            unigoldHivTest.put(ENCOUNTER_TYPE, String.format(context.getString(R.string.hts_unigold_hiv_test_action_entity_type), repeatNumber));

            BaseHtsVisitAction action = getBuilder(actionTitle)
                    .withOptional(true)
                    .withHelper(actionHelper)
                    .withFormName(Constants.FORMS.HTS_UNIGOLD_HIV_TEST)
                    .build();
            actionList.put(context.getString(R.string.hts_unigold_hiv_test_action_title), action);

            if (mDetails != null) {
                JsonFormUtils.populateForm(unigoldHivTest, mDetails);
                action.setJsonPayload(unigoldHivTest.toString());
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }


    /**
     * Evaluates and initializes the repeat of the first HIV test action for the HTS visit.
     * This test is conducted if the second test is non-reactive or inconclusive.
     *
     * @param details A map of visit details, containing key-value pairs of information about the visit.
     * @throws BaseHtsVisitAction.ValidationException If the action validation fails during initialization.
     */
    private void evaluateRepeatOfFirstHivTest(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        HivRepeatFirstHivTestActionHelper actionHelper = new HivRepeatFirstHivTestActionHelper(mContext, memberObject, mVisitType) {
            @Override
            public void processFirstHivTestResults(String firstHivTestResults) {
                try {
                    if (firstHivTestResults.equalsIgnoreCase(Constants.HIV_TEST_RESULTS.NON_REACTIVE)) {
                        evaluatePostTestServices(details);
                        evaluateLinkageToPreventionServices(details);

                        actionList.remove(mContext.getString(R.string.hts_dna_pcr_sample_collection_action_title));
                    } else if (StringUtils.isNotBlank(mVisitType) && mClientType.equalsIgnoreCase("repeat") && firstHivTestResults.equalsIgnoreCase(Constants.HIV_TEST_RESULTS.REACTIVE)) {
                        evaluateDnaPcrSampleCollection(details);
                        removeCommonActions();
                    }
                } catch (Exception e) {
                    Timber.e(e);
                }
                appExecutors.mainThread().execute(() -> callBack.preloadActions(actionList));
            }
        };

        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_repeate_of_first_hiv_test_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_REPEAT_FIRST_HIV_TEST)
                .build();
        actionList.put(context.getString(R.string.hts_repeate_of_first_hiv_test_title), action);
    }

    /**
     * Evaluates and initializes the post-test services action for the HTS visit.
     * This includes counseling and guidance based on the HIV test results.
     *
     * @param details A map of visit details, containing key-value pairs of information about the visit.
     * @throws BaseHtsVisitAction.ValidationException If the action validation fails during initialization.
     */
    private void evaluatePostTestServices(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        PostTestServicesActionHelper actionHelper = new PostTestServicesActionHelper(mContext, memberObject);
        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_post_test_services_action_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_POST_TEST_SERVICES)
                .build();
        actionList.put(context.getString(R.string.hts_post_test_services_action_title), action);
    }

    private void evaluateDnaPcrSampleCollection(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        DnaPcrSampleCollectionActionHelper actionHelper = new DnaPcrSampleCollectionActionHelper(mContext, memberObject);
        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_dna_pcr_sample_collection_action_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_DNA_PCR_SAMPLE_COLLECTION)
                .build();
        actionList.put(context.getString(R.string.hts_dna_pcr_sample_collection_action_title), action);
    }

    /**
     * Evaluates and initializes the linkage to prevention services action
     * for the HTS visit for clients who are found to be HIV Negative.
     * This includes referring clients to prevention programs such as PrEP or risk reduction counseling.
     *
     * @param details A map of visit details, containing key-value pairs of information about the visit.
     * @throws BaseHtsVisitAction.ValidationException If the action validation fails during initialization.
     */
    private void evaluateLinkageToPreventionServices(Map<String, List<VisitDetail>> details) throws BaseHtsVisitAction.ValidationException {
        LinkageToPreventionServicesActionHelper actionHelper = new LinkageToPreventionServicesActionHelper(mContext, memberObject);
        BaseHtsVisitAction action = getBuilder(context.getString(R.string.hts_linkage_to_prevention_services_action_title))
                .withOptional(true)
                .withDetails(details)
                .withHelper(actionHelper)
                .withFormName(Constants.FORMS.HTS_LINKAGE_TO_PREVENTION_SERVICES)
                .build();
        actionList.put(context.getString(R.string.hts_linkage_to_prevention_services_action_title), action);
    }

    /**
     * Removes extra repeat test actions from the action list.
     *
     * @param resId               Resource ID for the test action string
     * @param currentRepeatNumber The current repeat test number
     */
    private void removeExtraRepeatActions(@StringRes int resId, int currentRepeatNumber) {
        for (int testNumber = currentRepeatNumber + 1; testNumber <= actionList.size(); testNumber++) {
            actionList.remove(String.format(context.getString(resId), testNumber));
        }
    }

    /**
     * Removes common actions from the action list (e.g., post-test services, prevention services).
     */
    private void removeCommonActions() {
        actionList.remove(context.getString(R.string.hts_post_test_services_action_title));
        actionList.remove(context.getString(R.string.hts_linkage_to_prevention_services_action_title));
    }

    /**
     * Gets the encounter type for the visit.
     *
     * @return The encounter type string
     */
    @Override
    protected String getEncounterType() {
        return Constants.EVENT_TYPE.HTS_SERVICES;
    }

    /**
     * Gets the table name associated with the visit.
     *
     * @return The table name string
     */
    @Override
    protected String getTableName() {
        return Constants.TABLES.HTS_SERVICES;
    }

}
