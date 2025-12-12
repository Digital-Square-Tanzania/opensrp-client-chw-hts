package org.smartregister.chw.hts.dao;

import static org.smartregister.chw.hts.util.Constants.TABLES.HTS_SAMPLE_REGISTER;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.dao.AbstractDao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HtsDao extends AbstractDao {
    private static final SimpleDateFormat df = new SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
    );

    private static final DataMap<MemberObject> memberObjectMap = cursor -> {

        MemberObject memberObject = new MemberObject();

        memberObject.setFirstName(getCursorValue(cursor, "first_name", ""));
        memberObject.setMiddleName(getCursorValue(cursor, "middle_name", ""));
        memberObject.setLastName(getCursorValue(cursor, "last_name", ""));
        memberObject.setAddress(getCursorValue(cursor, "village_town"));
        memberObject.setGender(getCursorValue(cursor, "gender"));
        memberObject.setMartialStatus(getCursorValue(cursor, "marital_status"));
        memberObject.setUniqueId(getCursorValue(cursor, "unique_id", ""));
        memberObject.setDob(getCursorValue(cursor, "dob"));
        memberObject.setFamilyBaseEntityId(getCursorValue(cursor, "relational_id", ""));
        memberObject.setRelationalId(getCursorValue(cursor, "relational_id", ""));
        memberObject.setPrimaryCareGiver(getCursorValue(cursor, "primary_caregiver"));
        memberObject.setFamilyName(getCursorValue(cursor, "family_name", ""));
        memberObject.setPhoneNumber(getCursorValue(cursor, "phone_number", ""));
        memberObject.setHtsTestDate(getCursorValueAsDate(cursor, "hts_test_date", df));
        memberObject.setBaseEntityId(getCursorValue(cursor, "base_entity_id", ""));
        memberObject.setFamilyHead(getCursorValue(cursor, "family_head", ""));
        memberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "pcg_phone_number", ""));
        memberObject.setFamilyHeadPhoneNumber(getCursorValue(cursor, "family_head_phone_number", ""));
        memberObject.setEnrollmentDate(getCursorValue(cursor,
                "enrollment_date",
                ""));

        String familyHeadName = getCursorValue(cursor, "family_head_first_name", "") + " "
                + getCursorValue(cursor, "family_head_middle_name", "");

        familyHeadName =
                (familyHeadName.trim() + " " + getCursorValue(cursor, "family_head_last_name", "")).trim();
        memberObject.setFamilyHeadName(familyHeadName);

        String familyPcgName = getCursorValue(cursor, "pcg_first_name", "") + " "
                + getCursorValue(cursor, "pcg_middle_name", "");

        familyPcgName =
                (familyPcgName.trim() + " " + getCursorValue(cursor, "pcg_last_name", "")).trim();
        memberObject.setPrimaryCareGiverName(familyPcgName);

        return memberObject;
    };

    public static Date getHtsTestDate(String baseEntityID) {
        String sql = "select hts_test_date from ec_hts_enrollment where base_entity_id = '" + baseEntityID + "'";

        DataMap<Date> dataMap = cursor -> getCursorValueAsDate(cursor, "hts_test_date", getNativeFormsDateFormat());

        List<Date> res = readData(sql, dataMap);
        if (res == null || res.size() != 1)
            return null;

        return res.get(0);
    }

    public static String getClientHtsID(String baseEntityId) {
        String sql = "SELECT hts_client_id FROM ec_hts_enrollment p " +
                " WHERE p.base_entity_id = '" + baseEntityId + "' ORDER BY enrollment_date DESC LIMIT 1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "hts_client_id");

        List<String> res = readData(sql, dataMap);
        if (res != null && !res.isEmpty() && res.get(0) != null) {
            return res.get(0);
        }
        return "";
    }

    public static String getClientUniqueId(String baseEntityId) {
        String sql = "SELECT unique_id FROM ec_family_member p " +
                " WHERE p.base_entity_id = '" + baseEntityId + "' LIMIT 1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "unique_id");

        List<String> res = readData(sql, dataMap);
        if (res != null && !res.isEmpty() && res.get(0) != null) {
            return res.get(0);
        }
        return "";
    }

    public static int getVisitNumber(String baseEntityID) {
        String sql = "SELECT visit_number  FROM ec_hts_follow_up_visit WHERE entity_id='" + baseEntityID + "' ORDER BY visit_number DESC LIMIT 1";
        DataMap<Integer> map = cursor -> getCursorIntValue(cursor, "visit_number");
        List<Integer> res = readData(sql, map);

        if (res != null && !res.isEmpty() && res.get(0) != null) {
            return res.get(0) + 1;
        } else
            return 0;
    }

    public static boolean isRegisteredForHts(String baseEntityID) {
        String sql = "SELECT count(p.base_entity_id) count FROM ec_hts_enrollment p " +
                "WHERE p.base_entity_id = '" + baseEntityID + "' AND p.is_closed = 0 ";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count");

        List<Integer> res = readData(sql, dataMap);
        if (res == null || res.size() != 1)
            return false;

        return res.get(0) > 0;
    }

    public static Integer getHtsFamilyMembersCount(String familyBaseEntityId) {
        String sql = "SELECT count(emc.base_entity_id) count FROM ec_hts_enrollment emc " +
                "INNER Join ec_family_member fm on fm.base_entity_id = emc.base_entity_id " +
                "WHERE fm.relational_id = '" + familyBaseEntityId + "' AND fm.is_closed = 0 " +
                "AND emc.is_closed = 0 AND emc.hts = 1";

        DataMap<Integer> dataMap = cursor -> getCursorIntValue(cursor, "count");

        List<Integer> res = readData(sql, dataMap);
        if (res == null || res.isEmpty())
            return 0;
        return res.get(0);
    }

    public static MemberObject getMember(String baseEntityID) {
        String sql = "select " +
                "m.base_entity_id , " +
                "m.unique_id , " +
                "m.relational_id , " +
                "m.dob , " +
                "m.first_name , " +
                "m.middle_name , " +
                "m.last_name , " +
                "m.gender , " +
                "m.marital_status , " +
                "m.phone_number , " +
                "m.other_phone_number , " +
                "f.first_name as family_name ," +
                "f.primary_caregiver , " +
                "f.family_head , " +
                "f.village_town ," +
                "fh.first_name as family_head_first_name , " +
                "fh.middle_name as family_head_middle_name , " +
                "fh.last_name as family_head_last_name, " +
                "fh.phone_number as family_head_phone_number ,  " +
                "pcg.first_name as pcg_first_name , " +
                "pcg.last_name as pcg_last_name , " +
                "pcg.middle_name as pcg_middle_name , " +
                "pcg.phone_number as  pcg_phone_number , " +
                "mr.* " +
                "from ec_family_member m " +
                "inner join ec_family f on m.relational_id = f.base_entity_id " +
                "inner join " + Constants.TABLES.HTS_REGISTER + " mr on mr.base_entity_id = m.base_entity_id " +
                "left join ec_family_member fh on fh.base_entity_id = f.family_head " +
                "left join ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver " +
                "where mr.is_closed = 0 AND m.base_entity_id ='" + baseEntityID + "' ";
        List<MemberObject> res = readData(sql, memberObjectMap);
        if (res == null || res.size() != 1)
            return null;

        return res.get(0);
    }

    public static MemberObject getIndexContactMember(String baseEntityID) {
        String sql = "select " +
                "m.base_entity_id , " +
                "m.unique_id , " +
                "m.relational_id , " +
                "m.dob , " +
                "m.first_name , " +
                "m.middle_name , " +
                "m.last_name , " +
                "m.gender , " +
                "m.marital_status , " +
                "m.phone_number , " +
                "m.other_phone_number , " +
                "f.first_name as family_name ," +
                "f.primary_caregiver , " +
                "f.family_head , " +
                "f.village_town ," +
                "fh.first_name as family_head_first_name , " +
                "fh.middle_name as family_head_middle_name , " +
                "fh.last_name as family_head_last_name, " +
                "fh.phone_number as family_head_phone_number ,  " +
                "pcg.first_name as pcg_first_name , " +
                "pcg.last_name as pcg_last_name , " +
                "pcg.middle_name as pcg_middle_name , " +
                "pcg.phone_number as  pcg_phone_number , " +
                "mr.* " +
                "from ec_family_member m " +
                "inner join ec_family f on m.relational_id = f.base_entity_id " +
                "inner join ec_hiv_index mr on mr.base_entity_id = m.base_entity_id " +
                "left join ec_family_member fh on fh.base_entity_id = f.family_head " +
                "left join ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver " +
                "where mr.is_closed = 0 AND m.base_entity_id ='" + baseEntityID + "' ";
        List<MemberObject> res = readData(sql, memberObjectMap);
        if (res == null || res.size() != 1)
            return null;

        return res.get(0);
    }

    public static List<MemberObject> getMembers() {
        String sql = "select " +
                "m.base_entity_id , " +
                "m.unique_id , " +
                "m.relational_id , " +
                "m.dob , " +
                "m.first_name , " +
                "m.middle_name , " +
                "m.last_name , " +
                "m.gender , " +
                "m.marital_status , " +
                "m.phone_number , " +
                "m.other_phone_number , " +
                "f.first_name as family_name ," +
                "f.primary_caregiver , " +
                "f.family_head , " +
                "f.village_town ," +
                "fh.first_name as family_head_first_name , " +
                "fh.middle_name as family_head_middle_name , " +
                "fh.last_name as family_head_last_name, " +
                "fh.phone_number as family_head_phone_number ,  " +
                "pcg.first_name as pcg_first_name , " +
                "pcg.last_name as pcg_last_name , " +
                "pcg.middle_name as pcg_middle_name , " +
                "pcg.phone_number as  pcg_phone_number , " +
                "mr.* " +
                "from ec_family_member m " +
                "inner join ec_family f on m.relational_id = f.base_entity_id " +
                "inner join " + Constants.TABLES.HTS_REGISTER + " mr on mr.base_entity_id = m.base_entity_id " +
                "left join ec_family_member fh on fh.base_entity_id = f.family_head " +
                "left join ec_family_member pcg on pcg.base_entity_id = f.primary_caregiver " +
                "where mr.is_closed = 0 ";

        return readData(sql, memberObjectMap);
    }


    //TODO update implementation for checking whether DNA PCR should be collected
    public static boolean shouldCollectDnaPCR(String baseEntityID) {
        String sql = "SELECT sample_collection_for_dna_pcr_test, hts_visit_id FROM ec_hts_services p " +
                "LEFT JOIN ec_lab_requests requests ON requests.hts_visit_id = p.visit_id " +
                "WHERE p.entity_id = '" + baseEntityID + "' ORDER BY p.last_interacted_with DESC LIMIT 1 ";

        DataMap<String> sampleCollectionForDnaPcrTestMap = cursor -> getCursorValue(cursor, "sample_collection_for_dna_pcr_test");
        DataMap<String> visitIdMap = cursor -> getCursorValue(cursor, "hts_visit_id");

        List<String> sampleCollectionRes = readData(sql, sampleCollectionForDnaPcrTestMap);
        List<String> htsVisitId = readData(sql, visitIdMap);

        if (sampleCollectionRes == null || sampleCollectionRes.size() != 1)
            return false;
        else if (StringUtils.isNotBlank(sampleCollectionRes.get(0))) {
            return sampleCollectionRes.get(0).equalsIgnoreCase("yes") && (htsVisitId == null || htsVisitId.isEmpty() || StringUtils.isBlank(htsVisitId.get(0)));
        }
        return false;
    }

    public static String getClientLastVisitId(String baseEntityID) {
        String sql = "SELECT visit_id FROM ec_hts_services p " +
                "WHERE p.entity_id = '" + baseEntityID + "' ORDER BY last_interacted_with DESC LIMIT 1";

        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "visit_id");

        List<String> res = readData(sql, dataMap);
        if (res == null || res.size() != 1)
            return null;

        return res.get(0);
    }

    public static boolean hasAnyVisit(String baseEntityID) {
        String sql = "SELECT COUNT(*) AS count FROM ec_hts_services s " +
                "WHERE s.entity_id = '" + baseEntityID + "' ";
        DataMap<Integer> countMap = cursor -> getCursorIntValue(cursor, "count");
        List<Integer> countResults = readData(sql, countMap);
        return !countResults.isEmpty() && countResults.get(0) > 0;
    }

    public static boolean hasRecentlyTestedWithHivst(String baseEntityID) {
        String sql = "SELECT COUNT(*) AS count FROM ec_hivst_followup s " +
                "WHERE s.entity_id = '" + baseEntityID + "' ";
        DataMap<Integer> countMap = cursor -> getCursorIntValue(cursor, "count");
        List<Integer> countResults = readData(sql, countMap);
        return !countResults.isEmpty() && countResults.get(0) > 0;
    }

    public static String fetchKitType(String baseEntityID) {
        String sql = "SELECT client_kit_type FROM ec_hivst_followup s " +
                "WHERE s.entity_id = '" + baseEntityID + "' ";
        DataMap<String> countMap = cursor -> getCursorValue(cursor, "client_kit_type");
        List<String> kitTypeResult = readData(sql, countMap);
        if (!kitTypeResult.isEmpty()) {
            return kitTypeResult.get(0);
        } else {
            return null;
        }
    }

    public static String getTbSymptomsAssessment(String baseEntityID) {
        String sql = "SELECT does_the_client_have_any_of_the_following_tuberculosis_symptoms_assessment FROM ec_hts_register s " +
                "WHERE s.base_entity_id = '" + baseEntityID + "' ";
        DataMap<String> countMap = cursor -> getCursorValue(cursor, "does_the_client_have_any_of_the_following_tuberculosis_symptoms_assessment");
        List<String> tbSymptomsAssessment = readData(sql, countMap);
        if (!tbSymptomsAssessment.isEmpty()) {
            return tbSymptomsAssessment.get(0);
        } else {
            return null;
        }
    }

    public static String getHasTheClientEverTestedForHiv(String baseEntityID) {
        String sql = "SELECT has_the_client_ever_tested_for_hiv FROM ec_hts_register s " +
                "WHERE s.base_entity_id = '" + baseEntityID + "' ";
        DataMap<String> dataMap = cursor -> getCursorValue(cursor, "has_the_client_ever_tested_for_hiv");
        List<String> everTestedForHiv = readData(sql, dataMap);
        if (!everTestedForHiv.isEmpty()) {
            return everTestedForHiv.get(0);
        } else {
            return null;
        }
    }

    public static Boolean isClientAnIndexContact(String baseEntityID) {
        String sql = "SELECT count(*) as count FROM ec_hiv_index_hf s " +
                "WHERE s.base_entity_id = '" + baseEntityID + "' ";
        DataMap<Integer> countMap = cursor -> getCursorIntValue(cursor, "count");
        List<Integer> countResults = readData(sql, countMap);
        return !countResults.isEmpty() && countResults.get(0) > 0;
    }

    public static void saveSampleRegistration(String baseEntityId,
                                              String sampleType,
                                              String iqcType,
                                              String testingPoint,
                                              long lastInteractedWith) {
        String sql = "INSERT INTO " + HTS_SAMPLE_REGISTER +
                "    (base_entity_id, sample_type, iqc_type, pitc_testing_point, last_interacted_with) " +
                "VALUES ('" + baseEntityId + "', '" + sampleType + "', '" + iqcType + "', '" + testingPoint + "', " + lastInteractedWith + ") " +
                "ON CONFLICT (base_entity_id) DO UPDATE " +
                "SET sample_type = '" + sampleType + "', " +
                "    iqc_type = '" + iqcType + "', " +
                "    pitc_testing_point = '" + testingPoint + "', " +
                "    last_interacted_with = " + lastInteractedWith;

        updateDB(sql);
    }
}
