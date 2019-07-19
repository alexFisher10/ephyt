package com.epam.hytc.core.services;

import com.epam.hytc.core.model.BonusAccountModel;
import com.epam.hytc.core.model.BonusHistoryEntryModel;
import com.epam.hytc.core.model.ReturningCustomerTokenModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.servicelayer.data.SearchPageData;

public interface BonusHistoryEntryService {
    SearchPageData<BonusHistoryEntryModel> getPagedBonusHistory(CustomerModel customerModel, SearchPageData searchPageData);

    void createRegistrationBonusHistoryEntry(BonusAccountModel bonusAccountModel);

    void createIncomingBonusHistoryEntry(BonusAccountModel bonusAccountModel, double amount);

    void createOutgoingBonusHistoryEntry(BonusAccountModel bonusAccountModel, double amount);

    void createReturningBonusHistoryEntry(CustomerModel customerModel);

    void calculateBonusAfterPlaceOrder();
}