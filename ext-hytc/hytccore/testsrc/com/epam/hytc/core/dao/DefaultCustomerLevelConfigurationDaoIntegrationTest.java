package com.epam.hytc.core.dao;

import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.impex.jalo.ImpExException;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.user.UserService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@IntegrationTest
public class DefaultCustomerLevelConfigurationDaoIntegrationTest extends ServicelayerTransactionalTest {

    private static final double BONUS_AMOUNT = 10d;
    private static final double TOTAL_BONUS_AMOUNT = 30d;
    private static final double MULTIPLIER_FOR_BRONZE = 1.0;
    private static final double MULTIPLIER_FOR_SILVER = 2.0;
    private static final double MULTIPLIER_FOR_GOLD = 3.0;
    private static final String CUSTOMER1_UID = "firstcustomeruid";
    private CustomerModel customer;
    private BigDecimal bonusMultiplier;

    @Resource
    private CustomerLevelConfigurationDao customerLevelConfigurationDao;
    @Resource
    private UserService userService;

    @Before
    public void setUp() throws ImpExException {
        importCsv("/test/CustomerLevelConfigurationDaoIntegrationTest/testData.impex", StandardCharsets.UTF_8.name());
        importCsv("/hytccore/test/testDefaultCustomerLevelConfigurationDaoIntegrationTest.impex", StandardCharsets.UTF_8.name());

        customer = (CustomerModel) userService.getUserForUID(CUSTOMER1_UID);
    }

    @Test
    public void shouldMultiplyBonusForGoldLevelTypeCustomer() {
        bonusMultiplier = customerLevelConfigurationDao.getBonusMultiplier(customer.getBonusAccount());

        assertEquals(bonusMultiplier.multiply(BigDecimal.valueOf(BONUS_AMOUNT)).doubleValue(), TOTAL_BONUS_AMOUNT, 0);
    }

    @Test
    public void shouldEqualsForCustomerMultiplierWithAccountLevelGold() {
        bonusMultiplier = customerLevelConfigurationDao.getBonusMultiplier(customer.getBonusAccount());
        assertEquals(bonusMultiplier.doubleValue(), MULTIPLIER_FOR_GOLD, 0);
        assertNotEquals(bonusMultiplier.doubleValue(), MULTIPLIER_FOR_SILVER, 0);
        assertNotEquals(bonusMultiplier.doubleValue(), MULTIPLIER_FOR_BRONZE, 0);
    }
}