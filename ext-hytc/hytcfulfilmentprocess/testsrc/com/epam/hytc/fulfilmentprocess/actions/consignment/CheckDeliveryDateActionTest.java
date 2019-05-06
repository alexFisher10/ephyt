package com.epam.hytc.fulfilmentprocess.actions.consignment;

import com.epam.hytc.core.services.HytcConfigurationService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckDeliveryDateActionTest {
    private Transition result;
    private static final int QUANTITY_DAYS_ADD_TO_NOW = 4;

    @Mock
    private OrderModel order;
    @Mock
    private ConsignmentProcessModel consignmentProcess;
    @Mock
    private ConsignmentModel consignment;
    @Mock
    private HytcConfigurationService hytcConfigurationService;
    @Spy
    private Date date;
    @InjectMocks
    private CheckDeliveryDateAction checkDeliveryDateAction;

    @Before
    public void setUp() {
        doReturn(date).when(order).getDeliveryDate();
        doReturn(order).when(consignment).getOrder();
        doReturn(consignment).when(consignmentProcess).getConsignment();
    }

    @Test
    public void  shouldAssertNegativeResultWhenDeliveryDateLessThanTwoDaysAfterNow() {
        result = checkDeliveryDateAction.executeAction(consignmentProcess);

        assertEquals(Transition.OK, result);
    }

    @Test
    public void shouldAssertPositiveResultWhenDeliveryDateMoreThanTwoDaysAfterNow() {
        date.setDate(date.getDate() + QUANTITY_DAYS_ADD_TO_NOW);

        result = checkDeliveryDateAction.executeAction(consignmentProcess);

        assertEquals(Transition.NOK, result);
    }
}