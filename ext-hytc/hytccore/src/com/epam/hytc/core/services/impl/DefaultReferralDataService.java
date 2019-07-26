package com.epam.hytc.core.services.impl;

import com.epam.hytc.core.dao.CustomerReferralCodeDao;
import com.epam.hytc.core.model.ReferralDataModel;
import com.epam.hytc.core.services.ReferralDataService;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.model.ModelService;

import javax.annotation.Resource;

public class DefaultReferralDataService implements ReferralDataService {

    @Resource
    private ModelService modelService;
    @Resource
    private CustomerReferralCodeDao customerReferralCodeDao;

    @Override
    public void createReferralDataEntry(CustomerModel customer, String referralCode) {
        CustomerModel referralCodeOwner = customerReferralCodeDao.findCustomerByReferralCode(referralCode);
        if (referralCodeOwner != null) {
            ReferralDataModel referralData = modelService.create(ReferralDataModel.class);
            referralData.setCustomer(customer);
            referralData.setCodeOwner(referralCodeOwner);
            referralData.setNonAppliedCustomers(customer);
            modelService.save(referralData);
        }
    }
}