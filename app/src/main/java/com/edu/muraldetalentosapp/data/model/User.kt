package com.edu.muraldetalentosapp.data.model

import com.edu.muraldetalentosapp.ui.components.AccountType

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val type: AccountType = AccountType.CANDIDATE,
    val phone: String = "",
    val about: String = ""
)
