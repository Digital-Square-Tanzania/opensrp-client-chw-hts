package org.smartregister.chw.hts.actionhelper;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.model.BaseHtsVisitAction;
import org.smartregister.chw.hts.util.Constants;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PostTestServicesActionHelperTest {

    @Test
    public void shouldShowHusbandAndPartnerForFemaleClientTenAndAbove() throws Exception {
        PostTestServicesActionHelper helper = createHelper("Female", 10);

        Set<String> options = extractOptionKeys(helper.getPreProcessed());

        Assert.assertTrue(options.contains("husband"));
        Assert.assertFalse(options.contains("wife"));
        Assert.assertTrue(options.contains("partner_who_is_not_wife_or_husband"));
    }

    @Test
    public void shouldShowWifeAndPartnerForMaleClientEighteenAndAbove() throws Exception {
        PostTestServicesActionHelper helper = createHelper("Male", 18);

        Set<String> options = extractOptionKeys(helper.getPreProcessed());

        Assert.assertFalse(options.contains("husband"));
        Assert.assertTrue(options.contains("wife"));
        Assert.assertTrue(options.contains("partner_who_is_not_wife_or_husband"));
    }

    @Test
    public void shouldHidePartnerOptionsForFemaleClientBelowTen() throws Exception {
        PostTestServicesActionHelper helper = createHelper("Female", 9);

        Set<String> options = extractOptionKeys(helper.getPreProcessed());

        Assert.assertFalse(options.contains("husband"));
        Assert.assertFalse(options.contains("wife"));
        Assert.assertFalse(options.contains("partner_who_is_not_wife_or_husband"));
    }

    @Test
    public void shouldHidePartnerOptionsForMaleClientBelowEighteen() throws Exception {
        PostTestServicesActionHelper helper = createHelper("Male", 17);

        Set<String> options = extractOptionKeys(helper.getPreProcessed());

        Assert.assertFalse(options.contains("husband"));
        Assert.assertFalse(options.contains("wife"));
        Assert.assertFalse(options.contains("partner_who_is_not_wife_or_husband"));
    }

    @Test
    public void shouldMapFirstHivTestNonReactiveToNegativeFinalResult() throws Exception {
        Map<String, BaseHtsVisitAction> priorActions = new LinkedHashMap<>();
        priorActions.put("First HIV Test Using Bioline/Dual Test",
                createPriorAction(Constants.FORMS.HTS_FIRST_HIV_TEST,
                        "First HIV Test Using Bioline/Dual Test",
                        Constants.HIV_TEST_RESULTS.NON_REACTIVE));

        PostTestServicesActionHelper helper = createHelper("Female", 20, priorActions);
        String preProcessed = helper.getPreProcessed();

        Assert.assertEquals(Constants.HIV_TEST_RESULTS.NEGATIVE,
                extractFieldValue(preProcessed, "final_hiv_test_result"));
    }

    @Test
    public void shouldMapRepeatFirstHivTestReactiveToInconclusiveFinalResult() throws Exception {
        Map<String, BaseHtsVisitAction> priorActions = new LinkedHashMap<>();
        priorActions.put("Repeat of First HIV Test",
                createPriorAction(Constants.FORMS.HTS_REPEAT_FIRST_HIV_TEST,
                        "Repeat of First HIV Test",
                        Constants.HIV_TEST_RESULTS.REACTIVE));

        PostTestServicesActionHelper helper = createHelper("Female", 20, priorActions);
        String preProcessed = helper.getPreProcessed();

        Assert.assertEquals(Constants.HIV_TEST_RESULTS.INCONCLUSIVE,
                extractFieldValue(preProcessed, "final_hiv_test_result"));
    }

    @Test
    public void shouldMapRepeatFirstHivTestNonReactiveToNegativeFinalResult() throws Exception {
        Map<String, BaseHtsVisitAction> priorActions = new LinkedHashMap<>();
        priorActions.put("Repeat of First HIV Test",
                createPriorAction(Constants.FORMS.HTS_REPEAT_FIRST_HIV_TEST,
                        "Repeat of First HIV Test",
                        Constants.HIV_TEST_RESULTS.NON_REACTIVE));

        PostTestServicesActionHelper helper = createHelper("Female", 20, priorActions);
        String preProcessed = helper.getPreProcessed();

        Assert.assertEquals(Constants.HIV_TEST_RESULTS.NEGATIVE,
                extractFieldValue(preProcessed, "final_hiv_test_result"));
    }

    @Test
    public void shouldMapUnigoldReactiveToPositiveFinalResult() throws Exception {
        Map<String, BaseHtsVisitAction> priorActions = new LinkedHashMap<>();
        priorActions.put("Uni-Gold HIV Test",
                createPriorAction(Constants.FORMS.HTS_UNIGOLD_HIV_TEST,
                        "Uni-Gold HIV Test",
                        Constants.HIV_TEST_RESULTS.REACTIVE));

        PostTestServicesActionHelper helper = createHelper("Female", 20, priorActions);
        String preProcessed = helper.getPreProcessed();

        Assert.assertEquals(Constants.HIV_TEST_RESULTS.POSITIVE,
                extractFieldValue(preProcessed, "final_hiv_test_result"));
    }

    @Test
    public void shouldMapUnigoldNonReactiveToInconclusiveFinalResult() throws Exception {
        Map<String, BaseHtsVisitAction> priorActions = new LinkedHashMap<>();
        priorActions.put("Uni-Gold HIV Test",
                createPriorAction(Constants.FORMS.HTS_UNIGOLD_HIV_TEST,
                        "Uni-Gold HIV Test",
                        Constants.HIV_TEST_RESULTS.NON_REACTIVE));

        PostTestServicesActionHelper helper = createHelper("Female", 20, priorActions);
        String preProcessed = helper.getPreProcessed();

        Assert.assertEquals(Constants.HIV_TEST_RESULTS.INCONCLUSIVE,
                extractFieldValue(preProcessed, "final_hiv_test_result"));
    }

    @Test
    public void shouldUseHighestSuffixForRepeatedActionInstances() throws Exception {
        Map<String, BaseHtsVisitAction> priorActions = new LinkedHashMap<>();
        priorActions.put("Repeat of First HIV Test",
                createPriorAction(Constants.FORMS.HTS_REPEAT_FIRST_HIV_TEST,
                        "Repeat of First HIV Test",
                        Constants.HIV_TEST_RESULTS.REACTIVE));
        priorActions.put("Repeat of First HIV Test 2",
                createPriorAction(Constants.FORMS.HTS_REPEAT_FIRST_HIV_TEST,
                        "Repeat of First HIV Test 2",
                        Constants.HIV_TEST_RESULTS.NON_REACTIVE));

        PostTestServicesActionHelper helper = createHelper("Female", 20, priorActions);
        String preProcessed = helper.getPreProcessed();

        Assert.assertEquals(Constants.HIV_TEST_RESULTS.NEGATIVE,
                extractFieldValue(preProcessed, "final_hiv_test_result"));
    }

    @Test
    public void shouldPrioritizeUnigoldResultWhenMultipleHivTestActionsExist() throws Exception {
        Map<String, BaseHtsVisitAction> priorActions = new LinkedHashMap<>();
        priorActions.put("First HIV Test Using Bioline/Dual Test",
                createPriorAction(Constants.FORMS.HTS_FIRST_HIV_TEST,
                        "First HIV Test Using Bioline/Dual Test",
                        Constants.HIV_TEST_RESULTS.NON_REACTIVE));
        priorActions.put("Repeat of First HIV Test",
                createPriorAction(Constants.FORMS.HTS_REPEAT_FIRST_HIV_TEST,
                        "Repeat of First HIV Test",
                        Constants.HIV_TEST_RESULTS.REACTIVE));
        priorActions.put("Uni-Gold HIV Test",
                createPriorAction(Constants.FORMS.HTS_UNIGOLD_HIV_TEST,
                        "Uni-Gold HIV Test",
                        Constants.HIV_TEST_RESULTS.REACTIVE));

        PostTestServicesActionHelper helper = createHelper("Female", 20, priorActions);
        String preProcessed = helper.getPreProcessed();

        Assert.assertEquals(Constants.HIV_TEST_RESULTS.POSITIVE,
                extractFieldValue(preProcessed, "final_hiv_test_result"));
    }

    private PostTestServicesActionHelper createHelper(String gender, int age) throws JSONException {
        return createHelper(gender, age, null);
    }

    private PostTestServicesActionHelper createHelper(String gender, int age, Map<String, BaseHtsVisitAction> priorActions) throws JSONException {
        MemberObject memberObject = new MemberObject();
        memberObject.setGender(gender);
        memberObject.setDob(DateTime.now().minusYears(age).minusDays(1).toString());

        PostTestServicesActionHelper helper = new PostTestServicesActionHelper(null, memberObject, "reactive", priorActions);
        helper.onJsonFormLoaded(createPostTestPayload(), null, null);
        return helper;
    }

    private BaseHtsVisitAction createPriorAction(String formName, String title, String testResult) throws JSONException {
        BaseHtsVisitAction action = mock(BaseHtsVisitAction.class);
        when(action.getFormName()).thenReturn(formName);
        when(action.getTitle()).thenReturn(title);
        when(action.getJsonPayload()).thenReturn(createActionPayload(testResult));
        return action;
    }

    private String createActionPayload(String testResult) throws JSONException {
        JSONObject testResultField = new JSONObject()
                .put("key", "test_result")
                .put("value", testResult);

        return new JSONObject()
                .put("count", "1")
                .put("step1", new JSONObject().put("fields", new JSONArray().put(testResultField)))
                .toString();
    }

    private String createPostTestPayload() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("global", new JSONObject());

        JSONObject finalHivTestResultField = new JSONObject();
        finalHivTestResultField.put("key", "final_hiv_test_result");

        JSONObject disclosureField = new JSONObject();
        disclosureField.put("key", "hts_hiv_results_disclosure");
        disclosureField.put("options", new JSONArray()
                .put(createOption("husband")
                        .put("visibility_allowed_sex", "female")
                        .put("visibility_minimum_age", 10))
                .put(createOption("wife")
                        .put("visibility_allowed_sex", "male")
                        .put("visibility_minimum_age", 18))
                .put(createOption("partner_who_is_not_wife_or_husband")
                        .put("visibility_allowed_sex", "female,male")
                        .put("visibility_minimum_age_female", 10)
                        .put("visibility_minimum_age_male", 18))
                .put(createOption("relative")));

        JSONArray fields = new JSONArray()
                .put(finalHivTestResultField)
                .put(disclosureField);
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
        JSONObject disclosureField = null;
        for (int index = 0; index < fields.length(); index++) {
            JSONObject field = fields.getJSONObject(index);
            if ("hts_hiv_results_disclosure".equals(field.optString("key"))) {
                disclosureField = field;
                break;
            }
        }

        if (disclosureField == null) {
            return new HashSet<>();
        }

        JSONArray options = disclosureField.getJSONArray("options");

        Set<String> optionKeys = new HashSet<>();
        for (int index = 0; index < options.length(); index++) {
            optionKeys.add(options.getJSONObject(index).getString("key"));
        }
        return optionKeys;
    }

    private String extractFieldValue(String jsonPayload, String key) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonPayload);
        JSONArray fields = jsonObject.getJSONObject("step1").getJSONArray("fields");
        for (int index = 0; index < fields.length(); index++) {
            JSONObject field = fields.getJSONObject(index);
            if (key.equals(field.optString("key"))) {
                return field.optString("value", null);
            }
        }
        return null;
    }
}
