package com.epam.hytc.core.dao.impl;

import com.epam.hytc.core.dao.CustomerLevelConfigurationDao;
import com.epam.hytc.core.enums.CustomerLevel;
import com.epam.hytc.core.model.BonusAccountModel;
import com.epam.hytc.core.model.CustomerLevelConfigurationModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;

public class DefaultCustomerLevelConfigurationDao implements CustomerLevelConfigurationDao {

    private static final String CUSTOMER_LEVEL = "customerLevel";
    private static final String FIND_CUSTOMER_BONUS_MULTIPLIER = "SELECT {c.bonusMultiplier} FROM {CustomerLevelConfiguration AS c} WHERE {c.customerLevelType} = ?" + CUSTOMER_LEVEL;
    private static final String TOTAL_SPEND = "totalSpend";
    private static final String QUERY_TO_GET_CUSTOMER_LEVEL = "SELECT {pk} FROM {CustomerLevelConfiguration AS c} WHERE {c.minSpend} = ({{SELECT max({s.minSpend}) FROM {CustomerLevelConfiguration as s} WHERE  {s.minSpend} <= ?"+TOTAL_SPEND+"}})";

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Override
    public CustomerLevel getCustomerLevelByTotalSpend(BigDecimal totalSpend) {
        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(QUERY_TO_GET_CUSTOMER_LEVEL);
        flexibleSearchQuery.addQueryParameter(TOTAL_SPEND, totalSpend);
        CustomerLevelConfigurationModel customerLevelConfigurationModel = flexibleSearchService.searchUnique(flexibleSearchQuery);
        return customerLevelConfigurationModel.getCustomerLevelType();
    }

    @Override
    public BigDecimal getBonusMultiplier(BonusAccountModel bonusAccount) {
        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(FIND_CUSTOMER_BONUS_MULTIPLIER);
        flexibleSearchQuery.setResultClassList(Collections.singletonList(BigDecimal.class));
        flexibleSearchQuery.addQueryParameter(CUSTOMER_LEVEL, bonusAccount.getAccountLevel());
        return flexibleSearchService.searchUnique(flexibleSearchQuery);
    }
}
