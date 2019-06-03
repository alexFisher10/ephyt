package com.epam.hytc.core.services.impl;

import com.epam.hytc.core.services.HytcConfigurationService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.servicelayer.time.TimeService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultHytcUserServiceTest {

    private static final String VALID_EMAIL = "some.email@mail.com";
    private static final String INVALID_EMAIL = "anonymous";
    private static final int EXPECTED_FILTERED_LIST_SIZE = 2;

    private List<CustomerModel> customersFromDB;
    private List<CustomerModel> filteredCustomers;

    @Rule
    public ExpectedException rule = ExpectedException.none();

    @Mock
    private Date date;
    @Mock
    private CustomerModel customerWithValidEmail;
    @Mock
    private CustomerModel customerWithInvalidEmail;
    @Mock
    private SearchResult<CustomerModel> searchResult;
    @Mock
    private HytcConfigurationService hytcConfigurationService;
    @Mock
    private TimeService timeService;
    @Mock
    private FlexibleSearchService flexibleSearchService;

    @InjectMocks
    private DefaultHytcUserService defaultHytcUserService;

    @Before
    public void setUp() {
        customersFromDB = new ArrayList<>();
        filteredCustomers = new ArrayList<>();
        doReturn(VALID_EMAIL).when(customerWithValidEmail).getUid();
        doReturn(INVALID_EMAIL).when(customerWithInvalidEmail).getUid();
        doReturn(date).when(timeService).getCurrentTime();
        doReturn(customersFromDB).when(searchResult).getResult();
        doReturn(searchResult).when(flexibleSearchService).search(any(FlexibleSearchQuery.class));
    }

    @Test
    public void shouldAssertFilteredCustomersWhenNotAllCustomersHaveValidEmail() {
        customersFromDB.add(customerWithValidEmail);
        customersFromDB.add(customerWithValidEmail);
        customersFromDB.add(customerWithInvalidEmail);
        customersFromDB.add(customerWithInvalidEmail);

        filteredCustomers = defaultHytcUserService.findInactiveUsers();

        assertEquals(EXPECTED_FILTERED_LIST_SIZE, filteredCustomers.size());
        assertEquals(customerWithValidEmail, filteredCustomers.get(0));
    }

    @Test
    public void shouldAssertFilteredCustomersWhenCustomersNotExist() {
        filteredCustomers = defaultHytcUserService.findInactiveUsers();

        assertTrue(filteredCustomers.isEmpty());
    }
}