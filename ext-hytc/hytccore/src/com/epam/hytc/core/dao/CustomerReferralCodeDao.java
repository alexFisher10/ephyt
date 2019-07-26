package com.epam.hytc.core.dao;

import de.hybris.platform.core.model.user.CustomerModel;

public interface CustomerReferralCodeDao {

    CustomerModel findCustomerByReferralCode(String referralCode);

}
