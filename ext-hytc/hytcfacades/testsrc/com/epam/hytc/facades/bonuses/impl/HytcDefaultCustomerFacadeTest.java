package com.epam.hytc.facades.bonuses.impl;

import com.epam.hytc.core.services.BonusAccountService;
import com.epam.hytc.core.services.BonusHistoryEntryService;
import com.epam.hytc.core.services.HytcCustomerService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class HytcDefaultCustomerFacadeTest {

    private static final String STUB = "stub";
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
    private HytcCustomerService mockHytcCustomerService;
    @Spy
    private HytcDefaultCustomerFacade defaultCustomerFacade;

    @Before
    public void setUp() throws DuplicateUidException {
        ReflectionTestUtils.setField(defaultCustomerFacade, "userService", mockUserService);
        ReflectionTestUtils.setField(defaultCustomerFacade, "bonusAccountService", mockBonusAccountService);
        ReflectionTestUtils.setField(defaultCustomerFacade, "bonusHistoryEntryService", mockBonusHistoryEntryService);
        ReflectionTestUtils.setField(defaultCustomerFacade, "hytcCustomerService", mockHytcCustomerService);
        when(mockRegisterData.getLogin()).thenReturn(STUB);
        when(mockUserService.getUserForUID(STUB, CustomerModel.class)).thenReturn(mockCustomerModel);
        doNothing().when(defaultCustomerFacade).superRegister(mockRegisterData);
        doCallRealMethod().when(defaultCustomerFacade).register(mockRegisterData);
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
    public void shouldGenerateReferralCodeToRegisteredCustomer() throws DuplicateUidException {
        defaultCustomerFacade.register(mockRegisterData);

        verify(mockHytcCustomerService).generateReferralCode(mockCustomerModel);
    }
}