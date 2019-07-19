package com.epam.hytc.core.hook;

import com.epam.hytc.core.services.BonusHistoryEntryService;
import de.hybris.platform.commerceservices.order.hook.CommercePlaceOrderMethodHook;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.commerceservices.service.data.CommerceOrderResult;

import javax.annotation.Resource;

public class HytcBonusPlaceOrderMethodHook implements CommercePlaceOrderMethodHook {

    @Resource
    private BonusHistoryEntryService bonusHistoryEntryService;

    @Override
    public void afterPlaceOrder(final CommerceCheckoutParameter parameter, final CommerceOrderResult order) {
        bonusHistoryEntryService.calculateBonusAfterPlaceOrder();
    }

    @Override
    public void beforePlaceOrder(final CommerceCheckoutParameter parameter) {
        // not implemented
    }

    @Override
    public void beforeSubmitOrder(final CommerceCheckoutParameter parameter, final CommerceOrderResult result) {
        // not implemented
    }
}
