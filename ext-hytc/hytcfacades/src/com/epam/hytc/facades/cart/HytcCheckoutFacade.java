package com.epam.hytc.facades.cart;

import de.hybris.platform.commercefacades.order.CheckoutFacade;

public interface HytcCheckoutFacade extends CheckoutFacade {

    void setDeliveryDate(String deliveryDate);
}
