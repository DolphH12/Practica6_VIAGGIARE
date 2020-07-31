package com.dolphhincapie.introviaggiare.model

class ClientBooking(
    var idHistoryBooking: String = "",
    var idClient: String = "",
    var idDriver: String = "",
    var destination: String = "",
    var origin: String = "",
    var time: String = "",
    var km: String = "",
    var status: String = "",
    var originLat: Double = 0.0,
    var originLng: Double = 0.0,
    var destinationLat: Double = 0.0,
    var destinationLng: Double = 0.0,
    var precio: Double = 0.0
)
