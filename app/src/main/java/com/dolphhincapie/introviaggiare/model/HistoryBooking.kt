package com.dolphhincapie.introviaggiare.model

class HistoryBooking(
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
    var calificationClient: Double = 0.0,
    var CalificationDriver: Double = 0.0,
    var timestamp: Long = 0
)