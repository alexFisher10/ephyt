package com.epam.hytc.core.services.impl;

import com.epam.hytc.core.dao.CustomerLevelConfigurationDao;
import com.epam.hytc.core.model.BonusAccountModel;
import com.epam.hytc.core.services.CustomerLevelConfigurationService;

import javax.annotation.Resource;
import java.math.BigDecimal;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

public class DefaultCustomerLevelConfigurationService implements CustomerLevelConfigurationService {

    private static final String CUSTOMER_NULL_MESSAGE = "Customer model must not be null!";

    @Resource
    private CustomerLevelConfigurationDao customerLevelConfigurationDao;

    @Override
    public BigDecimal getBonusMultiplier(BonusAccountModel bonusAccount) {
        validateParameterNotNull(bonusAccount, CUSTOMER_NULL_MESSAGE);
        return customerLevelConfigurationDao.getBonusMultiplier(bonusAccount);
    }
}
