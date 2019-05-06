package com.epam.hytc.fulfilmentprocess.actions.consignment;

import com.epam.hytc.core.services.HytcConfigurationService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.ordersplitting.model.ConsignmentProcessModel;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class CheckDeliveryDateAction extends AbstractSimpleDecisionAction<ConsignmentProcessModel> {

    private static final String QUANTITY_DAYS_FOR_START_DELAY_DELIVERY = "quantity.days.for.delay.delivery";

    @Resource
    private HytcConfigurationService hytcConfigurationService;

    @Override
    public Transition executeAction(final ConsignmentProcessModel consignmentProcessModel) {

        OrderModel orderModel = (OrderModel) consignmentProcessModel.getConsignment().getOrder();
        Date date = orderModel.getDeliveryDate();
        LocalDate deliveryDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate dateNowPlusDays = LocalDate.now().plusDays(hytcConfigurationService.getInt(QUANTITY_DAYS_FOR_START_DELAY_DELIVERY));
        if (deliveryDate.isAfter(dateNowPlusDays)) {
            return Transition.NOK;
        }
        return Transition.OK;
    }
}