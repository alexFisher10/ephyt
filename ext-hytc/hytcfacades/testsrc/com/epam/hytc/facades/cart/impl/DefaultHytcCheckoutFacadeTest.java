package com.epam.hytc.facades.cart.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultHytcCheckoutFacadeTest {

    private static final String VALID_DATE = "01/01/2019";
    private static final String INVALID_DATE = "abcde";

    @Mock
    private CartModel cart;
    @Mock
    private ModelService modelService;
    @Mock
    private CartService cartService;

    @Rule
    public ExpectedException rule = ExpectedException.none();

    @InjectMocks
    private DefaultHytcCheckoutFacade defaultHytcCheckoutFacade;

    @Before
    public void setUp() {
        doReturn(cart).when(cartService).getSessionCart();
    }

    @Test
    public void shouldSetDeliveryDateWhenDateIsValid() {
        defaultHytcCheckoutFacade.setDeliveryDate(VALID_DATE);

        verify(modelService).save(cart);
        verify(cart).setDeliveryDate(any(Date.class));
    }

    @Test
    public void shouldThrowIllegalArgumentExceptionWhenDateIsInvalid() {
        rule.expect(IllegalArgumentException.class);

        defaultHytcCheckoutFacade.setDeliveryDate(INVALID_DATE);

        verify(modelService, never()).save(cart);
        verify(cart, never()).setDeliveryDate(any(Date.class));
    }

}