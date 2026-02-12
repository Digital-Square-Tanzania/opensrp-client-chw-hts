package org.smartregister.chw.hts.actionhelper;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.domain.VisitDetail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class LinkageToPreventionServicesActionHelperTest {

    @Test
    public void femaleAge14TbPresumptiveShouldApplyExpectedVisibility() throws Exception {
        LinkageToPreventionServicesActionHelper helper = createHelper("Female", 14, "tb_suspect");

        Set<String> options = extractOptionKeys(helper.getPreProcessed());

        Assert.assertTrue(options.contains("sputum_testing_laboratory"));
        Assert.assertFalse(options.contains("family_planning"));
        Assert.assertFalse(options.contains("sexual_transmitted_infections_clinic"));
        Assert.assertFalse(options.contains("prep_services"));
        Assert.assertFalse(options.contains("voluntary_medical_male_circumcision_vmmc"));
        Assert.assertTrue(options.contains("adolescent_and_youth_people_friendly_services"));
        Assert.assertFalse(options.contains("care_and_treatment_clinic"));
    }

    @Test
    public void femaleAge15NotTbPresumptiveShouldHideSputumAndShowAge15Options() throws Exception {
        LinkageToPreventionServicesActionHelper helper = createHelper("Female", 15, "screened_negative");

        Set<String> options = extractOptionKeys(helper.getPreProcessed());

        Assert.assertFalse(options.contains("sputum_testing_laboratory"));
        Assert.assertTrue(options.contains("family_planning"));
        Assert.assertTrue(options.contains("sexual_transmitted_infections_clinic"));
        Assert.assertTrue(options.contains("prep_services"));
        Assert.assertFalse(options.contains("voluntary_medical_male_circumcision_vmmc"));
        Assert.assertTrue(options.contains("adolescent_and_youth_people_friendly_services"));
    }

    @Test
    public void maleAge20TbPresumptiveShouldShowMaleAndAgeEligibleOptions() throws Exception {
        LinkageToPreventionServicesActionHelper helper = createHelper("Male", 20, "tb_suspect");

        Set<String> options = extractOptionKeys(helper.getPreProcessed());

        Assert.assertTrue(options.contains("sputum_testing_laboratory"));
        Assert.assertTrue(options.contains("family_planning"));
        Assert.assertTrue(options.contains("sexual_transmitted_infections_clinic"));
        Assert.assertTrue(options.contains("prep_services"));
        Assert.assertTrue(options.contains("voluntary_medical_male_circumcision_vmmc"));
        Assert.assertTrue(options.contains("adolescent_and_youth_people_friendly_services"));
    }

    @Test
    public void maleAge25TbPresumptiveShouldHideYouthOption() throws Exception {
        LinkageToPreventionServicesActionHelper helper = createHelper("Male", 25, "tb_suspect");

        Set<String> options = extractOptionKeys(helper.getPreProcessed());

        Assert.assertTrue(options.contains("voluntary_medical_male_circumcision_vmmc"));
        Assert.assertFalse(options.contains("adolescent_and_youth_people_friendly_services"));
    }

    @Test
    public void persistedTbPresumptiveOutcomeShouldOverrideNonPresumptiveDetails() throws Exception {
        LinkageToPreventionServicesActionHelper helper = createHelper(
                "Female",
                20,
                "screened_negative",
                "tb_suspect",
                (String) null,
                null
        );

        Set<String> options = extractOptionKeys(helper.getPreProcessed());
        Assert.assertTrue(options.contains("sputum_testing_laboratory"));
    }

    @Test
    public void persistedNonTbOutcomeShouldOverrideTbPresumptiveDetails() throws Exception {
        LinkageToPreventionServicesActionHelper helper = createHelper(
                "Female",
                20,
                "tb_suspect",
                "screened_negative",
                (String) null,
                null
        );

        Set<String> options = extractOptionKeys(helper.getPreProcessed());
        Assert.assertFalse(options.contains("sputum_testing_laboratory"));
    }

    @Test
    public void latestPreTestActionOutcomeShouldOverridePersistedOutcome() throws Exception {
        LinkageToPreventionServicesActionHelper helper = createHelper(
                "Female",
                20,
                "screened_negative",
                "screened_negative",
                "tb_suspect",
                null
        );

        Set<String> options = extractOptionKeys(helper.getPreProcessed());
        Assert.assertTrue(options.contains("sputum_testing_laboratory"));
    }

    @Test
    public void shouldUseLatestOutcomeFromProviderAcrossRepeatedPreProcessing() throws Exception {
        AtomicReference<String> latestOutcome = new AtomicReference<>("tb_suspect");

        LinkageToPreventionServicesActionHelper helper = createHelper(
                "Female",
                20,
                "screened_negative",
                "screened_negative",
                latestOutcome::get,
                null
        );

        Set<String> firstOptions = extractOptionKeys(helper.getPreProcessed());
        Assert.assertTrue(firstOptions.contains("sputum_testing_laboratory"));

        latestOutcome.set("screened_negative");
        Set<String> secondOptions = extractOptionKeys(helper.getPreProcessed());
        Assert.assertFalse(secondOptions.contains("sputum_testing_laboratory"));
    }

    private LinkageToPreventionServicesActionHelper createHelper(String gender, int age, String tbOutcome) throws JSONException {
        return createHelper(gender, age, tbOutcome, null, null, null, null);
    }

    private LinkageToPreventionServicesActionHelper createHelper(String gender,
                                                                 int age,
                                                                 String detailsTbOutcome,
                                                                 String latestTbOutcome,
                                                                 String selectedPreTestOutcome,
                                                                 String tbSymptomsAssessment) throws JSONException {
        return createHelper(gender, age, detailsTbOutcome, latestTbOutcome, selectedPreTestOutcome, null, tbSymptomsAssessment);
    }

    private LinkageToPreventionServicesActionHelper createHelper(String gender,
                                                                 int age,
                                                                 String detailsTbOutcome,
                                                                 String latestTbOutcome,
                                                                 LinkageToPreventionServicesActionHelper.TbScreeningOutcomeProvider tbOutcomeProvider,
                                                                 String tbSymptomsAssessment) throws JSONException {
        return createHelper(gender, age, detailsTbOutcome, latestTbOutcome, null, tbOutcomeProvider, tbSymptomsAssessment);
    }

    private LinkageToPreventionServicesActionHelper createHelper(String gender,
                                                                 int age,
                                                                 String detailsTbOutcome,
                                                                 String latestTbOutcome,
                                                                 String selectedPreTestOutcome,
                                                                 LinkageToPreventionServicesActionHelper.TbScreeningOutcomeProvider tbOutcomeProvider,
                                                                 String tbSymptomsAssessment) throws JSONException {
        MemberObject memberObject = new MemberObject();
        memberObject.setGender(gender);
        memberObject.setDob(DateTime.now().minusYears(age).minusDays(1).toString());

        LinkageToPreventionServicesActionHelper helper = tbOutcomeProvider == null
                ? new LinkageToPreventionServicesActionHelper(
                null,
                memberObject,
                selectedPreTestOutcome
        ) {
            @Override
            protected String getLatestTbScreeningOutcome() {
                return latestTbOutcome;
            }

            @Override
            protected String getTbSymptomsAssessment() {
                return tbSymptomsAssessment;
            }
        }
                : new LinkageToPreventionServicesActionHelper(
                null,
                memberObject,
                tbOutcomeProvider
        ) {
            @Override
            protected String getLatestTbScreeningOutcome() {
                return latestTbOutcome;
            }

            @Override
            protected String getTbSymptomsAssessment() {
                return tbSymptomsAssessment;
            }
        };
        helper.onJsonFormLoaded(createPreventiveServicesPayload(), null, createDetailsMap(detailsTbOutcome));
        return helper;
    }

    private Map<String, List<VisitDetail>> createDetailsMap(String tbOutcome) {
        Map<String, List<VisitDetail>> details = new HashMap<>();
        if (tbOutcome == null) {
            return details;
        }

        VisitDetail visitDetail = new VisitDetail();
        visitDetail.setDetails(tbOutcome);
        List<VisitDetail> visitDetails = new ArrayList<>();
        visitDetails.add(visitDetail);
        details.put("hts_clients_tb_screening_outcome", visitDetails);
        return details;
    }

    private String createPreventiveServicesPayload() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("global", new JSONObject());

        JSONObject preventiveServicesField = new JSONObject();
        preventiveServicesField.put("key", "hts_preventive_services");
        preventiveServicesField.put("options", new JSONArray()
                .put(createOption("tuberculosis_clinic"))
                .put(createOption("sputum_testing_laboratory")
                        .put("visibility_only_tb_presumptive", true))
                .put(createOption("family_planning")
                        .put("visibility_minimum_age", 15))
                .put(createOption("sexual_transmitted_infections_clinic")
                        .put("visibility_minimum_age", 15))
                .put(createOption("voluntary_medical_male_circumcision_vmmc")
                        .put("visibility_allowed_sex", "male"))
                .put(createOption("prep_services")
                        .put("visibility_minimum_age", 15))
                .put(createOption("adolescent_and_youth_people_friendly_services")
                        .put("visibility_minimum_age", 10)
                        .put("visibility_maximum_age", 24))
                .put(createOption("others"))
                .put(createOption("none")));

        JSONArray fields = new JSONArray().put(preventiveServicesField);
        JSONObject stepOne = new JSONObject().put("fields", fields);
        jsonObject.put("step1", stepOne);

        return jsonObject.toString();
    }

    private JSONObject createOption(String key) throws JSONException {
        return new JSONObject().put("key", key);
    }

    private Set<String> extractOptionKeys(String jsonPayload) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonPayload);
        JSONArray fields = jsonObject.getJSONObject("step1").getJSONArray("fields");
        JSONArray options = fields.getJSONObject(0).getJSONArray("options");

        Set<String> optionKeys = new HashSet<>();
        for (int index = 0; index < options.length(); index++) {
            optionKeys.add(options.getJSONObject(index).getString("key"));
        }
        return optionKeys;
    }
}
