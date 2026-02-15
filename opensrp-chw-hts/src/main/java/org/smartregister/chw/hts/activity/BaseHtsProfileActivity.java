package org.smartregister.chw.hts.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import org.apache.commons.lang3.StringUtils;
import org.smartregister.chw.hts.HtsLibrary;
import org.smartregister.chw.hts.R;
import org.smartregister.chw.hts.contract.HtsProfileContract;
import org.smartregister.chw.hts.custom_views.BaseHtsFloatingMenu;
import org.smartregister.chw.hts.dao.HtsDao;
import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.domain.Visit;
import org.smartregister.chw.hts.interactor.BaseHtsProfileInteractor;
import org.smartregister.chw.hts.presenter.BaseHtsProfilePresenter;
import org.smartregister.chw.hts.util.Constants;
import org.smartregister.chw.hts.util.HtsUtil;
import org.smartregister.chw.hts.util.HtsVisitsUtil;
import org.smartregister.domain.AlertStatus;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.view.activity.BaseProfileActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;


public abstract class BaseHtsProfileActivity extends BaseProfileActivity implements HtsProfileContract.View, HtsProfileContract.InteractorCallBack {

    protected MemberObject memberObject;
    protected HtsProfileContract.Presenter profilePresenter;
    protected CircleImageView imageView;
    protected TextView textViewName;
    protected TextView textViewGender;
    protected TextView textViewLocation;
    protected TextView textViewUniqueID;
    protected TextView textViewRecordHts;
    protected TextView textViewRecordAnc;
    protected TextView textViewContinueHtsService;
    protected TextView manualProcessVisit;
    protected TextView textview_positive_date;
    protected View view_last_visit_row;
    protected View view_most_due_overdue_row;
    protected View view_family_row;
    protected View view_positive_date_row;
    protected RelativeLayout rlLastVisit;
    protected RelativeLayout rlUpcomingServices;
    protected RelativeLayout rlFamilyServicesDue;
    protected RelativeLayout visitStatus;
    protected RelativeLayout htsServiceInProgress;
    protected ImageView imageViewCross;
    protected TextView textViewUndo;
    protected RelativeLayout rlHtsPositiveDate;
    protected TextView textViewVisitDone;
    protected RelativeLayout visitDone;
    protected LinearLayout recordVisits;
    protected TextView textViewVisitDoneEdit;
    protected TextView textViewRecordAncNotDone;
    protected TextView ivViewCtcId;
    protected TextView textViewVerificationDate;
    protected View rowVerificationCtcId;
    protected String profileType;
    protected BaseHtsFloatingMenu baseHtsFloatingMenu;
    private TextView tvUpComingServices;
    private TextView tvFamilyStatus;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
    private ProgressBar progressBar;

    public static void startProfileActivity(Activity activity, String baseEntityId) {
        Intent intent = new Intent(activity, BaseHtsProfileActivity.class);
        intent.putExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID, baseEntityId);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_hts_profile);
        Toolbar toolbar = findViewById(R.id.collapsing_toolbar);
        setSupportActionBar(toolbar);
        String baseEntityId = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.BASE_ENTITY_ID);
        profileType = getIntent().getStringExtra(Constants.ACTIVITY_PAYLOAD.PROFILE_TYPE);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
            upArrow.setColorFilter(getResources().getColor(R.color.text_blue), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
        }

        toolbar.setNavigationOnClickListener(v -> BaseHtsProfileActivity.this.finish());
        appBarLayout = this.findViewById(R.id.collapsing_toolbar_appbarlayout);
        if (Build.VERSION.SDK_INT >= 21) {
            appBarLayout.setOutlineProvider(null);
        }

        textViewName = findViewById(R.id.textview_name);
        textViewGender = findViewById(R.id.textview_gender);
        textViewLocation = findViewById(R.id.textview_address);
        textViewUniqueID = findViewById(R.id.textview_id);
        view_last_visit_row = findViewById(R.id.view_last_visit_row);
        view_most_due_overdue_row = findViewById(R.id.view_most_due_overdue_row);
        view_family_row = findViewById(R.id.view_family_row);
        view_positive_date_row = findViewById(R.id.view_positive_date_row);
        imageViewCross = findViewById(R.id.tick_image);
        tvUpComingServices = findViewById(R.id.textview_name_due);
        tvFamilyStatus = findViewById(R.id.textview_family_has);
        textview_positive_date = findViewById(R.id.textview_verification_results);
        ivViewCtcId = findViewById(R.id.ivViewCtcId);
        textViewVerificationDate = findViewById(R.id.textview_verification_date);
        rowVerificationCtcId = findViewById(R.id.row_verification_ctc_id);
        rlLastVisit = findViewById(R.id.rlLastVisit);
        rlUpcomingServices = findViewById(R.id.rlUpcomingServices);
        rlFamilyServicesDue = findViewById(R.id.rlFamilyServicesDue);
        rlHtsPositiveDate = findViewById(R.id.rlHtsPositiveDate);
        textViewVisitDone = findViewById(R.id.textview_visit_done);
        visitStatus = findViewById(R.id.record_visit_not_done_bar);
        visitDone = findViewById(R.id.visit_done_bar);
        htsServiceInProgress = findViewById(R.id.record_hts_service_visit_in_progress);
        recordVisits = findViewById(R.id.record_visits);
        progressBar = findViewById(R.id.progress_bar);
        textViewRecordAncNotDone = findViewById(R.id.textview_record_anc_not_done);
        textViewVisitDoneEdit = findViewById(R.id.textview_edit);
        textViewRecordHts = findViewById(R.id.textview_record_hts);
        textViewContinueHtsService = findViewById(R.id.continue_hts_service);
        manualProcessVisit = findViewById(R.id.textview_manual_process);
        textViewRecordAnc = findViewById(R.id.textview_record_anc);
        textViewUndo = findViewById(R.id.textview_undo);
        imageView = findViewById(R.id.imageview_profile);

        textViewRecordAncNotDone.setOnClickListener(this);
        textViewVisitDoneEdit.setOnClickListener(this);
        rlLastVisit.setOnClickListener(this);
        rlUpcomingServices.setOnClickListener(this);
        rlFamilyServicesDue.setOnClickListener(this);
        rlHtsPositiveDate.setOnClickListener(this);
        textViewRecordHts.setOnClickListener(this);
        textViewContinueHtsService.setOnClickListener(this);
        manualProcessVisit.setOnClickListener(this);
        textViewRecordAnc.setOnClickListener(this);
        textViewUndo.setOnClickListener(this);

        imageRenderHelper = new ImageRenderHelper(this);
        memberObject = getMemberObject(baseEntityId);
        initializePresenter();
        profilePresenter.fillProfileData(memberObject);
        setupViews();
    }


    @Override
    protected void onResume() {
        super.onResume();
        setupViews();
    }

    @Override
    protected void onResumption() {
        super.onResumption();
        setupViews();
    }

    @Override
    protected void setupViews() {
        initializeFloatingMenu();
        refreshVerificationTestResults();
        setupButtons();
    }

    protected void setupButtons() {
        try {

            if (HtsDao.isHtsRegisterClosed(memberObject.getBaseEntityId())) {
                textViewRecordHts.setVisibility(View.GONE);
                return;
            }

            if (HtsDao.hasReactiveVerificationTest(memberObject.getBaseEntityId())) {
                textViewRecordHts.setVisibility(View.GONE);
            } else if (getServiceVisit() != null) {
                if (!getServiceVisit().getProcessed() && HtsVisitsUtil.getHtsVisitStatus(getServiceVisit()).equalsIgnoreCase(HtsVisitsUtil.Complete)) {
                    textViewRecordHts.setVisibility(View.GONE);
                    manualProcessVisit.setVisibility(View.VISIBLE);
                    textViewContinueHtsService.setText(R.string.edit_visit);
                    manualProcessVisit.setOnClickListener(view -> {
                        try {
                            HtsVisitsUtil.manualProcessVisit(getServiceVisit());
                            displayToast(R.string.hts_visit_conducted);
                            setupViews();
                        } catch (Exception e) {
                            Timber.d(e);
                        }
                    });
                } else {
                    manualProcessVisit.setVisibility(View.GONE);
                }
                if (isVisitOnProgress(getServiceVisit())) {
                    textViewRecordHts.setVisibility(View.GONE);
                    htsServiceInProgress.setVisibility(View.VISIBLE);
                } else {
                    textViewRecordHts.setVisibility(View.VISIBLE);
                    htsServiceInProgress.setVisibility(View.GONE);
                }

                if (HtsDao.shouldCollectDnaPCR(memberObject.getBaseEntityId())) {
                    textViewRecordHts.setText(R.string.collect_dna_pcr);
                } else {
                    textViewRecordHts.setText(R.string.record_hts);
                }
                processHtsService();
            }

        } catch (Exception e) {
            Timber.d(e);
        }
    }

    protected Visit getServiceVisit() {
        return HtsLibrary.getInstance().visitRepository().getLatestVisit(memberObject.getBaseEntityId(), Constants.EVENT_TYPE.HTS_SERVICES);
    }


    protected void processHtsService() {
        rlLastVisit.setVisibility(View.VISIBLE);
    }


    protected MemberObject getMemberObject(String baseEntityId) {
        return HtsDao.getMember(baseEntityId);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.title_layout) {
            onBackPressed();
        } else if (id == R.id.rlLastVisit) {
            this.openMedicalHistory();
        } else if (id == R.id.rlUpcomingServices) {
            this.openUpcomingService();
        } else if (id == R.id.rlFamilyServicesDue) {
            this.openFamilyDueServices();
        } else if (id == R.id.textview_record_hts) {
            if (((TextView) view).getText().toString().equalsIgnoreCase(getString(R.string.collect_dna_pcr))) {
                this.startDnaPcrSampleCollection();
            } else {
                this.openFollowupVisit();
            }
        } else if (id == R.id.continue_hts_service) {
            this.continueService();
        }
    }

    @Override
    protected void initializePresenter() {
        showProgressBar(true);
        profilePresenter = new BaseHtsProfilePresenter(this, new BaseHtsProfileInteractor(), memberObject);
        fetchProfileData();
        profilePresenter.refreshProfileBottom();
    }

    public void initializeFloatingMenu() {
        if (StringUtils.isNotBlank(memberObject.getPhoneNumber())) {
            baseHtsFloatingMenu = new BaseHtsFloatingMenu(this, memberObject);
            baseHtsFloatingMenu.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            addContentView(baseHtsFloatingMenu, linearLayoutParams);
        }
    }

    @Override
    public void hideView() {
        textViewRecordHts.setVisibility(View.GONE);
    }

    @Override
    public void openFollowupVisit() {
        //Implement in application
    }


    @SuppressLint("DefaultLocale")
    @Override
    public void setProfileViewWithData() {
        textViewName.setText(String.format("%s %s %s, %d", memberObject.getFirstName(), memberObject.getMiddleName(), memberObject.getLastName(), memberObject.getAge()));
        textViewGender.setText(HtsUtil.getGenderTranslated(this, memberObject.getGender()));
        textViewLocation.setText(memberObject.getAddress());
        textViewUniqueID.setText(memberObject.getUniqueId());
    }

    private void refreshVerificationTestResults() {
        if (memberObject == null || StringUtils.isBlank(memberObject.getBaseEntityId())) {
            rlHtsPositiveDate.setVisibility(View.GONE);
            return;
        }

        rlHtsPositiveDate.setVisibility(View.VISIBLE);
        HtsDao.VerificationTestResult verificationTestResult = HtsDao.getLatestVerificationTestResult(memberObject.getBaseEntityId());
        String verificationResult = verificationTestResult == null
                ? ""
                : StringUtils.trimToEmpty(verificationTestResult.getVerificationResult());
        String verificationDate = verificationTestResult == null
                ? ""
                : StringUtils.trimToEmpty(verificationTestResult.getVerificationDate());
        String ctcId = verificationTestResult == null
                ? ""
                : StringUtils.trimToEmpty(verificationTestResult.getCtcId());

        VerificationStatus status = getVerificationStatus(verificationResult);
        applyVerificationStatusStyle(status);

        if (StringUtils.isNotBlank(verificationDate)) {
            textViewVerificationDate.setText(verificationDate);
        } else {
            textViewVerificationDate.setText(R.string.verification_value_not_available);
        }

        if (status == VerificationStatus.POSITIVE && StringUtils.isNotBlank(ctcId)) {
            ivViewCtcId.setText(ctcId);
            rowVerificationCtcId.setVisibility(View.VISIBLE);
        } else {
            clearCtcId();
        }
    }

    private void clearCtcId() {
        ivViewCtcId.setText(R.string.verification_value_not_available);
        rowVerificationCtcId.setVisibility(View.GONE);
    }

    private VerificationStatus getVerificationStatus(String verificationResult) {
        if (StringUtils.isBlank(verificationResult)) {
            return VerificationStatus.UNKNOWN;
        }

        String normalizedResult = verificationResult.trim().replace("_", " ").toLowerCase(Locale.getDefault());
        if (StringUtils.equals(normalizedResult, Constants.HIV_TEST_RESULTS.POSITIVE)
                || StringUtils.equals(normalizedResult, "hiv positive")
                || StringUtils.equals(normalizedResult, "hts positive")) {
            return VerificationStatus.POSITIVE;
        }

        if (StringUtils.equals(normalizedResult, Constants.HIV_TEST_RESULTS.NEGATIVE)
                || StringUtils.equals(normalizedResult, "hiv negative")
                || StringUtils.equals(normalizedResult, "hts negative")) {
            return VerificationStatus.NEGATIVE;
        }

        return VerificationStatus.UNKNOWN;
    }

    private void applyVerificationStatusStyle(VerificationStatus status) {
        if (status == VerificationStatus.POSITIVE) {
            textview_positive_date.setText(R.string.verification_status_positive);
            textview_positive_date.setTextColor(ContextCompat.getColor(this, R.color.verification_status_positive_text));
            textview_positive_date.setBackgroundResource(R.drawable.bg_verification_status_chip_positive);
            return;
        }

        if (status == VerificationStatus.NEGATIVE) {
            textview_positive_date.setText(R.string.verification_status_negative);
            textview_positive_date.setTextColor(ContextCompat.getColor(this, R.color.verification_status_negative_text));
            textview_positive_date.setBackgroundResource(R.drawable.bg_verification_status_chip_negative);
            return;
        }

        textview_positive_date.setText(R.string.verification_status_unknown);
        textview_positive_date.setTextColor(ContextCompat.getColor(this, R.color.verification_status_neutral_text));
        textview_positive_date.setBackgroundResource(R.drawable.bg_verification_status_chip_neutral);
    }

    private enum VerificationStatus {
        POSITIVE,
        NEGATIVE,
        UNKNOWN
    }

    @Override
    public void setOverDueColor() {
        textViewRecordHts.setBackground(getResources().getDrawable(R.drawable.record_btn_selector_overdue));

    }

    @Override
    protected ViewPager setupViewPager(ViewPager viewPager) {
        return null;
    }

    @Override
    protected void fetchProfileData() {
        //fetch profile data
    }

    @Override
    public void showProgressBar(boolean status) {
        progressBar.setVisibility(status ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void refreshMedicalHistory(boolean hasHistory) {
        showProgressBar(false);
        rlLastVisit.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
    }

    @Override
    public void refreshUpComingServicesStatus(String service, AlertStatus status, Date date) {
        showProgressBar(false);
        if (status == AlertStatus.complete) return;
        view_most_due_overdue_row.setVisibility(View.GONE);
        rlUpcomingServices.setVisibility(View.GONE);

        if (status == AlertStatus.upcoming) {
            tvUpComingServices.setText(HtsUtil.fromHtml(getString(R.string.vaccine_service_upcoming, service, dateFormat.format(date))));
        } else {
            tvUpComingServices.setText(HtsUtil.fromHtml(getString(R.string.vaccine_service_due, service, dateFormat.format(date))));
        }
    }

    @Override
    public void refreshFamilyStatus(AlertStatus status) {
        showProgressBar(false);
        if (status == AlertStatus.complete) {
            setFamilyStatus(getString(R.string.family_has_nothing_due));
        } else if (status == AlertStatus.normal) {
            setFamilyStatus(getString(R.string.family_has_services_due));
        } else if (status == AlertStatus.urgent) {
            tvFamilyStatus.setText(HtsUtil.fromHtml(getString(R.string.family_has_service_overdue)));
        }
    }

    private void setFamilyStatus(String familyStatus) {
        view_family_row.setVisibility(View.VISIBLE);
        rlFamilyServicesDue.setVisibility(View.GONE);
        tvFamilyStatus.setText(familyStatus);
    }

    @Override
    public void openMedicalHistory() {
        //implement
    }

    @Override
    public void openUpcomingService() {
        //implement
    }

    @Override
    public void openFamilyDueServices() {
        //implement
    }

    @Nullable
    private String formatTime(Date dateTime) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy");
            return formatter.format(dateTime);
        } catch (Exception e) {
            Timber.d(e);
        }
        return null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            profilePresenter.saveForm(data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON));
        }
    }

    protected boolean isVisitOnProgress(Visit visit) {

        return visit != null && !visit.getProcessed();
    }
}
