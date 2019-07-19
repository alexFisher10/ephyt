package com.epam.hytc.core.services;

import com.epam.hytc.core.model.BonusAccountModel;

import java.math.BigDecimal;

public interface CustomerLevelConfigurationService {

    BigDecimal getBonusMultiplier(BonusAccountModel bonusAccount);
}
