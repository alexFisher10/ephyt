package com.epam.hytc.facades.cart.impl;

import com.epam.hytc.facades.cart.HytcCheckoutFacade;
import de.hybris.platform.commercefacades.order.impl.DefaultCheckoutFacade;
import de.hybris.platform.core.model.order.CartModel;
import org.apache.log4j.Logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.epam.hytc.facades.constants.HytcFacadesConstants.DATEPATTERN;

public class DefaultHytcCheckoutFacade extends DefaultCheckoutFacade implements HytcCheckoutFacade {

    private static final Logger LOG = Logger.getLogger(DefaultHytcCheckoutFacade.class);

    @Override
    public void setDeliveryDate(String date) {
        Date deliveryDate;
        try {
            deliveryDate = new SimpleDateFormat(DATEPATTERN).parse(date);
        } catch (ParseException e) {
            LOG.error("Order delivery date is invalid for order: " + e);
            throw new IllegalArgumentException(e);
        }
        CartModel cartModel = getCartService().getSessionCart();
        cartModel.setDeliveryDate(deliveryDate);

        getModelService().save(cartModel);
    }
}