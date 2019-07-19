package com.epam.hytc.core.services.impl;

import com.epam.hytc.core.enums.BonusHistoryOperationType;
import com.epam.hytc.core.model.BonusAccountModel;
import com.epam.hytc.core.model.BonusHistoryEntryModel;
import com.epam.hytc.core.model.ReturningCustomerTokenModel;
import com.epam.hytc.core.services.BonusAccountService;
import com.epam.hytc.core.services.BonusHistoryEntryService;
import com.epam.hytc.core.services.CustomerLevelConfigurationService;
import com.epam.hytc.core.services.HytcConfigurationService;
import com.epam.hytc.core.services.ReturningCustomerTokenService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.paginated.dao.PaginatedGenericDao;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static com.epam.hytc.core.constants.HytcCoreConstants.REGISTRATION_BONUS_AMOUNT;
import static com.epam.hytc.core.enums.BonusHistoryOperationType.INCOMING;
import static com.epam.hytc.core.enums.BonusHistoryOperationType.OUTGOING;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultBonusHistoryEntryService implements BonusHistoryEntryService {

    private static final String BONUS_ACCOUNT = "bonusAccount";
    private static final String CUSTOMER_MODEL = "CustomerModel";
    private static final String PAGINATION_DATA = "SearchPageData";
    private static final String RETURN_BONUS = "bonuses.for.returning.customer";
    private static final String ORDER_BONUSES_ADDING = "OrderBonusesAdding";

    @Resource
    private BonusAccountService bonusAccountService;
    @Resource
    private PaginatedGenericDao<BonusHistoryEntryModel> bonusHistoryEntryDao;
    @Resource
    private ModelService modelService;
    @Resource
    private ConfigurationService configurationService;
    @Resource
    private ReturningCustomerTokenService returningCustomerTokenService;
    @Resource
    private HytcConfigurationService hytcConfigurationService;
    @Resource
    private CustomerLevelConfigurationService customerLevelConfigurationService;
    @Resource
    private CartService cartService;

    @Override
    public SearchPageData<BonusHistoryEntryModel> getPagedBonusHistory(final CustomerModel customer, final SearchPageData searchPageData) {
        validateParameterNotNullStandardMessage(CUSTOMER_MODEL, customer);
        validateParameterNotNullStandardMessage(PAGINATION_DATA, searchPageData);
        checkBonusAccountAndAttach(customer);
        return bonusHistoryEntryDao.find(buildBonusHistoryEntryQueryParam(customer), searchPageData);
    }

    @Override
    public void createRegistrationBonusHistoryEntry(final BonusAccountModel bonusAccountModel) {
        final double amount = configurationService.getConfiguration().getDouble(REGISTRATION_BONUS_AMOUNT);
        createIncomingBonusHistoryEntry(bonusAccountModel, amount);
    }

    @Override
    public void createIncomingBonusHistoryEntry(final BonusAccountModel bonusAccountModel, final double amount) {
        if (isPositive(amount)) {
            createDefaultBonusHistoryEntry(bonusAccountModel, amount, INCOMING);
        }
    }

    @Override
    public void createOutgoingBonusHistoryEntry(final BonusAccountModel bonusAccountModel, final double amount) {
        if(isPositive(amount)){
            createDefaultBonusHistoryEntry(bonusAccountModel, amount, OUTGOING);
        }
    }

    @Override
    public void createReturningBonusHistoryEntry(CustomerModel customer) {
        validateParameterNotNullStandardMessage(CustomerModel._TYPECODE, customer);
        returningCustomerTokenService.getActiveTokenByCustomer(customer).ifPresent(this::addReturningBonuses);
    }

    @Override
    public void calculateBonusAfterPlaceOrder() {
        final CartModel cart = cartService.getSessionCart();
        final CustomerModel customer = (CustomerModel) cart.getUser();
        createOutgoingBonusHistoryEntry(customer.getBonusAccount(), cart.getBonusPaid());
        cart.getAllPromotionResults().stream()
                .filter(promotionResult -> promotionResult.getPromotion().getCode().equals(ORDER_BONUSES_ADDING))
                .forEach(promotionResult -> addBonusesToAccount(cart));
    }
    private void applyBonuses(final double bonusesAmount, final CustomerModel currentCustomer) {
        BonusAccountModel bonusAccount = currentCustomer.getBonusAccount();
        BigDecimal bonusMultiplier = customerLevelConfigurationService.getBonusMultiplier(bonusAccount);
        double finalBonusAmount = bonusMultiplier.multiply(BigDecimal.valueOf(bonusesAmount)).doubleValue();
        createIncomingBonusHistoryEntry(bonusAccount, finalBonusAmount);
    }

    private void createDefaultBonusHistoryEntry(final BonusAccountModel bonusAccountModel, final double amount, final BonusHistoryOperationType operation){
        final BonusHistoryEntryModel bonusHistoryEntryModel = modelService.create(BonusHistoryEntryModel.class);
        bonusHistoryEntryModel.setAmount(amount);
        bonusHistoryEntryModel.setDate(new Date());
        bonusHistoryEntryModel.setBonusAccount(bonusAccountModel);
        bonusHistoryEntryModel.setType(operation);

        modelService.save(bonusHistoryEntryModel);
    }

    private boolean isPositive(final double amount){
        return amount > 0;
    }

    private void checkBonusAccountAndAttach(final CustomerModel customer) {
        if (Objects.isNull(customer.getBonusAccount())) {
            bonusAccountService.attachNewBonusAccount(customer);
        }
    }

    private Map<String, BonusAccountModel> buildBonusHistoryEntryQueryParam(final CustomerModel customer) {
        return Collections.singletonMap(BONUS_ACCOUNT, customer.getBonusAccount());
    }

    private void addReturningBonuses(ReturningCustomerTokenModel token) {
        final double amount = hytcConfigurationService.getDouble(RETURN_BONUS);
        CustomerModel customer = token.getCustomer();
        checkBonusAccountAndAttach(customer);
        createIncomingBonusHistoryEntry(customer.getBonusAccount(), amount);
        token.setActive(false);
        modelService.save(token);
    }

    private void addBonusesToAccount(final CartModel cart) {
        final double bonusesAmount = cart.getBonuses();
        CustomerModel currentCustomer = (CustomerModel) cart.getUser();
        if (currentCustomer.getBonusAccount() == null) {
            bonusAccountService.attachNewBonusAccount(currentCustomer);
        }
        applyBonuses(bonusesAmount, currentCustomer);
    }
}