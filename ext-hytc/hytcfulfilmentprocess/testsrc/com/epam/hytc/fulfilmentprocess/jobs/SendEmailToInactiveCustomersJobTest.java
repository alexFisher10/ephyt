package com.epam.hytc.fulfilmentprocess.jobs;

import com.epam.hytc.core.model.ReturningCustomerSendEmailsProcessModel;
import com.epam.hytc.core.model.ReturningCustomerTokenModel;
import com.epam.hytc.core.services.impl.DefaultHytcUserService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.cronjob.model.CronJobModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.site.BaseSiteService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class SendEmailToInactiveCustomersJobTest {

    private static final String URL = "url";
    private static final String TOKEN_CODE = "tokenCode";

    private PerformResult result;
    private List<CustomerModel> customers;

    @Mock
    private ReturningCustomerTokenModel returningCustomerToken;
    @Mock
    private CustomerModel customer;
    @Mock
    private CronJobModel cronJob;
    @Mock
    private ReturningCustomerSendEmailsProcessModel returningCustomerSendEmailsProcess;
    @Mock
    private BaseSiteModel baseSite;
    @Mock
    private BaseStoreModel baseStore;
    @Mock
    private LanguageModel language;
    @Mock
    private ModelService modelService;
    @Mock
    private BusinessProcessService businessProcessService;
    @Mock
    private BaseSiteService baseSiteService;
    @Mock
    private BaseStoreService baseStoreService;
    @Mock
    private CommonI18NService commonI18NService;
    @Mock
    private DefaultHytcUserService defaultHytcUserService;

    @InjectMocks
    private SendEmailToInactiveCustomersJob sendEmailToInactiveCustomersJob;

    @Before
    public void setUp() {
        customers = new ArrayList<>();
        doReturn(baseSite).when(baseSiteService).getBaseSiteForUID(anyString());
        doReturn(baseStore).when(baseStoreService).getBaseStoreForUid(anyString());
        doReturn(language).when(commonI18NService).getLanguage(anyString());
        doReturn(TOKEN_CODE).when(returningCustomerToken).getCode();
        doReturn(returningCustomerSendEmailsProcess).when(businessProcessService).createProcess(anyString(), anyString());
        doReturn(customers).when(defaultHytcUserService).findInactiveUsers();
        Configuration configuration = mock(Configuration.class);
        doReturn(URL).when(configuration).getString(anyString());
    }

    @Test
    public void shouldSuccessfullyStartProcessWhenLongPeriodInactiveCustomerExists() {
        customers.add(customer);
        result = sendEmailToInactiveCustomersJob.perform(cronJob);

        verify(businessProcessService).startProcess(returningCustomerSendEmailsProcess);

        assertEquals(CronJobResult.SUCCESS, result.getResult());
        assertEquals(CronJobStatus.FINISHED, result.getStatus());
    }

    @Test
    public void shouldNotStartProcessWhenLongPeriodInactiveCustomerNotExists() {
        result = sendEmailToInactiveCustomersJob.perform(cronJob);

        verify(businessProcessService, never()).createProcess(anyString(), anyString());
        verify(returningCustomerSendEmailsProcess, never()).setSite(baseSite);
        verify(returningCustomerSendEmailsProcess, never()).setCustomer(customer);
        verify(returningCustomerSendEmailsProcess, never()).setLanguage(language);
        verify(returningCustomerSendEmailsProcess, never()).setStore(baseStore);
        verify(modelService, never()).save(returningCustomerSendEmailsProcess);
        verify(businessProcessService, never()).startProcess(returningCustomerSendEmailsProcess);
        assertEquals(CronJobResult.SUCCESS, result.getResult());
        assertEquals(CronJobStatus.FINISHED, result.getStatus());
    }
}