package com.tsutsen.platformplayer.activities

/** The settings behind the "at home" (Wi-Fi) and "on the go" (mobile data) presets. */
object NetworkPreset {
    fun forWifi(wifi: Boolean): List<Pair<String, Any>> =
        if (wifi) {
            listOf(
                "dualScreenSleepWhilePlaying" to false,
                "dualScreenLook" to "same",
                "batterySaver" to true,
            )
        } else {
            listOf(
                "dualScreenSleepWhilePlaying" to true,
                "dualScreenSleepSeconds" to 10,
                "dualScreenSleepLevel" to 0,
                "dualScreenLook" to "black",
                "batterySaver" to true,
            )
        }
}
