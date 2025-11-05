package com.ksi.g1controlpanel

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import kotlinx.serialization.Serializable
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Jest to w jednym pliku bo jestem leniwy
@Serializable
data class ExecuteRequest(
    val programName: String,
    val parameters: List<String>
)

@Serializable
data class RobotResponse(
    val status: String,
    val message: String
)

@Serializable
data class RobotStatusResponse(
    val battery_percent: Int,
    val is_charging: Boolean,
    val current_program: String,
    @SerialName("cos tam cos tam")
    val cos_tam_cos_tam: String
)

interface RobotApiService {

    /*
      Przykladowy request:
      {
        "programName": "run_demo_1",
        "parameters": ["paramA", "paramB"]
      }
      Albo cos w stylu:
      {
        "programName": "run_demo_1",
      }

      Przykladowy response:
      {
        "status": "success",
        "message": "Program run_demo_1 started"
      }
    */
    @POST("/robot/execute")
    suspend fun executeProgram(@Body request: ExecuteRequest): RobotResponse

    /*
      Jak chcesz to fajnie by bylo jakby byl taki get z statusu robota zeby ta
      apka wiedziala cokolwiek, albo z takich uzytecznych to zrobic geta ktory wylistuje
      programy ktore moznaby uruchomic na tym terminatorze
    {
        "battery_percent": 87,
        "is_charging": false,
        "current_program": "idle",
        "cos tam cos tam": "nie dziala"
    }
    */

    @GET("/robot/status")
    suspend fun getRobotStatus(): RobotStatusResponse

}

object RetrofitClient {
    private const val BASE_URL = "http://10.102.23.26:8080/"
    private val json = Json { ignoreUnknownKeys = true }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: RobotApiService by lazy {
        retrofit.create(RobotApiService::class.java)
    }
}