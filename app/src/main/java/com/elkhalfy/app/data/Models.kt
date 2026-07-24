package com.elkhalfy.app.data

import com.google.gson.annotations.SerializedName

data class Channel(
    @SerializedName("stream_id") val streamId: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("stream_icon") val streamIcon: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    var catName: String = ""
)

data class Category(
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("category_name") val categoryName: String = "",
    @SerializedName("parent_id") val parentId: Int = 0
)

data class Movie(
    @SerializedName("stream_id") val streamId: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("stream_icon") val streamIcon: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("rating") val rating: String = "",
    @SerializedName("container_extension") val containerExtension: String = "mp4",
    @SerializedName("category_name") val categoryName: String = ""
)

data class Series(
    @SerializedName("series_id") val seriesId: Int = 0,
    @SerializedName("name") val name: String = "",
    @SerializedName("cover") val cover: String = "",
    @SerializedName("stream_icon") val streamIcon: String = "",
    @SerializedName("category_id") val categoryId: String = "",
    @SerializedName("rating") val rating: String = "",
    @SerializedName("num_seasons") val numSeasons: Int = 0,
    @SerializedName("category_name") val categoryName: String = ""
)

data class SeriesInfo(
    @SerializedName("episodes") val episodes: Map<String, List<Episode>> = emptyMap()
)

data class Episode(
    @SerializedName("id") val id: String = "",
    @SerializedName("episode_num") val episodeNum: Int = 0,
    @SerializedName("title") val title: String = "",
    @SerializedName("container_extension") val containerExtension: String = "mp4"
)

data class Server(
    val url: String = "",
    val username: String = "",
    val password: String = ""
)

data class AppConfig(
    val name: String = "Elkhalfy",
    val features: Features = Features(),
    val social: List<SocialLink> = emptyList(),
    val maintMsg: String = ""
)

data class Features(
    val maintenance: Boolean = false
)

data class SocialLink(
    val platform: String = "",
    val label: String = "",
    val url: String = ""
)

// Football models
data class FixturesResponse(val response: List<FixtureItem> = emptyList())

data class FixtureItem(
    val fixture: FixtureData = FixtureData(),
    val league: LeagueData = LeagueData(),
    val teams: TeamsData = TeamsData(),
    val goals: GoalsData = GoalsData(),
    val events: List<MatchEvent> = emptyList()
)

data class FixtureData(
    val id: Int = 0,
    val date: String = "",
    val status: StatusData = StatusData()
)

data class StatusData(
    val short: String = "",
    val elapsed: Int? = null
)

data class LeagueData(
    val id: Int = 0,
    val name: String = "",
    val logo: String = "",
    val country: String = ""
)

data class TeamsData(
    val home: TeamData = TeamData(),
    val away: TeamData = TeamData()
)

data class TeamData(
    val id: Int = 0,
    val name: String = "",
    val logo: String = ""
)

data class GoalsData(
    val home: Int? = null,
    val away: Int? = null
)

data class MatchEvent(
    val time: EventTime = EventTime(),
    val team: TeamData = TeamData(),
    val player: PlayerRef = PlayerRef(),
    val type: String = "",
    val detail: String = ""
)

data class EventTime(val elapsed: Int = 0, val extra: Int? = null)
data class PlayerRef(val name: String = "")

data class StandingsResponse(val response: List<StandingLeague> = emptyList())
data class StandingLeague(val league: StandingLeagueData = StandingLeagueData())
data class StandingLeagueData(val standings: List<List<StandingEntry>> = emptyList())
data class StandingEntry(
    val rank: Int = 0,
    val team: TeamData = TeamData(),
    val points: Int = 0,
    val all: AllStats = AllStats(),
    val group: String = ""
)
data class AllStats(val played: Int = 0, val win: Int = 0, val draw: Int = 0, val lose: Int = 0)

data class TopScorersResponse(val response: List<ScorerItem> = emptyList())
data class ScorerItem(
    val player: PlayerData = PlayerData(),
    val statistics: List<PlayerStats> = emptyList()
)
data class PlayerData(val id: Int = 0, val name: String = "", val photo: String = "")
data class PlayerStats(
    val team: TeamData = TeamData(),
    val goals: GoalStats = GoalStats()
)
data class GoalStats(val total: Int = 0)
