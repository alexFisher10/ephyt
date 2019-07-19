package com.epam.hytc.core.dao;

import com.epam.hytc.core.enums.CustomerLevel;
import com.epam.hytc.core.model.BonusAccountModel;

import java.math.BigDecimal;

public interface CustomerLevelConfigurationDao {
    CustomerLevel getCustomerLevelByTotalSpend(BigDecimal totalSpend);

    BigDecimal getBonusMultiplier(BonusAccountModel bonusAccount);
}
