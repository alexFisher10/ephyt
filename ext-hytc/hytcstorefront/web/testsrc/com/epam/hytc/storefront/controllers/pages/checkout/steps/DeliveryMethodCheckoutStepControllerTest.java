package com.epam.hytc.storefront.controllers.pages.checkout.steps;

import com.epam.hytc.facades.cart.HytcCheckoutFacade;
import com.epam.hytc.storefront.controllers.ControllerConstants;
import com.epam.hytc.storefront.data.beans.DeliveryDateAndMethodForm;
import com.epam.hytc.storefront.forms.validation.DeliveryDateAndMethodValidator;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.acceleratorservices.config.SiteConfigService;
import de.hybris.platform.acceleratorservices.storefront.util.PageTitleResolver;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.Breadcrumb;
import de.hybris.platform.acceleratorstorefrontcommons.breadcrumb.ResourceBreadcrumbBuilder;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutGroup;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.AbstractPageModel;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.cms2.model.pages.PageTemplateModel;
import de.hybris.platform.cms2.servicelayer.services.CMSPageService;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.BindingResult;
import org.springframework.validation.support.BindingAwareModelMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DeliveryMethodCheckoutStepControllerTest {

    private static final String DELIVERY_DATE_AND_METHOD_FORM_NAME = "deliveryDateAndMethodForm";
    private static final String MODEL_ATTR_CART_DATA = "cartData";
    private static final String MODEL_ATTR_DELIVERY_METHODS = "deliveryMethods";
    private static final String MODEL_ATTR_TAX_ESTIMATION_ENABLED = "taxEstimationEnabled";
    private static final String MODEL_ATTR_IS_OMS_ENABLED = "isOmsEnabled";
    private static final String MODEL_ATTR_SUPPORTED_COUNTRIES = "supportedCountries";
    private static final String MODEL_ATTR_EXPRESS_CHECKOUT_ALLOWED = "expressCheckoutAllowed";
    private static final String MODEL_ATTR_META_ROBOTS = "metaRobots";
    private static final String MODEL_ATTR_META_ROBOTS_VALUE = "noindex,nofollow";
    private static final String TITLE_FOR_PAGE = "Delivery Date And Method Test Title";
    private static final String CMS_PAGE_MODEL = "cmsPage";
    private static final String VIEW_FOR_PAGE = "deliveryDateAndMethodTest.jsp";
    private static final String CHECKOUT_FLOW_GROUP = "checkoutFlowGroup";
    private static final String OMS_ENABLED = "oms.enabled";
    private static final String DELIVERY_DATE = "01/01/2019";

    private List supportedDeliveryModes;
    private List<CountryData> supportedCountries;
    private List<Breadcrumb> breadcrumbs;

    @Mock
    private DeliveryDateAndMethodValidator validator;
    @Mock
    private CartData cartData;
    @Mock
    private HytcCheckoutFacade hytcCheckoutFacade;
    @Mock
    private AcceleratorCheckoutFacade checkoutFacade;
    @Mock
    private DeliveryDateAndMethodForm form;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private CartFacade cartFacade;
    @Mock
    private ResourceBreadcrumbBuilder resourceBreadcrumbBuilder;
    @Mock
    private CMSPageService cmsPageService;
    @Mock
    private ContentPageModel contentPageModel;
    @Mock
    private PageTitleResolver pageTitleResolver;
    @Mock
    private AbstractPageModel abstractPageModel;
    @Mock
    private PageTemplateModel pageTemplateModel;
    @Mock
    private Breadcrumb breadcrumb;
    @Mock
    private CountryData countryData;
    @Mock
    private CheckoutStep checkoutStep;
    @Mock
    private HashMap<String, CheckoutStep> checkoutStepMap;
    @Spy
    private CheckoutGroup checkoutGroup;
    @Mock
    private SiteConfigService siteConfigService;
    @Mock
    private Map<String, CheckoutGroup> checkoutFlowGroupMap;
    @Spy
    private BindingAwareModelMap model;
    @InjectMocks
    private DeliveryMethodCheckoutStepController controller;

    @Before
    public void setUp() throws CMSItemNotFoundException {
        when(checkoutFacade.getCheckoutCart()).thenReturn(cartData);
        doNothing().when(validator).validate(form, bindingResult);
        doReturn(DELIVERY_DATE).when(form).getDeliveryDate();
        breadcrumbs = Collections.singletonList(breadcrumb);
        when(resourceBreadcrumbBuilder.getBreadcrumbs(anyString())).thenReturn(breadcrumbs);
        when(cmsPageService.getPageForLabelOrId(anyString())).thenReturn(contentPageModel);
        when(pageTitleResolver.resolveContentPageTitle(anyString())).thenReturn(TITLE_FOR_PAGE);
        when(model.containsAttribute(CMS_PAGE_MODEL)).thenReturn(Boolean.TRUE);
        when(model.asMap().get(CMS_PAGE_MODEL)).thenReturn(abstractPageModel);
        when(abstractPageModel.getMasterTemplate()).thenReturn(pageTemplateModel);
        when(cmsPageService.getFrontendTemplateName(pageTemplateModel)).thenReturn(VIEW_FOR_PAGE);
        supportedDeliveryModes = new ArrayList();
        when(checkoutFacade.getSupportedDeliveryModes()).thenReturn(supportedDeliveryModes);
        when(siteConfigService.getBoolean(OMS_ENABLED, false)).thenReturn(any(Boolean.class));
        supportedCountries = Collections.singletonList(countryData);
        when(cartFacade.getDeliveryCountries()).thenReturn(supportedCountries);
        when(checkoutFacade.isExpressCheckoutAllowedForCart()).thenReturn(any(Boolean.class));
        when(checkoutFacade.isTaxEstimationEnabledForCart()).thenReturn(any(Boolean.class));
        setUpCheckoutGroup();
    }

    private void setUpCheckoutGroup() {
        when(checkoutFacade.getCheckoutFlowGroupForCheckout()).thenReturn(CHECKOUT_FLOW_GROUP);
        checkoutGroup = new CheckoutGroup();
        when(checkoutFlowGroupMap.get(CHECKOUT_FLOW_GROUP)).thenReturn(checkoutGroup);
        checkoutGroup.setCheckoutStepMap(checkoutStepMap);
        when(checkoutStepMap.get(anyString())).thenReturn(checkoutStep);
    }

    @Test
    public void shouldGetCheckoutCart_whenAddDeliveryDateAndMethod() throws CMSItemNotFoundException {
        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(checkoutFacade).getCheckoutCart();
    }

    @Test
    public void shouldValidate_whenAddDeliveryDateAndMethod() throws CMSItemNotFoundException {
        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(validator).validate(form, bindingResult);
    }

    @Test
    public void shouldAddCartDataAttribute_whenAddDeliveryDateAndMethod() throws CMSItemNotFoundException {
        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(model).addAttribute(MODEL_ATTR_CART_DATA, cartData);
    }

    @Test
    public void shouldAddDeliveryDateAndMethodFormAttribute_whenAddDeliveryDateAndMethod()
            throws CMSItemNotFoundException {
        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(model).addAttribute(DELIVERY_DATE_AND_METHOD_FORM_NAME, form);
    }

    @Test
    public void shouldAddSupportedDeliveryModesAttribute_whenAddDeliveryDateAndMethod()
            throws CMSItemNotFoundException {
        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(model).addAttribute(MODEL_ATTR_DELIVERY_METHODS, supportedDeliveryModes);
    }

    @Test
    public void shouldAddIsOmsEnabledAttribute_whenAddDeliveryDateAndMethod() throws CMSItemNotFoundException {
        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(model).addAttribute(MODEL_ATTR_IS_OMS_ENABLED, any(Boolean.class));
    }

    @Test
    public void shouldAddSupportedCountriesAttribute_whenAddDeliveryDateAndMethod() throws CMSItemNotFoundException {
        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(model).addAttribute(MODEL_ATTR_SUPPORTED_COUNTRIES, supportedCountries);
    }

    @Test
    public void shouldAddExpressCheckoutAllowedAttribute_whenAddDeliveryDateAndMethod()
            throws CMSItemNotFoundException {
        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(model).addAttribute(MODEL_ATTR_EXPRESS_CHECKOUT_ALLOWED, any(Boolean.class));
    }

    @Test
    public void shouldAddTaxEstimationEnabledAttribute_whenAddDeliveryDateAndMethod() throws CMSItemNotFoundException {
        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(model).addAttribute(MODEL_ATTR_TAX_ESTIMATION_ENABLED, any(Boolean.class));
    }

    @Test
    public void shouldAddBreadcrumbsAttribute_whenAddDeliveryDateAndMethod() throws CMSItemNotFoundException {
        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(model).addAttribute(WebConstants.BREADCRUMBS_KEY, breadcrumbs);
    }

    @Test
    public void shouldAddMetaRobotsAttribute_whenAddDeliveryDateAndMethod() throws CMSItemNotFoundException {
        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(model).addAttribute(MODEL_ATTR_META_ROBOTS, MODEL_ATTR_META_ROBOTS_VALUE);
    }

    @Test
    public void shouldReturnChooseDeliveryMethodPage_whenAddDeliveryDateAndMethodAndBindingResultHasErrors()
            throws CMSItemNotFoundException {
        when(bindingResult.hasErrors()).thenReturn(true);

        final String result = controller.addDeliveryDateAndMethod(form, bindingResult, model);

        Assertions.assertThat(result).isEqualTo(ControllerConstants.Views.Pages.MultiStepCheckout
                .ChooseDeliveryMethodPage);
    }

    @Test
    public void shouldSetDeliveryMode_whenAddDeliveryDateAndMethodAndBindingResultHasNoErrors()
            throws CMSItemNotFoundException {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(checkoutFacade.setDeliveryMode(anyString())).thenReturn(true);

        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(checkoutFacade).setDeliveryMode(anyString());
    }

    @Test
    public void shouldCallNextStep_whenAddDeliveryDateAndMethodAndBindingResultHasNoErrors()
            throws CMSItemNotFoundException {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(checkoutFacade.setDeliveryMode(anyString())).thenReturn(true);

        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(checkoutStep, atLeastOnce()).nextStep();
    }

    @Test
    public void shouldSetDeliveryDate_whenAddDeliveryDateAndMethodAndBindingResultHasNoErrors()
            throws CMSItemNotFoundException {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(checkoutFacade.setDeliveryMode(anyString())).thenReturn(true);

        controller.addDeliveryDateAndMethod(form, bindingResult, model);

        verify(hytcCheckoutFacade).setDeliveryDate(DELIVERY_DATE);
    }
}