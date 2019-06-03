package com.epam.hytc.core.services;

import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.List;

public interface HytcUsersService extends UserService {

    List<CustomerModel> findInactiveUsers();
}
