package com.epam.hytc.core.services;

import de.hybris.platform.core.model.user.CustomerModel;

public interface ReferralDataService {

    void createReferralDataEntry(CustomerModel customer, String referralCode);
}
