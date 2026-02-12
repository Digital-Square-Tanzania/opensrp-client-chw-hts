package org.smartregister.chw.hts.actionhelper;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.smartregister.chw.hts.domain.MemberObject;

import java.util.HashSet;
import java.util.Set;

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

    private PostTestServicesActionHelper createHelper(String gender, int age) throws JSONException {
        MemberObject memberObject = new MemberObject();
        memberObject.setGender(gender);
        memberObject.setDob(DateTime.now().minusYears(age).minusDays(1).toString());

        PostTestServicesActionHelper helper = new PostTestServicesActionHelper(null, memberObject, "reactive");
        helper.onJsonFormLoaded(createPostTestPayload(), null, null);
        return helper;
    }

    private String createPostTestPayload() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("global", new JSONObject());

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

        JSONArray fields = new JSONArray().put(disclosureField);
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
