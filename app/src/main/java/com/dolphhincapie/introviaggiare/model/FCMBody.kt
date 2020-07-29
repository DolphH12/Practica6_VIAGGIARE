package com.dolphhincapie.introviaggiare.model

class FCMBody(
    var to: String = "",
    var priority: String = "",
    var data: MutableMap<String, String> = HashMap()
)