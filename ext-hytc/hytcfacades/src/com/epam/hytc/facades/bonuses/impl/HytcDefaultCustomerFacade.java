package com.epam.hytc.facades.bonuses.impl;

import com.epam.hytc.core.services.BonusAccountService;
import com.epam.hytc.core.services.BonusHistoryEntryService;
import com.epam.hytc.core.services.ReferralDataService;
import de.hybris.platform.commercefacades.customer.impl.DefaultCustomerFacade;
import de.hybris.platform.commercefacades.user.data.RegisterData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.core.model.user.CustomerModel;

import javax.annotation.Resource;

public class HytcDefaultCustomerFacade extends DefaultCustomerFacade {

    @Resource
    private BonusAccountService bonusAccountService;
    @Resource
    private BonusHistoryEntryService bonusHistoryEntryService;
    @Resource
    private ReferralDataService referralDataService;

    @Override
    public void register(final RegisterData registerData) throws DuplicateUidException {
        superRegister(registerData);
        final String userId = registerData.getLogin().toLowerCase();
        final CustomerModel customerModel = getUserService().getUserForUID(userId, CustomerModel.class);
        bonusAccountService.attachNewBonusAccount(customerModel);
        bonusHistoryEntryService.createRegistrationBonusHistoryEntry(customerModel.getBonusAccount());

        String referralCode = registerData.getReferralCode();
        if (!referralCode.isEmpty()) {
            referralDataService.createReferralDataEntry(customerModel, referralCode);
        }
    }

    protected void superRegister(final RegisterData registerData) throws DuplicateUidException {
        super.register(registerData);
    }
}
