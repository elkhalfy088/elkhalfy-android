package com.elkhalfy.app.data

import android.content.Context
import android.content.SharedPreferences
import com.elkhalfy.app.network.ApiClient
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.tasks.await

object AppRepository {
    private val gson = Gson()
    private lateinit var prefs: SharedPreferences

    var appConfig = AppConfig()
    var servers = listOf<Server>()
    var allowedCats = listOf<String>()
    var catMap = mapOf<String, String>()
    var catList = listOf<Category>()
    var channelLimit = 0

    var allChannels = listOf<Channel>()
    var allMovies = listOf<Movie>()
    var allMovieCategories = listOf<Category>()
    var allSeries = listOf<Series>()
    var allSeriesCategories = listOf<Category>()

    fun init(ctx: Context) {
        prefs = ctx.getSharedPreferences("elkhalfy_cache", Context.MODE_PRIVATE)
    }

    suspend fun loadFirebase() {
        try {
            val db = Firebase.firestore
            val (appDoc, serversColl, chDoc) = Triple(
                db.collection("config").document("app").get().await(),
                db.collection("servers").get().await(),
                db.collection("config").document("channels").get().await()
            )
            if (appDoc.exists()) {
                val name = appDoc.getString("name") ?: "Elkhalfy"
                val maintenance = appDoc.getBoolean("features.maintenance") ?: false
                val maintMsg = appDoc.getString("maintMsg") ?: ""
                @Suppress("UNCHECKED_CAST")
                val socialData = appDoc.get("social") as? List<Map<String, Any>> ?: emptyList()
                val social = socialData.map { SocialLink(it["platform"] as? String ?: "", it["label"] as? String ?: "", it["url"] as? String ?: "") }
                appConfig = AppConfig(name, Features(maintenance), social, maintMsg)
            }
            servers = serversColl.documents.map { d ->
                Server(d.getString("url") ?: "", d.getString("username") ?: "", d.getString("password") ?: "")
            }.filter { it.url.isNotEmpty() }
            if (chDoc.exists()) {
                @Suppress("UNCHECKED_CAST")
                allowedCats = chDoc.get("allowed") as? List<String> ?: emptyList()
                @Suppress("UNCHECKED_CAST")
                val catData = chDoc.get("catData") as? List<Map<String, Any>> ?: emptyList()
                catList = catData.map { Category(it["category_id"] as? String ?: "", it["category_name"] as? String ?: "") }
                catMap = catList.associate { it.categoryId to it.categoryName }
                channelLimit = (chDoc.getLong("limit") ?: 0L).toInt()
            }
        } catch (e: Exception) { /* use defaults */ }
    }

    suspend fun loadChannels(): Boolean {
        if (servers.isEmpty() || allowedCats.isEmpty()) return false
        val srv = servers[0]
        val channels = mutableListOf<Channel>()
        var hiddenIds = listOf<String>()
        try {
            val db = Firebase.firestore
            val hDoc = db.collection("config").document("hidden").get().await()
            if (hDoc.exists()) {
                @Suppress("UNCHECKED_CAST")
                hiddenIds = hDoc.get("ids") as? List<String> ?: emptyList()
            }
        } catch (e: Exception) { }

        for (catId in allowedCats) {
            try {
                val chs = ApiClient.getLiveStreams(srv, catId)
                channels.addAll(chs.filter { !hiddenIds.contains(it.streamId.toString()) }.map { it.copy(catName = catMap[catId] ?: "") })
                if (channelLimit > 0 && channels.size >= channelLimit) break
            } catch (e: Exception) { }
        }
        val result = if (channelLimit > 0) channels.take(channelLimit) else channels
        allChannels = result
        saveToCache("chs", result)
        return result.isNotEmpty()
    }

    suspend fun loadMovies(): Boolean {
        if (servers.isEmpty()) return false
        val srv = servers[0]
        return try {
            val (cats, movies) = Pair(ApiClient.getVodCategories(srv), ApiClient.getVodStreams(srv))
            allMovieCategories = cats
            allMovies = movies
            saveToCache("mov", movies)
            saveToCache("movCats", cats)
            true
        } catch (e: Exception) { false }
    }

    suspend fun loadSeries(): Boolean {
        if (servers.isEmpty()) return false
        val srv = servers[0]
        return try {
            val (cats, series) = Pair(ApiClient.getSeriesCategories(srv), ApiClient.getAllSeries(srv))
            allSeriesCategories = cats
            allSeries = series
            saveToCache("ser", series)
            saveToCache("serCats", cats)
            true
        } catch (e: Exception) { false }
    }

    fun loadChannelsFromCache(): Boolean {
        val chs = loadListFromCache<Channel>("chs") ?: return false
        if (chs.isEmpty()) return false
        allChannels = chs
        return true
    }

    fun loadMoviesFromCache(): Boolean {
        val mov = loadListFromCache<Movie>("mov") ?: return false
        val cats = loadListFromCache<Category>("movCats") ?: return false
        if (mov.isEmpty()) return false
        allMovies = mov
        allMovieCategories = cats
        return true
    }

    fun loadSeriesFromCache(): Boolean {
        val ser = loadListFromCache<Series>("ser") ?: return false
        val cats = loadListFromCache<Category>("serCats") ?: return false
        if (ser.isEmpty()) return false
        allSeries = ser
        allSeriesCategories = cats
        return true
    }

    fun getChannelStreamUrl(ch: Channel): String {
        val srv = servers.firstOrNull() ?: return ""
        return "${srv.url}/live/${enc(srv.username)}/${enc(srv.password)}/${ch.streamId}.m3u8"
    }

    fun getMovieStreamUrl(movie: Movie): String {
        val srv = servers.firstOrNull() ?: return ""
        val ext = movie.containerExtension.ifEmpty { "mp4" }
        return "${srv.url}/movie/${enc(srv.username)}/${enc(srv.password)}/${movie.streamId}.$ext"
    }

    fun getEpisodeStreamUrl(srv: Server, episodeId: String, ext: String): String {
        return "${srv.url}/series/${enc(srv.username)}/${enc(srv.password)}/$episodeId.$ext"
    }

    fun getFirstServer() = servers.firstOrNull()

    private fun <T> saveToCache(key: String, data: T) {
        try { prefs.edit().putString(key, gson.toJson(data)).apply() } catch (e: Exception) { }
    }

    private inline fun <reified T> loadListFromCache(key: String): List<T>? {
        return try {
            val json = prefs.getString(key, null) ?: return null
            gson.fromJson<List<T>>(json, object : TypeToken<List<T>>() {}.type)
        } catch (e: Exception) { null }
    }

    fun clearCache() {
        prefs.edit().clear().apply()
    }

    private fun enc(s: String) = java.net.URLEncoder.encode(s, "UTF-8")
}
