package com.dolphhincapie.introviaggiare.model

class FCMResponse(
    val multicast_id: Long = 0,
    val success: Int = 0,
    val failure: Int = 0,
    val canonical_ids: Int = 0,
    val results: ArrayList<Any> = arrayListOf()

)