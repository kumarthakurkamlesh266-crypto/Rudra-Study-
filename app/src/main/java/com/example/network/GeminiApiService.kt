package com.example.network

import android.graphics.Bitmap
import android.util.Base64
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>,
    val role: String? = null
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null,
    val inline_data: InlineData? = null
)

@JsonClass(generateAdapter = true)
data class InlineData(
    val mime_type: String,
    val data: String
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val error: GeminiError? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null,
    val finishReason: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

interface GeminiApi {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"
    const val MODEL_FLASH = "gemini-3.5-flash"
    const val MODEL_PRO = "gemini-3.1-pro-preview"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    suspend fun askAi(
        apiKey: String,
        prompt: String,
        systemInstruction: String = "You are Rudra AI, a personal study operating system coach and subject master for Class 12 Science (BSEB/CBSE). You provide clear, concise, structured, and action-oriented answers with zero fluff.",
        bitmap: Bitmap? = null,
        model: String = MODEL_FLASH
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Gemini API Key is not set. Please add it in Settings."))
        }

        val parts = mutableListOf<Part>()
        parts.add(Part(text = prompt))

        if (bitmap != null) {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
            parts.add(Part(inline_data = InlineData(mime_type = "image/jpeg", data = base64)))
        }

        val request = GeminiRequest(
            contents = listOf(Content(parts = parts, role = "user")),
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        try {
            val response = api.generateContent(model = model, apiKey = apiKey, request = request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else if (response.error != null) {
                Result.failure(Exception("Gemini API Error: ${response.error.message}"))
            } else {
                Result.failure(Exception("Empty response from AI"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to communicate with Gemini API"))
        }
    }

    suspend fun chatMultiTurn(
        apiKey: String,
        messages: List<Content>,
        systemInstruction: String,
        model: String = MODEL_FLASH
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(Exception("Gemini API Key is not configured. Please add your key in Settings."))
        }

        val request = GeminiRequest(
            contents = messages,
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        try {
            val response = api.generateContent(model = model, apiKey = apiKey, request = request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success(text)
            } else if (response.error != null) {
                Result.failure(Exception("Gemini API Error: ${response.error.message}"))
            } else {
                Result.failure(Exception("Empty response from AI Tutor"))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "Failed to reach Gemini API. Please check your network and API key."))
        }
    }
}
