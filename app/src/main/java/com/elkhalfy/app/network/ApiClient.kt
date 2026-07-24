package com.elkhalfy.app.network

import com.elkhalfy.app.data.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

object ApiClient {
    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private const val FOOTBALL_API_KEY = "d3a1f339f2f702915bf4329c2a8b176c"
    private const val FOOTBALL_BASE = "https://v3.football.api-sports.io"

    suspend fun getLiveStreams(srv: Server, categoryId: String): List<Channel> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val url = "${srv.url}/player_api.php?username=${enc(srv.username)}&password=${enc(srv.password)}&action=get_live_streams&category_id=$categoryId"
        val json = get(url) ?: return@withContext emptyList()
        try { gson.fromJson<List<Channel>>(json, object : TypeToken<List<Channel>>() {}.type) ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    suspend fun getVodCategories(srv: Server): List<Category> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val url = "${srv.url}/player_api.php?username=${enc(srv.username)}&password=${enc(srv.password)}&action=get_vod_categories"
        val json = get(url) ?: return@withContext emptyList()
        try { gson.fromJson<List<Category>>(json, object : TypeToken<List<Category>>() {}.type) ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    suspend fun getVodStreams(srv: Server): List<Movie> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val url = "${srv.url}/player_api.php?username=${enc(srv.username)}&password=${enc(srv.password)}&action=get_vod_streams"
        val json = get(url) ?: return@withContext emptyList()
        try { gson.fromJson<List<Movie>>(json, object : TypeToken<List<Movie>>() {}.type) ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    suspend fun getSeriesCategories(srv: Server): List<Category> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val url = "${srv.url}/player_api.php?username=${enc(srv.username)}&password=${enc(srv.password)}&action=get_series_categories"
        val json = get(url) ?: return@withContext emptyList()
        try { gson.fromJson<List<Category>>(json, object : TypeToken<List<Category>>() {}.type) ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    suspend fun getAllSeries(srv: Server): List<Series> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val url = "${srv.url}/player_api.php?username=${enc(srv.username)}&password=${enc(srv.password)}&action=get_series"
        val json = get(url) ?: return@withContext emptyList()
        try { gson.fromJson<List<Series>>(json, object : TypeToken<List<Series>>() {}.type) ?: emptyList() } catch (e: Exception) { emptyList() }
    }

    suspend fun getSeriesInfo(srv: Server, seriesId: Int): SeriesInfo = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val url = "${srv.url}/player_api.php?username=${enc(srv.username)}&password=${enc(srv.password)}&action=get_series_info&series_id=$seriesId"
        val json = get(url) ?: return@withContext SeriesInfo()
        try { gson.fromJson(json, SeriesInfo::class.java) ?: SeriesInfo() } catch (e: Exception) { SeriesInfo() }
    }

    suspend fun getFixtures(date: String): FixturesResponse = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val json = getFootball("/fixtures?date=$date&timezone=Asia/Riyadh") ?: return@withContext FixturesResponse()
        try { gson.fromJson(json, FixturesResponse::class.java) ?: FixturesResponse() } catch (e: Exception) { FixturesResponse() }
    }

    suspend fun getLiveFixtures(): FixturesResponse = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val json = getFootball("/fixtures?live=all") ?: return@withContext FixturesResponse()
        try { gson.fromJson(json, FixturesResponse::class.java) ?: FixturesResponse() } catch (e: Exception) { FixturesResponse() }
    }

    suspend fun getFixtureDetails(id: Int): FixturesResponse = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val json = getFootball("/fixtures?id=$id") ?: return@withContext FixturesResponse()
        try { gson.fromJson(json, FixturesResponse::class.java) ?: FixturesResponse() } catch (e: Exception) { FixturesResponse() }
    }

    suspend fun getStandings(leagueId: Int, season: Int): StandingsResponse = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val json = getFootball("/standings?league=$leagueId&season=$season") ?: return@withContext StandingsResponse()
        try { gson.fromJson(json, StandingsResponse::class.java) ?: StandingsResponse() } catch (e: Exception) { StandingsResponse() }
    }

    suspend fun getTopScorers(leagueId: Int, season: Int): TopScorersResponse = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        val json = getFootball("/players/topscorers?league=$leagueId&season=$season") ?: return@withContext TopScorersResponse()
        try { gson.fromJson(json, TopScorersResponse::class.java) ?: TopScorersResponse() } catch (e: Exception) { TopScorersResponse() }
    }

    private fun get(url: String): String? = try {
        val req = Request.Builder().url(url).build()
        client.newCall(req).execute().use { it.body?.string() }
    } catch (e: Exception) { null }

    private fun getFootball(path: String): String? = try {
        val req = Request.Builder()
            .url("$FOOTBALL_BASE$path")
            .header("x-apisports-key", FOOTBALL_API_KEY)
            .build()
        client.newCall(req).execute().use { it.body?.string() }
    } catch (e: Exception) { null }

    private fun enc(s: String) = java.net.URLEncoder.encode(s, "UTF-8")
}
