package com.epam.hytc.fulfilmentprocess.jobs;

import com.epam.hytc.core.model.ReturningCustomerSendEmailsProcessModel;
import com.epam.hytc.core.services.impl.DefaultHytcUserService;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.cronjob.AbstractJobPerformable;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.services.BaseStoreService;

import javax.annotation.Resource;
import java.util.List;

public class SendEmailToInactiveCustomersJob extends AbstractJobPerformable<CronJobModel> {

    @Resource
    private BusinessProcessService businessProcessService;
    @Resource
    private BaseSiteService baseSiteService;
    @Resource
    private BaseStoreService baseStoreService;
    @Resource
    private CommonI18NService commonI18NService;
    @Resource
    private DefaultHytcUserService defaultHytcUserService;

    private static final String ELECTRONICS_SITE_ID = "electronics";
    private static final String EN = "en";
    private static final String RETURNING_CUSTOMER_SEND_EMAIL_PROCESS = "ReturningCustomerSendEmailsProcess";

    @Override
    public PerformResult perform(CronJobModel cronJobModel) {
        List<CustomerModel> customers = defaultHytcUserService.findInactiveUsers();
        customers.forEach(customerModel -> createAndStartReturningCustomerSendEmailsProcess(customerModel));
        return new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED);
    }

    public void createAndStartReturningCustomerSendEmailsProcess(CustomerModel customer) {
        ReturningCustomerSendEmailsProcessModel returningCustomerSendEmailsProcess = businessProcessService.createProcess(RETURNING_CUSTOMER_SEND_EMAIL_PROCESS + "_" + System.currentTimeMillis(), RETURNING_CUSTOMER_SEND_EMAIL_PROCESS);
        returningCustomerSendEmailsProcess.setSite(baseSiteService.getBaseSiteForUID(ELECTRONICS_SITE_ID));
        returningCustomerSendEmailsProcess.setCustomer(customer);
        returningCustomerSendEmailsProcess.setLanguage(commonI18NService.getLanguage(EN));
        returningCustomerSendEmailsProcess.setStore(baseStoreService.getBaseStoreForUid(ELECTRONICS_SITE_ID));

        modelService.save(returningCustomerSendEmailsProcess);
        businessProcessService.startProcess(returningCustomerSendEmailsProcess);
    }
}
