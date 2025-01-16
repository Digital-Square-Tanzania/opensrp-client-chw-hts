package org.smartregister.chw.hts.util;

public interface Constants {

    int REQUEST_CODE_GET_JSON = 2244;
    String ENCOUNTER_TYPE = "encounter_type";
    String STEP_ONE = "step1";
    String Hts_VISIT_GROUP = "hts_visit_group";


    interface HIV_TEST_RESULTS {
        String REACTIVE = "reactive";
        String NON_REACTIVE = "non_reactive";
        String POSITIVE = "positive";
        String NEGATIVE = "negative";
        String INCONCLUSIVE = "inconclusive";
    }

    interface JSON_FORM_EXTRA {
        String JSON = "json";
        String ENCOUNTER_TYPE = "encounter_type";
        String EVENT_TYPE = "eventType";
    }

    interface EVENT_TYPE {
        String HTS_SCREENING = "HTS Screening";
        String HTS_SERVICES = "Hts Services";
        String VOID_EVENT = "Void Event";
        String CLOSE_HTS_SERVICE = "Close Hts Service";
        String HTS_FIRST_HIV_TEST = "HTS First HIV Test";

    }

    interface FORMS {
        String HTS_SCREENING = "hts_screening";
        String HTS_VISIT_TYPE = "hts_visit_type";
        String HTS_PRE_TEST_SERVICES = "hts_pre_test_services";
        String HTS_FIRST_HIV_TEST = "hts_first_hiv_test";
        String HTS_SECOND_HIV_TEST = "hts_second_hiv_test";
        String HTS_UNIGOLD_HIV_TEST = "hts_unigold_hiv_test";
        String HTS_POST_TEST_SERVICES = "hts_post_test_services";
        String HTS_DNA_PCR_SAMPLE_COLLECTION = "hts_dna_pcr_sample_collection";
        String HTS_LINKAGE_TO_PREVENTION_SERVICES = "hts_preventive_services";
        String HTS_REPEAT_FIRST_HIV_TEST = "hts_repeat_first_hiv_test";
        String HTS_SCREENING_15_AND_ABOVE = "hts_screening_15_and_above_form";
        String HTS_SCREENING_2_TO_9 = "hts_screening_children_aged_2_to_9_form";
        String HTS_SCREENING_10_TO_14 = "hts_screening_children_aged_10_to_14_form";
    }

    interface TABLES {
        String HTS_REGISTER = "ec_hts_register";
        String HTS_SERVICES = "ec_hts_services";
    }

    interface ACTIVITY_PAYLOAD {
        String BASE_ENTITY_ID = "BASE_ENTITY_ID";
        String FAMILY_BASE_ENTITY_ID = "FAMILY_BASE_ENTITY_ID";
        String HTS_FORM_NAME = "HTS_FORM_NAME";
        String MEMBER_PROFILE_OBJECT = "MemberObject";
        String EDIT_MODE = "editMode";
        String PROFILE_TYPE = "profile_type";
        String SEX = "sex";

    }

    interface ACTIVITY_PAYLOAD_TYPE {
        String REGISTRATION = "REGISTRATION";
        String FOLLOW_UP_VISIT = "FOLLOW_UP_VISIT";
    }

    interface CONFIGURATION {
        String Hts_ENROLLMENT = "hts_enrollment";
    }

    interface Hts_MEMBER_OBJECT {
        String MEMBER_OBJECT = "memberObject";
    }

    interface PROFILE_TYPES {
        String Hts_PROFILE = "hts_profile";
    }

    interface VALUES {
        String NONE = "none";
        String CHORDAE = "chordae";
        String HIV = "hiv";
        String RBG = "random_blood_glucose_test";
        String FBG = "fast_blood_glucose_test";
        String HYPERTENSION = "hypertension";
        String SILICON_OR_LEXAN = "silicon_or_lexan";
        String NEGATIVE = "negative";
        String SATISFACTORY = "satisfactory";
        String NEEDS_FOLLOWUP = "needs_followup";
        String YES = "yes";
    }

    interface TABLE_COLUMN {
        String GENITAL_EXAMINATION = "genital_examination";
        String SYSTOLIC = "systolic";
        String DIASTOLIC = "diastolic";
        String ANY_COMPLAINTS = "any_complaints";
        String CLIENT_DIAGNONISED_WITH = "is_client_diagnosed_with_any";
        String COMPLICATION_TYPE = "type_complication";
        String HEMATOLOGICAL_DISEASE_SYMPTOMS = "any_hematological_disease_symptoms";
        String KNOWN_ALLEGIES = "known_allergies";
        String HIV_RESULTS = "hiv_result";
        String HIV_VIRAL_LOAD = "hiv_viral_load_text";
        String TYPE_OF_BLOOD_FOR_GLUCOSE_TEST = "type_of_blood_for_glucose_test";
        String BLOOD_FOR_GLUCOSE = "blood_for_glucose";
        String DISCHARGE_CONDITION = "discharge_condition";
        String IS_MALE_PROCEDURE_CIRCUMCISION_CONDUCTED = "is_male_procedure_circumcision_conducted";
    }

}
