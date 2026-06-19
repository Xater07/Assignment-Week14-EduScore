package com.example.data

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import com.example.BuildConfig

object SupabaseClient {
    private const val TAG = "SupabaseClient"

    val supabaseUrl: String
        get() = try {
            BuildConfig.SUPABASE_URL.trim()
        } catch (e: Exception) {
            ""
        }

    val supabaseAnonKey: String
        get() = try {
            BuildConfig.SUPABASE_ANON_KEY.trim()
        } catch (e: Exception) {
            ""
        }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val mapAdapter = moshi.adapter(Map::class.java)

    /**
     * Checks if URL and Keys are set by the developer in the Secrets console config.
     */
    fun isConfigured(): Boolean {
        val url = supabaseUrl
        val key = supabaseAnonKey
        return url.isNotEmpty() 
                && url.startsWith("http") 
                && key.isNotEmpty() 
                && !url.contains("PLACEHOLDER") 
                && !key.contains("PLACEHOLDER")
    }

    /**
     * Fetch all records from a Supabase PostgREST table synchronously (must run on background thread).
     */
    fun fetchAll(table: String): List<Map<String, Any?>> {
        if (!isConfigured()) return emptyList()

        val url = "$supabaseUrl/rest/v1/$table?select=*"
        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("apikey", supabaseAnonKey)
            .addHeader("Authorization", "Bearer $supabaseAnonKey")
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Fetch from $table failed: Code ${response.code}, Message: ${response.message}")
                    emptyList()
                } else {
                    val bodyString = response.body?.string() ?: ""
                    if (bodyString.isEmpty()) {
                        emptyList()
                    } else {
                        @Suppress("UNCHECKED_CAST")
                        val rawList = moshi.adapter(List::class.java).fromJson(bodyString) as? List<*>
                        rawList?.mapNotNull { item ->
                            @Suppress("UNCHECKED_CAST")
                            item as? Map<String, Any?>
                        } ?: emptyList()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching from $table: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Upsert a record into a Supabase PostgREST table asynchronously.
     */
    fun upsert(table: String, data: Map<String, Any?>) {
        if (!isConfigured()) return

        val url = "$supabaseUrl/rest/v1/$table"
        val jsonPayload = mapAdapter.toJson(data)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonPayload.toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("apikey", supabaseAnonKey)
            .addHeader("Authorization", "Bearer $supabaseAnonKey")
            .addHeader("Content-Type", "application/json")
            .addHeader("Prefer", "resolution=merge-duplicates")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "Upsert to $table failed: ${e.message}", e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Upsert to $table unsuccessful: Code ${response.code}, Payload: $jsonPayload, Message: ${response.message}")
                    } else {
                        Log.d(TAG, "Upserted to $table successfully")
                    }
                }
            }
        })
    }

    /**
     * Delete a record from a Supabase PostgREST table asynchronously.
     */
    fun delete(table: String, primaryKeyCol: String, primaryKeyValue: String) {
        if (!isConfigured()) return

        val url = "$supabaseUrl/rest/v1/$table?$primaryKeyCol=eq.$primaryKeyValue"
        val request = Request.Builder()
            .url(url)
            .delete()
            .addHeader("apikey", supabaseAnonKey)
            .addHeader("Authorization", "Bearer $supabaseAnonKey")
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "Delete from $table failed: ${e.message}", e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    if (!response.isSuccessful) {
                        Log.e(TAG, "Delete from $table unsuccessful: Code ${response.code}, Message: ${response.message}")
                    } else {
                        Log.d(TAG, "Deleted from $table successfully where $primaryKeyCol = $primaryKeyValue")
                    }
                }
            }
        })
    }
}
