package com.epam.hytc.core.services.impl;

import com.epam.hytc.core.model.BonusAccountModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultHytcCustomerServiceUnitTest {

    private final static double CURRENT_ORDER_TOTAL_PRICE = 159.0;
    private final static double TOTAL_SPEND = 160.0;
    private final static double EXPECTED_TOTAL_SPEND = 319.0;
    private static final String EXCEPTION_MESSAGE = "Parameter Customer can not be null";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private CustomerModel customer;
    @Mock
    private ModelService modelService;
    @Mock
    private OrderModel order;

    @InjectMocks
    private DefaultHytcCustomerService defaultHytcCustomerService;

    @Before
    public void setUp() {
        when(order.getTotalPrice()).thenReturn(CURRENT_ORDER_TOTAL_PRICE);
        customer.setTotalSpent(new BigDecimal(TOTAL_SPEND));
        customer.setBonusAccount(new BonusAccountModel());
        when(customer.getTotalSpent()).thenReturn(new BigDecimal(TOTAL_SPEND).add(BigDecimal.valueOf(CURRENT_ORDER_TOTAL_PRICE)));
    }

    @Test
    public void assertRecalculatedTotalSpendAndModelSaving() {
        defaultHytcCustomerService.updateTotalSpentForCustomerAfterPlacingOrder(customer, CURRENT_ORDER_TOTAL_PRICE);
        assertEquals(EXPECTED_TOTAL_SPEND, customer.getTotalSpent().doubleValue(), 0.0);
        verify(modelService, atLeastOnce()).save(customer);
    }

    @Test
    public void shouldReturnCustomerWithFilledReferralCodeWhenGenerateReferralCodeWasExecuted() {
        defaultHytcCustomerService.generateReferralCode(customer);

        verify(customer).setReferralCode(anyString());
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWithExpectedMessageWhenCustomerModelIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(EXCEPTION_MESSAGE);

        defaultHytcCustomerService.generateReferralCode(null);
    }
}
