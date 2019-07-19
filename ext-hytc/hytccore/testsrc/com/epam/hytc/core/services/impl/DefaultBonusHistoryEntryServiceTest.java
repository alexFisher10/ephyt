package com.epam.hytc.core.services.impl;

import com.epam.hytc.core.model.BonusAccountModel;
import com.epam.hytc.core.model.BonusHistoryEntryModel;
import com.epam.hytc.core.model.OrderAddBonusesPromotionModel;
import com.epam.hytc.core.model.ReturningCustomerTokenModel;
import com.epam.hytc.core.services.BonusAccountService;
import com.epam.hytc.core.services.BonusHistoryEntryService;
import com.epam.hytc.core.services.CustomerLevelConfigurationService;
import com.epam.hytc.core.services.HytcConfigurationService;
import com.epam.hytc.core.services.ReturningCustomerTokenService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;
import de.hybris.platform.order.CartService;
import de.hybris.platform.promotions.model.AbstractPromotionModel;
import de.hybris.platform.promotions.model.ProductPercentageDiscountPromotionModel;
import de.hybris.platform.promotions.model.PromotionResultModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.paginated.dao.PaginatedGenericDao;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.epam.hytc.core.constants.HytcCoreConstants.REGISTRATION_BONUS_AMOUNT;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultBonusHistoryEntryServiceTest {

    private static final String BONUS_ACCOUNT = "bonusAccount";
    private static final String CUSTOMER_EXCEPTION_MESSAGE = "Parameter CustomerModel can not be null";
    private static final String PAGED_EXCEPTION_MESSAGE = "Parameter SearchPageData can not be null";
    private static final double POSITIVE_CUSTOMER_RETURNING_BONUS_AMOUNT =  111;
    private static final double NEGATIVE_CUSTOMER_RETURNING_BONUS_AMOUNT =  -111;
    private static final double BONUS_AMOUNT = 10d;
    private static final String VALID_PROMOTION_CODE = "OrderBonusesAdding";
    private static final String INVALID_PROMOTION_CODE = "10DiscountProduct";
    private static final BigDecimal BONUS_MULTIPLIER = BigDecimal.valueOf(2);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    @Mock
    private BonusAccountService mockBonusAccountService;
    @Mock
    private PaginatedGenericDao<BonusHistoryEntryModel> mockBonusHistoryEntryDao;
    @Mock
    private CustomerModel mockCustomerModel;
    @Mock
    private SearchPageData mockSearchPageData;
    @Mock
    private BonusAccountModel mockBonusAccountModel;
    @Mock
    private BonusHistoryEntryModel mockBonusHistoryEntryModel;
    @Mock
    private ModelService mockModelService;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConfigurationService mockConfigurationService;
    @Mock
    private ReturningCustomerTokenModel mockReturningCustomerTokenModel;
    @Mock
    private ReturningCustomerTokenService mockReturningCustomerTokenService;
    @Mock
    private HytcConfigurationService hytcConfigurationService;
    @Mock
    private BonusHistoryEntryService mockBonusHistoryEntryService;
    @Mock
    private CartService mockCartService;
    @Mock
    private CartModel mockCartModel;
    @Mock
    private CustomerLevelConfigurationService customerLevelConfigurationService;

    private Set<PromotionResultModel> promotionResultSet;
    private PromotionResultModel promotionResult;
    private AbstractPromotionModel promotionModel;

    @InjectMocks
    private DefaultBonusHistoryEntryService bonusHistoryEntryService;

    @Before
    public void before(){
        when(mockReturningCustomerTokenModel.getCustomer()).thenReturn(mockCustomerModel);
        when(mockModelService.create(BonusHistoryEntryModel.class)).thenReturn(mockBonusHistoryEntryModel);
        doNothing().when(mockBonusAccountService).attachNewBonusAccount(mockCustomerModel);
        doReturn(mockCartModel).when(mockCartService).getSessionCart();
        doReturn(mockCustomerModel).when(mockCartModel).getUser();
        doReturn(BONUS_AMOUNT).when(mockCartModel).getBonuses();
        doReturn(mockBonusAccountModel).when(mockCustomerModel).getBonusAccount();

        doReturn(BONUS_MULTIPLIER).when(customerLevelConfigurationService).getBonusMultiplier(mockBonusAccountModel);
    }

    @Test
    public void shouldNotAddBonusHistoryEntryToAccountInvalidPromotion() {
        prepareInvalidPromotion();
        doReturn(promotionResultSet).when(mockCartModel).getAllPromotionResults();
        bonusHistoryEntryService.calculateBonusAfterPlaceOrder();
        verify(mockBonusHistoryEntryService, never()).createIncomingBonusHistoryEntry(mockCustomerModel.getBonusAccount(), BONUS_AMOUNT);
    }

    @Test
    public void shouldAddBonusHistoryEntryToAccountValidPromotion() {
        prepareValidPromotion();
        doReturn(promotionResultSet).when(mockCartModel).getAllPromotionResults();
        bonusHistoryEntryService.calculateBonusAfterPlaceOrder();

        verify(mockModelService).save(mockBonusHistoryEntryModel);
    }

    @Test
    public void shouldThrowNullArgumentExceptionWithExpectedMessageWhenSearchPageDataIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(PAGED_EXCEPTION_MESSAGE);

        bonusHistoryEntryService.getPagedBonusHistory(mockCustomerModel, null);
    }

    @Test
    public void shouldThrowNullArgumentExceptionWithExpectedMessageWhenCustomerIsNull() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(CUSTOMER_EXCEPTION_MESSAGE);

        bonusHistoryEntryService.getPagedBonusHistory(null, mockSearchPageData);
    }

    @Test
    public void shouldNotAttachBonusAccountWhenExist() {
        when(mockCustomerModel.getBonusAccount()).thenReturn(mockBonusAccountModel);

        bonusHistoryEntryService.getPagedBonusHistory(mockCustomerModel, mockSearchPageData);

        verify(mockBonusAccountService, never()).attachNewBonusAccount(mockCustomerModel);
    }

    @Test
    public void shouldFindPagedDataByIncomingParams() {
        final Map<String, BonusAccountModel> stubQueryParam = buildBonusHistoryEntryQueryParam();
        when(mockCustomerModel.getBonusAccount()).thenReturn(mockBonusAccountModel);

        bonusHistoryEntryService.getPagedBonusHistory(mockCustomerModel, mockSearchPageData);

        verify(mockBonusHistoryEntryDao).find(stubQueryParam, mockSearchPageData);
    }

    @Test
    public void shouldSaveANewBonusHistoryEntryWhenRegistrationBonusAmountGreaterThanZero() {
        when(mockConfigurationService.getConfiguration().getDouble(REGISTRATION_BONUS_AMOUNT)).thenReturn(15.01);

        bonusHistoryEntryService.createRegistrationBonusHistoryEntry(mockBonusAccountModel);
        verify(mockModelService).save(mockBonusHistoryEntryModel);
    }

    @Test
    public void shouldNotSaveANewBonusHistoryEntryWhenRegistrationBonusAmountIsZero() {
        when(mockConfigurationService.getConfiguration().getDouble(REGISTRATION_BONUS_AMOUNT)).thenReturn(0.0);

        bonusHistoryEntryService.createRegistrationBonusHistoryEntry(mockBonusAccountModel);
        verify(mockModelService, never()).save(mockBonusHistoryEntryModel);
    }

    @Test
    public void shouldNotSaveANewBonusHistoryEntryWhenRegistrationBonusAmountIsNegative() {
        when(mockConfigurationService.getConfiguration().getDouble(REGISTRATION_BONUS_AMOUNT)).thenReturn(-3.0);

        bonusHistoryEntryService.createRegistrationBonusHistoryEntry(mockBonusAccountModel);
        verify(mockModelService, never()).save(mockBonusHistoryEntryModel);
    }

    @Test
    public void shouldAddNewBonusHistoryEntryWhenBonusAmountGreaterThanZero(){
        double bonusAmount = 10.0;

        bonusHistoryEntryService.createIncomingBonusHistoryEntry(mockBonusAccountModel, bonusAmount);
        verify(mockModelService).save(mockBonusHistoryEntryModel);
    }

    @Test
    public void shouldNotAddNewBonusHistoryEntryWhenBonusAmountIsZero(){
        double bonusAmount = 0;

        bonusHistoryEntryService.createIncomingBonusHistoryEntry(mockBonusAccountModel, bonusAmount);
        verify(mockModelService, never()).save(mockBonusHistoryEntryModel);
    }

    @Test
    public void shouldNotAddNewBonusHistoryEntryWhenBonusAmountIsNegative(){
        double bonusAmount = -10.0;

        bonusHistoryEntryService.createIncomingBonusHistoryEntry(mockBonusAccountModel, bonusAmount);
        verify(mockModelService, never()).save(mockBonusHistoryEntryModel);
    }

    @Test
    public void shouldAddNewOutgoingBonusHistoryEntryWhenBonusAmountGreaterThanZero(){
        double bonusAmount = 10.0;

        bonusHistoryEntryService.createOutgoingBonusHistoryEntry(mockBonusAccountModel, bonusAmount);
        verify(mockModelService).save(mockBonusHistoryEntryModel);
    }

    @Test
    public void shouldNotAddNewOutgoingBonusHistoryEntryWhenBonusAmountIsZero(){
        double bonusAmount = 0;

        bonusHistoryEntryService.createOutgoingBonusHistoryEntry(mockBonusAccountModel, bonusAmount);
        verify(mockModelService, never()).save(mockBonusHistoryEntryModel);
    }

    @Test
    public void shouldNotAddNewOutgoingBonusHistoryEntryWhenBonusAmountIsNegative(){
        double bonusAmount = -10.0;

        bonusHistoryEntryService.createOutgoingBonusHistoryEntry(mockBonusAccountModel, bonusAmount);
        verify(mockModelService, never()).save(mockBonusHistoryEntryModel);
    }

    @Test
    public void shouldAddReturningCustomerBonusHistoryEntryWhenBonusTokenIsActiveAndAmountGreaterThenZero() {
        when(hytcConfigurationService.getDouble(anyString())).thenReturn(POSITIVE_CUSTOMER_RETURNING_BONUS_AMOUNT);
        when(mockReturningCustomerTokenModel.isActive()).thenReturn(true);
        when(mockReturningCustomerTokenService.getActiveTokenByCustomer(mockCustomerModel)).thenReturn(Optional.of(mockReturningCustomerTokenModel));

        bonusHistoryEntryService.createReturningBonusHistoryEntry(mockCustomerModel);

        verify(mockModelService).save(mockReturningCustomerTokenModel);
        verify(mockModelService).save(mockBonusHistoryEntryModel);
    }

    @Test
    public void shouldNotAddReturningCustomerBonusHistoryEntryWhenBonusTokenIsActiveAndAmountLessThenZero() {
        when(hytcConfigurationService.getDouble(anyString())).thenReturn(NEGATIVE_CUSTOMER_RETURNING_BONUS_AMOUNT);
        when(mockReturningCustomerTokenService.getActiveTokenByCustomer(mockCustomerModel)).thenReturn(Optional.of(mockReturningCustomerTokenModel));

        bonusHistoryEntryService.createReturningBonusHistoryEntry(mockCustomerModel);

        verify(mockModelService).save(mockReturningCustomerTokenModel);
        verify(mockModelService, never()).save(mockBonusHistoryEntryModel);
    }

    @Test
    public void shouldNotAddReturningCustomerBonusHistoryEntryWhenTokenIsNullOrWasInactive() {
        when(hytcConfigurationService.getDouble(anyString())).thenReturn(POSITIVE_CUSTOMER_RETURNING_BONUS_AMOUNT);
        when(mockReturningCustomerTokenService.getActiveTokenByCustomer(mockCustomerModel)).thenReturn(Optional.empty());

        bonusHistoryEntryService.createReturningBonusHistoryEntry(mockCustomerModel);

        verify(mockModelService, never()).save(mockReturningCustomerTokenModel);
        verify(mockModelService, never()).save(mockBonusHistoryEntryModel);
    }

    private Map<String, BonusAccountModel> buildBonusHistoryEntryQueryParam() {
        return Collections.singletonMap(BONUS_ACCOUNT, mockBonusAccountModel);
    }

    private void prepareValidPromotion() {
        promotionModel = new OrderAddBonusesPromotionModel();
        promotionModel.setCode(VALID_PROMOTION_CODE);
        promotionResult = new PromotionResultModel();
        promotionResult.setPromotion(promotionModel);
        promotionResultSet = org.fest.util.Collections.set(promotionResult);
    }

    private void prepareInvalidPromotion() {
        promotionModel = new ProductPercentageDiscountPromotionModel();
        promotionModel.setCode(INVALID_PROMOTION_CODE);
        promotionResult = new PromotionResultModel();
        promotionResult.setPromotion(promotionModel);
        promotionResultSet = org.fest.util.Collections.set(promotionResult);
    }
}