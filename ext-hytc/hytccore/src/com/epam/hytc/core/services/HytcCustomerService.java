package com.epam.hytc.core.services;

import de.hybris.platform.core.model.user.CustomerModel;

public interface HytcCustomerService {

    void updateTotalSpentForCustomerAfterPlacingOrder(CustomerModel customerModel, double totalPrice);

    void generateReferralCode(CustomerModel customerModel);
}
