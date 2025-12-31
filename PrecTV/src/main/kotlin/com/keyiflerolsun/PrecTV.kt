// PrecTV Provider for Cloudstream3

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addTrailer

class PrecTV : MainAPI() {
    override var mainUrl              = "https://m.prectv60.lol"
    override var name                 = "PrecTV"
    override val hasMainPage          = true
    override var lang                 = "tr"
    override val hasQuickSearch       = true
    override val supportedTypes       = setOf(TvType.TvSeries, TvType.Movie)

    private val TOKEN = "4F5A9C3D9A86FA54EACEDDD635185"
    private val UUID  = "c3c5bd17-e37b-4b94-a944-8a3688a30452"

    override val mainPage = mainPageOf(
        "featured" to "Öne Çıkanlar"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val apiUrl   = "${mainUrl}/api/first/${TOKEN}/${UUID}/"
        val response = app.get(apiUrl).parsedSafe<FirstResponse>() ?: return newHomePageResponse(listOf())

        val home = response.slides?.mapNotNull { slide ->
            slide.poster?.toSearchResponse()
        } ?: emptyList()

        return newHomePageResponse(listOf(HomePageList(request.name, home)))
    }

    private fun Poster.toSearchResponse(): com.lagradost.cloudstream3.SearchResponse? {
        val posterId   = this.id ?: return null
        val posterType = this.type ?: return null
        val title      = this.title ?: return null
        val posterUrl  = fixUrlNull(this.image)

        return when (posterType) {
            "movie" -> newMovieSearchResponse(title, "${posterId}|movie", TvType.Movie) {
                this.posterUrl = posterUrl
            }
            "serie" -> newTvSeriesSearchResponse(title, "${posterId}|serie", TvType.TvSeries) {
                this.posterUrl = posterUrl
            }
            else -> null
        }
    }

    override suspend fun search(query: String): List<com.lagradost.cloudstream3.SearchResponse> {
        val apiUrl   = "${mainUrl}/api/search/${query}/${TOKEN}/${UUID}/"
        val response = app.get(apiUrl).parsedSafe<PrecTVSearchResponse>() ?: return emptyList()

        return response.posters?.mapNotNull { it.toSearchResponse() } ?: emptyList()
    }

    override suspend fun quickSearch(query: String): List<com.lagradost.cloudstream3.SearchResponse> = search(query)

    override suspend fun load(url: String): LoadResponse? {
        // url format: {id}|{type}
        val parts      = url.split("|")
        val posterId   = parts.getOrNull(0) ?: return null
        val posterType = parts.getOrNull(1) ?: return null

        // Get poster details from search (we need to search by ID or use first endpoint)
        // Since we don't have a direct endpoint for single poster, we'll construct from URL
        // For now, we'll use the search endpoint and filter by ID
        // Alternative: Store poster data in URL or use a different approach

        // Using first endpoint to get all data
        val firstResponse = app.get("${mainUrl}/api/first/${TOKEN}/${UUID}/").parsedSafe<FirstResponse>()
        val poster = firstResponse?.slides?.firstOrNull { it.poster?.id?.toString() == posterId }?.poster

        if (poster == null) {
            // Try search endpoint
            val searchResponse = app.get("${mainUrl}/api/search/${posterId}/${TOKEN}/${UUID}/").parsedSafe<PrecTVSearchResponse>()
            val searchPoster = searchResponse?.posters?.firstOrNull { it.id?.toString() == posterId }
            return searchPoster?.toLoadResponse(posterId, posterType)
        }

        return poster.toLoadResponse(posterId, posterType)
    }

    private suspend fun Poster.toLoadResponse(posterId: String, posterType: String): LoadResponse? {
        val title       = this.title ?: return null
        val posterUrl   = fixUrlNull(this.image)
        val description = this.description
        val year        = this.year
        val score       = this.rating?.times(1000)?.toInt()
        val tags        = this.genres?.mapNotNull { it.title }
        val trailer     = this.trailer?.url

        return when (posterType) {
            "movie" -> {
                newMovieLoadResponse(title, "${posterId}|movie", TvType.Movie, "${posterId}|movie") {
                    this.posterUrl = posterUrl
                    this.plot      = description
                    this.year      = year
                    this.score     = score
                    this.tags      = tags
                    addTrailer(trailer)
                }
            }
            "serie" -> {
                // Get episodes from season endpoint
                val episodeList = mutableListOf<com.lagradost.cloudstream3.Episode>()
                val seasonsResponse = app.get("${mainUrl}/api/season/by/serie/${posterId}/${TOKEN}/${UUID}/").parsedSafe<List<Season>>()

                seasonsResponse?.forEach { season ->
                    val seasonTitle = season.title ?: ""
                    val seasonNumber = Regex("""(\d+)""").find(seasonTitle)?.groupValues?.get(1)?.toIntOrNull()

                    season.episodes?.forEach { episode ->
                        val epTitle = episode.title ?: return@forEach
                        val epId    = episode.id ?: return@forEach
                        val epNumber = Regex("""(\d+)""").find(epTitle)?.groupValues?.get(1)?.toIntOrNull()

                        episodeList.add(newEpisode("${posterId}|${epId}|episode") {
                            this.name    = epTitle
                            this.season  = seasonNumber
                            this.episode = epNumber
                        })
                    }
                }

                newTvSeriesLoadResponse(title, "${posterId}|serie", TvType.TvSeries, episodeList) {
                    this.posterUrl = posterUrl
                    this.plot      = description
                    this.year      = year
                    this.score     = score
                    this.tags      = tags
                    addTrailer(trailer)
                }
            }
            else -> null
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        Log.d("PrecTV", "data: ${data}")

        val parts    = data.split("|")
        val seriesId = parts.getOrNull(0) ?: return false
        val epId     = parts.getOrNull(1)
        val type     = parts.getOrNull(2) ?: parts.getOrNull(1) ?: return false

        when (type) {
            "movie" -> {
                // For movies, we need to get sources from poster data
                val firstResponse = app.get("${mainUrl}/api/first/${TOKEN}/${UUID}/").parsedSafe<FirstResponse>()
                val poster = firstResponse?.slides?.firstOrNull { it.poster?.id?.toString() == seriesId }?.poster

                poster?.sources?.forEach { source ->
                    source.url?.let { url ->
                        callback.invoke(
                            ExtractorLink(
                                source = this.name,
                                name = source.title ?: this.name,
                                url = url,
                                referer = mainUrl,
                                quality = getQualityFromName(source.quality ?: ""),
                                isM3u8 = source.type == "m3u8"
                            )
                        )
                    }
                }
            }
            "episode" -> {
                // For episodes, fetch all seasons and find the specific episode
                val seasonsResponse = app.get("${mainUrl}/api/season/by/serie/${seriesId}/${TOKEN}/${UUID}/").parsedSafe<List<Season>>()

                seasonsResponse?.forEach { season ->
                    season.episodes?.forEach { episode ->
                        if (episode.id?.toString() == epId) {
                            episode.sources?.forEach { source ->
                                source.url?.let { url ->
                                    callback.invoke(
                                        ExtractorLink(
                                            source = this.name,
                                            name = source.title ?: this.name,
                                            url = url,
                                            referer = mainUrl,
                                            quality = getQualityFromName(source.quality ?: ""),
                                            isM3u8 = source.type == "m3u8"
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> return false
        }

        return true
    }
}
