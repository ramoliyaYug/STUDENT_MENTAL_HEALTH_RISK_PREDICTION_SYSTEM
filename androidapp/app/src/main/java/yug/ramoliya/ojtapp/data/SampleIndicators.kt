package yug.ramoliya.ojtapp.data

import com.google.gson.JsonObject

/**
 * Default questionnaire payload matching backend [apitesting.md] / cleaned indicator keys.
 * Students can edit JSON in the app or replace programmatically.
 */
object SampleIndicators {
    fun asJsonObject(): JsonObject {
        val o = JsonObject()
        o.addProperty("Age", "18-22")
        o.addProperty("Gender", "Female")
        o.addProperty("University", "Independent University, Bangladesh (IUB)")
        o.addProperty("Department", "Engineering - CS / CSE / CSC / Similar to CS")
        o.addProperty("Academic_Year", "Fourth Year or Equivalent")
        o.addProperty("CGPA", "2.50 - 2.99")
        o.addProperty("Scholarship", "No")
        o.addProperty("A1_Nervous", 1)
        o.addProperty("A2_Worrying", 1)
        o.addProperty("A3_Relaxing", 1)
        o.addProperty("A4_Irritated", 2)
        o.addProperty("A5_TooMuchWorry", 2)
        o.addProperty("A6_Restless", 2)
        o.addProperty("A7_Afraid", 1)
        o.addProperty("S1_Upset", 2)
        o.addProperty("S2_Uncontrolled", 2)
        o.addProperty("S3_NervousStressed", 3)
        o.addProperty("S4_CannotCope", 2)
        o.addProperty("S5_Confident", 2)
        o.addProperty("S6_ThingsGoingWell", 2)
        o.addProperty("S7_ControlIrritations", 2)
        o.addProperty("S8_PerformanceOnTop", 2)
        o.addProperty("S9_Angered", 2)
        o.addProperty("S10_PilingUp", 2)
        o.addProperty("D1_LittleInterest", 1)
        o.addProperty("D2_Hopeless", 2)
        o.addProperty("D3_SleepTrouble", 1)
        o.addProperty("D4_Tired", 1)
        o.addProperty("D5_Appetite", 2)
        o.addProperty("D6_Failure", 1)
        o.addProperty("D7_Concentration", 1)
        o.addProperty("D8_Psychomotor", 1)
        o.addProperty("D9_SuicidalThoughts", 1)
        return o
    }

    fun asPrettyJson(): String = com.google.gson.GsonBuilder().setPrettyPrinting().create()
        .toJson(asJsonObject())
}
