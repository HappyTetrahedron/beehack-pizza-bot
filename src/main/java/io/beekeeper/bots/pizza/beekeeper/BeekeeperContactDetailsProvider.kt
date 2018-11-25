package io.beekeeper.bots.pizza.beekeeper

import io.beekeeper.bots.pizza.ordering.ContactDetails
import io.beekeeper.bots.pizza.ordering.ContactDetailsException
import io.beekeeper.bots.pizza.ordering.ContactDetailsProvider
import io.beekeeper.sdk.BeekeeperSDK
import io.beekeeper.sdk.exception.BeekeeperException

class BeekeeperContactDetailsProvider(private val sdk: BeekeeperSDK) : ContactDetailsProvider {

    override fun getContactDetails(username: String): ContactDetails {
        try {
            val profile = sdk.profiles.getProfileByUsername(username).execute()
            return ContactDetails(
                    firstName = profile.firstName?.takeUnless { it.isBlank() } ?: profile.displayName,
                    lastName = profile.lastName ?: "",
                    phoneNumber = profile.getCustomFieldValue("mobile") as? String?
                            ?: throw ContactDetailsException("No phone number set in profile"),
                    emailAddress = profile.getCustomFieldValue("email") as? String?
                            ?: throw ContactDetailsException("No email address set in profile")
            )
        } catch (e: BeekeeperException) {
            throw ContactDetailsException("Failed to retrieve contact details", e)
        }
    }

}