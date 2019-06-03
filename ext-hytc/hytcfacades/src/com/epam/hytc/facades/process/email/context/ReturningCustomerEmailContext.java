package com.epam.hytc.facades.process.email.context;

import com.epam.hytc.core.services.ReturningCustomerTokenService;
import com.epam.hytc.core.services.impl.DefaultHytcUserService;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.model.process.StoreFrontCustomerProcessModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import javax.annotation.Resource;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

public class ReturningCustomerEmailContext extends AbstractEmailContext<StoreFrontCustomerProcessModel> {

    @Resource
    private DefaultHytcUserService defaultHytcUserService;
    @Resource
    private ConfigurationService configurationService;
    @Resource
    private ReturningCustomerTokenService returningCustomerTokenService;

    private static final String CUSTOMER_SERVICE_TEAM = "Customer Services Team";
    private static final String CUSTOMER_SERVICE_EMAIL_ADDRESS = "customerservices@hybris.com";
    private final String WEBSITE_ELECTRONICS_HTTP = "website.electronics.http";
    private static final String RETURNING_CUSTOMER_TOKEN_PARAMETER = "returnUserToken";
    private static final String BASE_URL = "The first parameter 'baseUrl' of generateUrlWithTokenForReturningCustomer method was null";
    private static final String URL_PARAMETER = "The second parameter 'parameter' of generateUrlWithTokenForReturningCustomer method was null";
    private static final String PARAMETER_VALUE = "The third parameter 'parameterValue' of generateUrlWithTokenForReturningCustomer method was null";

    private String loginUrlWithToken;

    @Override
    public void init(StoreFrontCustomerProcessModel businessProcessModel, EmailPageModel emailPageModel) {
        put(DISPLAY_NAME, businessProcessModel.getCustomer().getDisplayName());
        put(EMAIL, businessProcessModel.getCustomer().getContactEmail());
        emailPageModel.setFromName(CUSTOMER_SERVICE_TEAM);
        emailPageModel.setFromEmail(CUSTOMER_SERVICE_EMAIL_ADDRESS);
        String loginUrl = configurationService.getConfiguration().getString(WEBSITE_ELECTRONICS_HTTP).concat("/electronics/en/login");
        loginUrlWithToken = generateUrlWithTokenForReturningCustomer(loginUrl, RETURNING_CUSTOMER_TOKEN_PARAMETER, returningCustomerTokenService.generate(businessProcessModel.getCustomer()).getCode());
        super.init(businessProcessModel, emailPageModel);
    }

    public String generateUrlWithTokenForReturningCustomer(String baseUrl, String parameterName, String token) {
        validateParameterNotNull(baseUrl, BASE_URL);
        validateParameterNotNull(parameterName, URL_PARAMETER);
        validateParameterNotNull(token, PARAMETER_VALUE);
        return baseUrl + "?" + parameterName + "=" + token;
    }

    @Override
    protected BaseSiteModel getSite(StoreFrontCustomerProcessModel businessProcessModel) {
        return businessProcessModel.getSite();
    }

    @Override
    protected CustomerModel getCustomer(StoreFrontCustomerProcessModel businessProcessModel) {
        return (CustomerModel) businessProcessModel.getUser();
    }

    @Override
    protected LanguageModel getEmailLanguage(StoreFrontCustomerProcessModel businessProcessModel) {
        return businessProcessModel.getLanguage();
    }

    public String getLoginUrlWithToken() {
        return loginUrlWithToken;
    }
}
