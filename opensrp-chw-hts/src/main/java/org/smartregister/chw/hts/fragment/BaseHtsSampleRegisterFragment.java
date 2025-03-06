package org.smartregister.chw.hts.fragment;

import android.widget.ImageView;
import android.widget.TextView;

import org.smartregister.chw.hts.R;
import org.smartregister.chw.hts.activity.BaseHtsProfileActivity;
import org.smartregister.chw.hts.model.BaseHtsRegisterFragmentModel;
import org.smartregister.chw.hts.presenter.BaseHtsSampleRegisterFragmentPresenter;
import org.smartregister.chw.hts.provider.HtsRegisterProvider;
import org.smartregister.commonregistry.CommonPersonObjectClient;
import org.smartregister.configurableviews.model.View;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.view.customcontrols.CustomFontTextView;
import org.smartregister.view.customcontrols.FontVariant;

import java.util.Set;

public class BaseHtsSampleRegisterFragment extends BaseHtsRegisterFragment {
    public static final String CLICK_VIEW_NORMAL = "click_view_normal";
    public static final String FOLLOW_UP_VISIT = "follow_up_visit";

    @Override
    public void setupViews(android.view.View view) {
        super.setupViews(view);

        CustomFontTextView titleView = view.findViewById(R.id.txt_title_label);
        if (titleView != null) {
            titleView.setVisibility(android.view.View.VISIBLE);
            titleView.setText(getString(R.string.hts_samples));
            titleView.setFontVariant(FontVariant.REGULAR);
        }
    }

    @Override
    public void initializeAdapter(Set<View> visibleColumns) {
        HtsRegisterProvider htsRegisterProvider = new HtsRegisterProvider(getActivity(), paginationViewHandler, registerActionHandler, visibleColumns);
        clientAdapter = new RecyclerViewPaginatedAdapter(null, htsRegisterProvider, context().commonrepository(this.tablename));
        clientAdapter.setCurrentlimit(20);
        clientsView.setAdapter(clientAdapter);
    }


    @Override
    protected void initializePresenter() {
        if (getActivity() == null) {
            return;
        }
        presenter = new BaseHtsSampleRegisterFragmentPresenter(this, new BaseHtsRegisterFragmentModel(), null);
    }

    @Override
    protected void onViewClicked(android.view.View view) {
        if (getActivity() == null || !(view.getTag() instanceof CommonPersonObjectClient)) {
            return;
        }

        CommonPersonObjectClient client = (CommonPersonObjectClient) view.getTag();
        if (view.getTag(R.id.VIEW_ID) == CLICK_VIEW_NORMAL) {
            openProfile(client.getCaseId());
        } else if (view.getTag(R.id.VIEW_ID) == FOLLOW_UP_VISIT) {
            openFollowUpVisit(client.getCaseId());
        }
    }

    protected void openProfile(String baseEntityId) {
        BaseHtsProfileActivity.startProfileActivity(getActivity(), baseEntityId);
    }

}
