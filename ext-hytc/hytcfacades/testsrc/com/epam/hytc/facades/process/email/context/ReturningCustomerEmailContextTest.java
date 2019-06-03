package com.epam.hytc.facades.process.email.context;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertTrue;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class ReturningCustomerEmailContextTest {

    private static final String BASE_URL = "The first parameter 'baseUrl' of generateUrlWithTokenForReturningCustomer method was null";
    private static final String URL_PARAMETER = "The second parameter 'parameter' of generateUrlWithTokenForReturningCustomer method was null";
    private static final String PARAMETER_VALUE = "The third parameter 'parameterValue' of generateUrlWithTokenForReturningCustomer method was null";

    private String generatedUrl;

    @Rule
    public ExpectedException rule = ExpectedException.none();

    @InjectMocks
    private ReturningCustomerEmailContext returningCustomerEmailContext;

    @Test
    public void shouldAssertGeneratedUrlWhenAllMethodParametersAreValid() {
        generatedUrl = returningCustomerEmailContext.generateUrlWithTokenForReturningCustomer(BASE_URL, URL_PARAMETER, PARAMETER_VALUE);

        assertTrue(generatedUrl.contains(BASE_URL));
        assertTrue(generatedUrl.contains(URL_PARAMETER));
        assertTrue(generatedUrl.contains(PARAMETER_VALUE));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenFirstArgumentInvalid() {
        rule.expect(IllegalArgumentException.class);
        rule.expectMessage(BASE_URL);

        returningCustomerEmailContext.generateUrlWithTokenForReturningCustomer(null, URL_PARAMETER, PARAMETER_VALUE);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenSecondArgumentInvalid() {
        rule.expect(IllegalArgumentException.class);
        rule.expectMessage(URL_PARAMETER);

        returningCustomerEmailContext.generateUrlWithTokenForReturningCustomer(BASE_URL, null, PARAMETER_VALUE);
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenThirdArgumentInvalid() {
        rule.expect(IllegalArgumentException.class);
        rule.expectMessage(PARAMETER_VALUE);

        returningCustomerEmailContext.generateUrlWithTokenForReturningCustomer(BASE_URL, URL_PARAMETER, null);
    }
}