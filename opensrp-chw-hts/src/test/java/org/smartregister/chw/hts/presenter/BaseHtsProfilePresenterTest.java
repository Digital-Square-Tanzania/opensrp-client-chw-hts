package org.smartregister.chw.hts.presenter;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.smartregister.chw.hts.contract.HtsProfileContract;
import org.smartregister.chw.hts.domain.MemberObject;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class BaseHtsProfilePresenterTest {

    @Mock
    private HtsProfileContract.View view = Mockito.mock(HtsProfileContract.View.class);

    @Mock
    private HtsProfileContract.Interactor interactor = Mockito.mock(HtsProfileContract.Interactor.class);

    @Mock
    private MemberObject htsMemberObject = new MemberObject();

    private BaseHtsProfilePresenter profilePresenter = new BaseHtsProfilePresenter(view, interactor, htsMemberObject);


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
        profilePresenter.recordHtsButton("");
        verify(view).hideView();
    }

    @Test
    public void malariaTestDatePeriodIsMoreThanFourteen() {
        profilePresenter.recordHtsButton("EXPIRED");
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
