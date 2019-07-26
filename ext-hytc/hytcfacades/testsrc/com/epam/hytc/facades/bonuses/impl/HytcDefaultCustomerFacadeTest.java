package com.epam.hytc.facades.bonuses.impl;

import com.epam.hytc.core.services.BonusAccountService;
import com.epam.hytc.core.services.BonusHistoryEntryService;
import com.epam.hytc.core.services.ReferralDataService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.user.data.RegisterData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class HytcDefaultCustomerFacadeTest {

    private static final String STUB = "stub";
    private static final String EXIST_REFERRAL_CODE = "existReferralCode";
    private static final String EMPTY_REFERRAL_CODE = "";
    @Mock
    private BonusAccountService mockBonusAccountService;
    @Mock
    private RegisterData mockRegisterData;
    @Mock
    private UserService mockUserService;
    @Mock
    private CustomerModel mockCustomerModel;
    @Mock
    private BonusHistoryEntryService mockBonusHistoryEntryService;
    @Mock
    private ReferralDataService referralDataService;
    @Spy
    private HytcDefaultCustomerFacade defaultCustomerFacade;

    @Before
    public void setUp() throws DuplicateUidException {
        ReflectionTestUtils.setField(defaultCustomerFacade, "userService", mockUserService);
        ReflectionTestUtils.setField(defaultCustomerFacade, "bonusAccountService", mockBonusAccountService);
        ReflectionTestUtils.setField(defaultCustomerFacade, "bonusHistoryEntryService", mockBonusHistoryEntryService);
        ReflectionTestUtils.setField(defaultCustomerFacade, "referralDataService", referralDataService);
        when(mockRegisterData.getLogin()).thenReturn(STUB);
        when(mockUserService.getUserForUID(STUB, CustomerModel.class)).thenReturn(mockCustomerModel);
        doNothing().when(defaultCustomerFacade).superRegister(mockRegisterData);
        doCallRealMethod().when(defaultCustomerFacade).register(mockRegisterData);
        when(mockRegisterData.getReferralCode()).thenReturn(EXIST_REFERRAL_CODE);
    }

    @Test
    public void shouldGetUserForUIDBeforeAttachBonusAccount() throws DuplicateUidException {
        defaultCustomerFacade.register(mockRegisterData);

        verify(mockUserService).getUserForUID(STUB, CustomerModel.class);
    }

    @Test
    public void shouldAttachNewBonusAccountToRegisteredCustomer() throws DuplicateUidException {
        defaultCustomerFacade.register(mockRegisterData);

        verify(mockBonusAccountService).attachNewBonusAccount(mockCustomerModel);
    }

    @Test
    public void shouldCreateRegistrationBonusHistoryEntryWhenANewCustomerRegisters() throws DuplicateUidException {
        defaultCustomerFacade.register(mockRegisterData);

        verify(mockBonusHistoryEntryService).createRegistrationBonusHistoryEntry(mockCustomerModel.getBonusAccount());
    }

    @Test
    public void shouldCreateReferralDataEntryToRegisteredCustomer() throws DuplicateUidException {
        defaultCustomerFacade.register(mockRegisterData);

        verify(referralDataService).createReferralDataEntry(mockCustomerModel, EXIST_REFERRAL_CODE);
    }

    @Test
    public void shouldNotCreateReferralDataEntryToRegisteredCustomer() throws DuplicateUidException {
        when(mockRegisterData.getReferralCode()).thenReturn(EMPTY_REFERRAL_CODE);

        defaultCustomerFacade.register(mockRegisterData);

        verify(referralDataService, never()).createReferralDataEntry(mockCustomerModel, EMPTY_REFERRAL_CODE);
    }
}