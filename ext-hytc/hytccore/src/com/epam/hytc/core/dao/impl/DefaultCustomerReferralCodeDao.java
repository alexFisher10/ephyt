package com.epam.hytc.core.dao.impl;

import com.epam.hytc.core.dao.CustomerReferralCodeDao;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import javax.annotation.Resource;
import java.util.List;

public class DefaultCustomerReferralCodeDao implements CustomerReferralCodeDao {

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Override
    public CustomerModel findCustomerByReferralCode(String referralCode) {
        CustomerModel customer = new CustomerModel();
        customer.setReferralCode(referralCode);
        List<CustomerModel> modelsByExample = flexibleSearchService.getModelsByExample(customer);
        if (modelsByExample.isEmpty()) {
            return null;
        } else {
            return modelsByExample.get(0);
        }
    }
}
