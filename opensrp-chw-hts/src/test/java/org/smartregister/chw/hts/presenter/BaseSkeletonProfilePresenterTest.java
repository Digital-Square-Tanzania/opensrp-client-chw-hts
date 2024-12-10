package org.smartregister.chw.hts.presenter;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.smartregister.chw.hts.contract.SkeletonProfileContract;
import org.smartregister.chw.hts.domain.MemberObject;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class BaseSkeletonProfilePresenterTest {

    @Mock
    private SkeletonProfileContract.View view = Mockito.mock(SkeletonProfileContract.View.class);

    @Mock
    private SkeletonProfileContract.Interactor interactor = Mockito.mock(SkeletonProfileContract.Interactor.class);

    @Mock
    private MemberObject htsMemberObject = new MemberObject();

    private BaseSkeletonProfilePresenter profilePresenter = new BaseSkeletonProfilePresenter(view, interactor, htsMemberObject);


    @Test
    public void fillProfileDataCallsSetProfileViewWithDataWhenPassedMemberObject() {
        profilePresenter.fillProfileData(htsMemberObject);
        verify(view).setProfileViewWithData();
    }

    @Test
    public void fillProfileDataDoesntCallsSetProfileViewWithDataIfMemberObjectEmpty() {
        profilePresenter.fillProfileData(null);
        verify(view, never()).setProfileViewWithData();
    }

    @Test
    public void malariaTestDatePeriodIsLessThanSeven() {
        profilePresenter.recordSkeletonButton("");
        verify(view).hideView();
    }

    @Test
    public void malariaTestDatePeriodIsMoreThanFourteen() {
        profilePresenter.recordSkeletonButton("EXPIRED");
        verify(view).hideView();
    }

    @Test
    public void refreshProfileBottom() {
        profilePresenter.refreshProfileBottom();
        verify(interactor).refreshProfileInfo(htsMemberObject, profilePresenter.getView());
    }

    @Test
    public void saveForm() {
        profilePresenter.saveForm(null);
        verify(interactor).saveRegistration(null, view);
    }
}
