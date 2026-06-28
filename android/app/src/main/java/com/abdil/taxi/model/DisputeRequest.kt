package com.abdil.taxi.model

data class DisputeRequest(
    val rideId: Long,
    val clientId: Long,
    val reason: String,
    val description: String
)