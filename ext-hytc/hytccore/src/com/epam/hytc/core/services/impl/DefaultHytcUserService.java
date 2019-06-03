package com.epam.hytc.core.services.impl;

import com.epam.hytc.core.services.HytcConfigurationService;
import com.epam.hytc.core.services.HytcUsersService;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.time.TimeService;
import de.hybris.platform.servicelayer.user.impl.DefaultUserService;
import org.apache.commons.validator.routines.EmailValidator;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultHytcUserService extends DefaultUserService implements HytcUsersService {

    private static final String DATE = "date";
    private static final String INACTIVE_DAYS_THRESHOLD = "customer.inactive.days.threshold";
    private static final String FIND_INACTIVE_USERS_QUERY = "SELECT {c.PK} FROM {Customer as c} " +
            "WHERE ?" + DATE + ">= {c.lastLogin} + INTERVAL ?" + INACTIVE_DAYS_THRESHOLD + " DAY";

    @Resource
    private HytcConfigurationService hytcConfigurationService;
    @Resource
    private TimeService timeService;
    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Override
    public List<CustomerModel> findInactiveUsers() {
        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(FIND_INACTIVE_USERS_QUERY);
        flexibleSearchQuery.addQueryParameter(INACTIVE_DAYS_THRESHOLD, hytcConfigurationService.getInt(INACTIVE_DAYS_THRESHOLD));
        flexibleSearchQuery.addQueryParameter(DATE, timeService.getCurrentTime());
        return flexibleSearchService.<CustomerModel>search(flexibleSearchQuery).getResult()
                .stream()
                .filter(u -> isEmail(u.getUid())).collect(Collectors.toList());
    }

    private Boolean isEmail(String email) {
        return EmailValidator.getInstance().isValid(email);
    }
}
