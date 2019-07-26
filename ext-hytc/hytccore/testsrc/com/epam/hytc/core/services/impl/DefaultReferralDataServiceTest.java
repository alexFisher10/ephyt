package com.epam.hytc.core.services.impl;

import com.epam.hytc.core.dao.CustomerReferralCodeDao;
import com.epam.hytc.core.model.ReferralDataModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultReferralDataServiceTest {

    private static final String EXIST_REFERRAL_CODE = "existReferralCode";
    private static final String WRONG_REFERRAL_CODE = "wrongReferralCode";
    private static final String WITHOUT_REFERRAL_CODE = "";

    @Mock
    private ModelService modelService;
    @Mock
    private CustomerReferralCodeDao customerReferralCodeDao;
    @Mock
    private CustomerModel customer;
    @Mock
    private ReferralDataModel referralData;

    @InjectMocks
    private DefaultReferralDataService defaultReferralDataService;

    @Before
    public void setUp() throws Exception {
        doReturn(null).when(customerReferralCodeDao).findCustomerByReferralCode(WITHOUT_REFERRAL_CODE);
        doReturn(null).when(customerReferralCodeDao).findCustomerByReferralCode(WRONG_REFERRAL_CODE);
        doReturn(customer).when(customerReferralCodeDao).findCustomerByReferralCode(EXIST_REFERRAL_CODE);
        doReturn(referralData).when(modelService).create(ReferralDataModel.class);
        doReturn(customer).when(referralData).getNonAppliedCustomers();
        doNothing().when(modelService).save(referralData);
    }

    @Test
    public void shouldNotCreateReferralDataEntryWhenRegisterCustomerWithWrongReferralCode() {
        defaultReferralDataService.createReferralDataEntry(customer, WRONG_REFERRAL_CODE);
        verify(customerReferralCodeDao, atLeastOnce()).findCustomerByReferralCode(WRONG_REFERRAL_CODE);
        verify(modelService, never()).create(ReferralDataModel.class);
    }

    @Test
    public void shouldNotCreateReferralDataEntryWhenRegisterCustomerWithoutReferralCode() {
        defaultReferralDataService.createReferralDataEntry(customer, WITHOUT_REFERRAL_CODE);
        verify(customerReferralCodeDao, atLeastOnce()).findCustomerByReferralCode(WITHOUT_REFERRAL_CODE);
        verify(modelService, never()).create(ReferralDataModel.class);
    }

    @Test
    public void shouldCreateReferralDataEntryWhenRegisterCustomerWithReferralCode() {
        defaultReferralDataService.createReferralDataEntry(customer, EXIST_REFERRAL_CODE);
        verify(customerReferralCodeDao, atLeastOnce()).findCustomerByReferralCode(EXIST_REFERRAL_CODE);
        verify(modelService).create(ReferralDataModel.class);
    }
}