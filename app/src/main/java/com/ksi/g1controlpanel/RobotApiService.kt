package com.ksi.g1controlpanel

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
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
    @POST("api/robot/execute")
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

    @GET("api/robot/status")
    suspend fun getRobotStatus(): RobotResponse

}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.1.100:8080/"
    private val json = Json { ignoreUnknownKeys = true }
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: RobotApiService by lazy {
        retrofit.create(RobotApiService::class.java)
    }
}