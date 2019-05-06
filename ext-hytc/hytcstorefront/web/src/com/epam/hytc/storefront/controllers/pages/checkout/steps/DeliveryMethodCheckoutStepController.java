/*
 * [y] hybris Platform
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */
package com.epam.hytc.storefront.controllers.pages.checkout.steps;

import com.epam.hytc.facades.cart.HytcCheckoutFacade;
import com.epam.hytc.storefront.controllers.ControllerConstants;
import com.epam.hytc.storefront.data.beans.DeliveryDateAndMethodForm;
import com.epam.hytc.storefront.forms.validation.DeliveryDateAndMethodValidator;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateQuoteCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.CartData;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/checkout/multi/delivery-method")
public class DeliveryMethodCheckoutStepController extends AbstractCheckoutStepController {

    private static final String DELIVERY_METHOD = "delivery-method";
    private static final String DELIVERY_DATE_AND_METHOD_FORM_NAME = "deliveryDateAndMethodForm";
    private static final String KEY_MESSAGE_DELIVERY_DATE_AND_METHOD_FORM_ERROR
            = "deliveryDateAndMethod.error.formentry.invalid";
    private static final String KEY_MESSAGE_BREADCRUMB = "checkout.multi.deliveryMethod.breadcrumb";
    private static final String MODEL_ATTR_CART_DATA = "cartData";
    private static final String MODEL_ATTR_DELIVERY_METHODS = "deliveryMethods";
    private static final String MODEL_ATTR_META_ROBOTS = "metaRobots";
    private static final String MODEL_ATTR_META_ROBOTS_VALUE = "noindex,nofollow";
    private static final String REQUEST_MAPPING_ADD = "/add";
    private static final String REQUEST_MAPPING_CHOOSE = "/choose";

    @Resource
    private DeliveryDateAndMethodValidator deliveryDateAndMethodValidator;

    @Resource
    private HytcCheckoutFacade hytcCheckoutFacade;

    @RequestMapping(value = REQUEST_MAPPING_CHOOSE, method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    @PreValidateQuoteCheckoutStep
    @PreValidateCheckoutStep(checkoutStep = DELIVERY_METHOD)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes)
            throws CMSItemNotFoundException {
        getCheckoutFacade().setDeliveryModeIfAvailable();
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        populateCommonModelAttributes(model, cartData, new DeliveryDateAndMethodForm());
        return ControllerConstants.Views.Pages.MultiStepCheckout.ChooseDeliveryMethodPage;
    }

    @RequestMapping(value = REQUEST_MAPPING_ADD, method = RequestMethod.POST)
    @RequireHardLogIn
    public String addDeliveryDateAndMethod(@ModelAttribute(DELIVERY_DATE_AND_METHOD_FORM_NAME) final
                                           DeliveryDateAndMethodForm deliveryDateAndMethodForm,
                                           final BindingResult bindingResult, final Model model)
            throws CMSItemNotFoundException {
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        deliveryDateAndMethodValidator.validate(deliveryDateAndMethodForm, bindingResult);
        populateCommonModelAttributes(model, cartData, deliveryDateAndMethodForm);
        if (bindingResult.hasErrors()) {
            GlobalMessages.addErrorMessage(model, KEY_MESSAGE_DELIVERY_DATE_AND_METHOD_FORM_ERROR);
            return ControllerConstants.Views.Pages.MultiStepCheckout.ChooseDeliveryMethodPage;
        }
        final String deliveryMethod = deliveryDateAndMethodForm.getDeliveryMethod();
        getCheckoutFacade().setDeliveryMode(deliveryMethod);

        final String deliveryDate = deliveryDateAndMethodForm.getDeliveryDate();
        hytcCheckoutFacade.setDeliveryDate(deliveryDate);

        return getCheckoutStep().nextStep();
    }

    @RequestMapping(value = "/back", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String back(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().previousStep();
    }

    @RequestMapping(value = "/next", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String next(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().nextStep();
    }

    protected CheckoutStep getCheckoutStep() {
        return getCheckoutStep(DELIVERY_METHOD);
    }

    protected void populateCommonModelAttributes(final Model model, final CartData cartData,
                                                 final DeliveryDateAndMethodForm deliveryDateAndMethodForm)
            throws CMSItemNotFoundException {
        model.addAttribute(MODEL_ATTR_CART_DATA, cartData);
        model.addAttribute(DELIVERY_DATE_AND_METHOD_FORM_NAME, deliveryDateAndMethodForm);
        model.addAttribute(MODEL_ATTR_DELIVERY_METHODS, getCheckoutFacade().getSupportedDeliveryModes());
        this.prepareDataForPage(model);
        storeCmsPageInModel(model, getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL));
        setUpMetaDataForContentPage(model, getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL));
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs(KEY_MESSAGE_BREADCRUMB));
        model.addAttribute(MODEL_ATTR_META_ROBOTS, MODEL_ATTR_META_ROBOTS_VALUE);
        setCheckoutStepLinksForModel(model, getCheckoutStep());
    }
}
