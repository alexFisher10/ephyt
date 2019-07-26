package com.epam.hytc.core.services.impl;

import com.epam.hytc.core.services.HytcCustomerService;
import com.epam.hytc.core.services.ReturningCustomerTokenService;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.log4j.Logger;
import org.springframework.security.crypto.codec.Hex;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;
import static java.lang.System.currentTimeMillis;
import static org.apache.commons.codec.digest.MessageDigestAlgorithms.MD5;

public class DefaultHytcCustomerService implements HytcCustomerService {

    private static final Logger LOGGER = Logger.getLogger(DefaultHytcCustomerService.class);

    @Resource
    private ModelService modelService;

    @Override
    public void updateTotalSpentForCustomerAfterPlacingOrder(CustomerModel customerModel, double totalPrice) {
        BigDecimal updatedTotalSpent = defineTotalSpentByCustomer(customerModel, totalPrice);
        customerModel.setTotalSpent(updatedTotalSpent);
        modelService.save(customerModel);
    }

    private BigDecimal defineTotalSpentByCustomer(CustomerModel customerModel, double totalPrice) {
        return (customerModel.getTotalSpent()).add(BigDecimal.valueOf(totalPrice));
    }

    @Override
    public void generateReferralCode(CustomerModel customer) {
        validateParameterNotNullStandardMessage(CustomerModel._TYPECODE, customer);
        try {
            String code = customer.getUid() + currentTimeMillis();
            final MessageDigest md5 = MessageDigest.getInstance(MD5);
            md5.update(code.getBytes());
            customer.setReferralCode(String.valueOf(Hex.encode(md5.digest())));
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
