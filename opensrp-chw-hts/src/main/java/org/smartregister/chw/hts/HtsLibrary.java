package org.smartregister.chw.hts;

import org.smartregister.Context;
import org.smartregister.CoreLibrary;
import org.smartregister.chw.hts.repository.VisitDetailsRepository;
import org.smartregister.chw.hts.repository.VisitRepository;
import org.smartregister.repository.Repository;
import org.smartregister.sync.ClientProcessorForJava;
import org.smartregister.sync.helper.ECSyncHelper;

import id.zelory.compressor.Compressor;

public class HtsLibrary {
    private static HtsLibrary instance;
    private static boolean submitOnSave = false;
    private final Context context;
    private final Repository repository;
    private int applicationVersion;
    private int databaseVersion;
    private ECSyncHelper syncHelper;
    private ClientProcessorForJava clientProcessorForJava;
    private VisitRepository visitRepository;
    private VisitDetailsRepository visitDetailsRepository;
    private String sourceDateFormat = "dd-MM-yyyy";
    private String saveDateFormat = "yyyy-MM-dd";
    private HtsLibrary(Context contextArg, Repository repositoryArg, int applicationVersion, int databaseVersion) {
        this.context = contextArg;
        this.repository = repositoryArg;
        this.applicationVersion = applicationVersion;
        this.databaseVersion = databaseVersion;
    }

    public static boolean isSubmitOnSave() {
        return submitOnSave;
    }

    public void setSubmitOnSave(boolean submitOnSave) {
        HtsLibrary.submitOnSave = submitOnSave;
    }

    public static void init(Context context, Repository repository, int applicationVersion, int databaseVersion) {
        if (instance == null) {
            instance = new HtsLibrary(context, repository, applicationVersion, databaseVersion);
        }
    }

    public static HtsLibrary getInstance() {
        if (instance == null) {
            throw new IllegalStateException(" Instance does not exist!!! Call "
                    + CoreLibrary.class.getName()
                    + ".init method in the onCreate method of "
                    + "your Application class ");
        }
        return instance;
    }

    public String getSaveDateFormat() {
        return saveDateFormat;
    }

    public void setSaveDateFormat(String saveDateFormat) {
        this.saveDateFormat = saveDateFormat;
    }

    public String getSourceDateFormat() {
        return sourceDateFormat;
    }

    public void setSourceDateFormat(String sourceDateFormat) {
        this.sourceDateFormat = sourceDateFormat;
    }

    public Context context() {
        return context;
    }

    public Repository getRepository() {
        return repository;
    }

    public int getApplicationVersion() {
        return applicationVersion;
    }

    public int getDatabaseVersion() {
        return databaseVersion;
    }

    public ECSyncHelper getEcSyncHelper() {
        if (syncHelper == null) {
            syncHelper = ECSyncHelper.getInstance(context().applicationContext());
        }
        return syncHelper;
    }

    public ClientProcessorForJava getClientProcessorForJava() {
        if (clientProcessorForJava == null) {
            clientProcessorForJava = ClientProcessorForJava.getInstance(context().applicationContext());
        }
        return clientProcessorForJava;
    }

    public void setClientProcessorForJava(ClientProcessorForJava clientProcessorForJava) {
        this.clientProcessorForJava = clientProcessorForJava;
    }

    public VisitRepository visitRepository() {
        if (visitRepository == null) {
            visitRepository = new VisitRepository();
        }
        return visitRepository;
    }

    public VisitDetailsRepository visitDetailsRepository() {
        if (visitDetailsRepository == null) {
            visitDetailsRepository = new VisitDetailsRepository();
        }
        return visitDetailsRepository;
    }

}
