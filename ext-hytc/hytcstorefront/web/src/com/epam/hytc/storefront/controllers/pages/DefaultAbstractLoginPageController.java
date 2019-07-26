package com.epam.hytc.storefront.controllers.pages;

import com.epam.hytc.storefront.forms.HytcRegisterForm;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.Breadcrumb;
import de.hybris.platform.acceleratorstorefrontcommons.consent.data.ConsentCookieData;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractLoginPageController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.ConsentForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.GuestForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.LoginForm;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.user.data.RegisterData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

abstract class DefaultAbstractLoginPageController extends AbstractLoginPageController {

    private static final Logger LOGGER = Logger.getLogger(DefaultAbstractLoginPageController.class);
    private static final String FORM_GLOBAL_ERROR = "form.global.error";
    private static final String CONSENT_FORM_GLOBAL_ERROR = "consent.form.global.error";

    String processRegisterUserRequest(final String referer, final HytcRegisterForm form, final BindingResult bindingResult,
                                      final Model model, final HttpServletRequest request, final HttpServletResponse response,
                                      final RedirectAttributes redirectModel) throws CMSItemNotFoundException {
        if (bindingResult.hasErrors()) {
            form.setTermsCheck(false);
            model.addAttribute(form);
            model.addAttribute(new LoginForm());
            model.addAttribute(new GuestForm());
            GlobalMessages.addErrorMessage(model, FORM_GLOBAL_ERROR);
            return handleRegistrationError(model);
        }

        final RegisterData data = new RegisterData();
        data.setFirstName(form.getFirstName());
        data.setLastName(form.getLastName());
        data.setLogin(form.getEmail());
        data.setPassword(form.getPwd());
        data.setTitleCode(form.getTitleCode());
        data.setReferralCode(form.getReferralCode());

        try {
            getCustomerFacade().register(data);
            getAutoLoginStrategy().login(form.getEmail().toLowerCase(), form.getPwd(), request, response);
            GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.CONF_MESSAGES_HOLDER,
                    "registration.confirmation.message.title");

        } catch (final DuplicateUidException e) {
            LOGGER.warn("registration failed: ", e);
            form.setTermsCheck(false);
            model.addAttribute(form);
            model.addAttribute(new LoginForm());
            model.addAttribute(new GuestForm());
            bindingResult.rejectValue("email", "registration.error.account.exists.title");
            GlobalMessages.addErrorMessage(model, FORM_GLOBAL_ERROR);
            return handleRegistrationError(model);
        }

        // Consent form data
        try {
            final ConsentForm consentForm = form.getConsentForm();
            if (consentForm != null && consentForm.getConsentGiven()) {
                getConsentFacade().giveConsent(consentForm.getConsentTemplateId(), consentForm.getConsentTemplateVersion());
            }
        } catch (final Exception e) {
            LOGGER.error("Error occurred while creating consents during registration", e);
            GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, CONSENT_FORM_GLOBAL_ERROR);
        }

        // save anonymous-consent cookies as ConsentData
        final Cookie cookie = WebUtils.getCookie(request, WebConstants.ANONYMOUS_CONSENT_COOKIE);
        if (cookie != null) {
            try {
                final ObjectMapper mapper = new ObjectMapper();
                final List<ConsentCookieData> consentCookieDataList = Arrays.asList(mapper.readValue(
                        URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8.displayName()), ConsentCookieData[].class));
                consentCookieDataList.stream().filter(consentData -> WebConstants.CONSENT_GIVEN.equals(consentData.getConsentState()))
                        .forEach(consentData -> consentFacade.giveConsent(consentData.getTemplateCode(),
                                Integer.valueOf(consentData.getTemplateVersion())));
            } catch (final UnsupportedEncodingException e) {
                LOGGER.error(String.format("Cookie Data could not be decoded : %s", cookie.getValue()), e);
            } catch (final IOException e) {
                LOGGER.error("Cookie Data could not be mapped into the Object", e);
            } catch (final Exception e) {
                LOGGER.error("Error occurred while creating Anonymous cookie consents", e);
            }
        }

        customerConsentDataStrategy.populateCustomerConsentDataInSession();

        return REDIRECT_PREFIX + getSuccessRedirect(request, response);
    }

    protected String getDefaultLoginPage(final boolean loginError, final HttpSession session, final Model model) throws CMSItemNotFoundException
    {
        final LoginForm loginForm = new LoginForm();
        model.addAttribute(loginForm);
        model.addAttribute(new HytcRegisterForm());
        model.addAttribute(new GuestForm());

        final String username = (String) session.getAttribute(SPRING_SECURITY_LAST_USERNAME);
        if (username != null)
        {
            session.removeAttribute(SPRING_SECURITY_LAST_USERNAME);
        }

        loginForm.setJ_username(username);
        storeCmsPageInModel(model, getCmsPage());
        setUpMetaDataForContentPage(model, (ContentPageModel) getCmsPage());
        model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.INDEX_NOFOLLOW);

        addRegistrationConsentDataToModel(model);

        final Breadcrumb loginBreadcrumbEntry = new Breadcrumb("#",
                getMessageSource().getMessage("header.link.login", null, "header.link.login", getI18nService().getCurrentLocale()),
                null);
        model.addAttribute("breadcrumbs", Collections.singletonList(loginBreadcrumbEntry));

        if (loginError)
        {
            model.addAttribute("loginError", Boolean.valueOf(loginError));
            GlobalMessages.addErrorMessage(model, "login.error.account.not.found.title");
        }

        return getView();
    }
}
