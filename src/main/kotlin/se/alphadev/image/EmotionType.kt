package se.alphadev.image

import java.util.*

enum class EmotionType(en: String, sv: String){
    anger("ANGER","ILSKA"),
    contempt("CONTEMPT","FÖRAKT"),
    disgust("DISGUST","AVSKY"),
    fear("FEAR","RÄDSLA"),
    happiness("HAPPINESS","GLÄDJE"),
    neutral("NEUTRAL","NEUTRAL"),
    sadness("SADNESS","SORG"),
    surprise("SURPRISE","FÖRVÅNING"),
    ;

    val sv = sv
    val en = en

    fun getLocalized(locale: Locale): String {
        if(locale.language.equals("sv")) {
            return this.sv
        }
        return this.en
    }
}