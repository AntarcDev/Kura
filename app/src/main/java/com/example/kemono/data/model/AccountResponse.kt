package com.example.kemono.data.model

data class AccountResponse(
    val props: AccountProps
)

data class AccountProps(
    val account: Account
)

data class Account(
    val id: Int,
    val username: String,
    val role: String,
    val created_at: String
)
