package org.smartregister.chw.hts.interactor;

import org.smartregister.chw.hts.domain.MemberObject;
import org.smartregister.chw.hts.activity.EntryActivity;


public class SkeletonServiceVisitInteractor extends BaseSkeletonServiceVisitInteractor {
    public SkeletonServiceVisitInteractor(String visitType) {
        super(visitType);
    }

    @Override
    public MemberObject getMemberClient(String memberID, String profileType) {
        return EntryActivity.getSampleMember();
    }
}
