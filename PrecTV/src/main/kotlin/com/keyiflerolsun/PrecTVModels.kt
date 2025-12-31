// PrecTV Provider for Cloudstream3

package com.keyiflerolsun

import com.fasterxml.jackson.annotation.JsonProperty

data class Source(
    @JsonProperty("id") val id: Int?,
    @JsonProperty("title") val title: String?,
    @JsonProperty("quality") val quality: String?,
    @JsonProperty("size") val size: String?,
    @JsonProperty("kind") val kind: String?,
    @JsonProperty("premium") val premium: String?,
    @JsonProperty("external") val external: Boolean?,
    @JsonProperty("type") val type: String?,
    @JsonProperty("url") val url: String?
)

data class Genre(
    @JsonProperty("id") val id: Int?,
    @JsonProperty("title") val title: String?
)

data class Trailer(
    @JsonProperty("id") val id: Int?,
    @JsonProperty("type") val type: String?,
    @JsonProperty("url") val url: String?
)

data class Poster(
    @JsonProperty("id") val id: Int?,
    @JsonProperty("title") val title: String?,
    @JsonProperty("label") val label: String?,
    @JsonProperty("sublabel") val sublabel: String?,
    @JsonProperty("type") val type: String?,
    @JsonProperty("description") val description: String?,
    @JsonProperty("year") val year: Int?,
    @JsonProperty("imdb") val imdb: Double?,
    @JsonProperty("rating") val rating: Double?,
    @JsonProperty("duration") val duration: String?,
    @JsonProperty("downloadas") val downloadas: String?,
    @JsonProperty("comment") val comment: Boolean?,
    @JsonProperty("playas") val playas: String?,
    @JsonProperty("classification") val classification: String?,
    @JsonProperty("image") val image: String?,
    @JsonProperty("cover") val cover: String?,
    @JsonProperty("genres") val genres: List<Genre>?,
    @JsonProperty("trailer") val trailer: Trailer?,
    @JsonProperty("sources") val sources: List<Source>?
)

data class Slide(
    @JsonProperty("id") val id: Int?,
    @JsonProperty("title") val title: String?,
    @JsonProperty("type") val type: String?,
    @JsonProperty("image") val image: String?,
    @JsonProperty("poster") val poster: Poster?
)

data class FirstResponse(
    @JsonProperty("channels") val channels: List<Any>?,
    @JsonProperty("slides") val slides: List<Slide>?
)

data class SearchResponse(
    @JsonProperty("channels") val channels: List<Any>?,
    @JsonProperty("posters") val posters: List<Poster>?
)

data class Episode(
    @JsonProperty("id") val id: Int?,
    @JsonProperty("title") val title: String?,
    @JsonProperty("description") val description: String?,
    @JsonProperty("duration") val duration: String?,
    @JsonProperty("downloadas") val downloadas: String?,
    @JsonProperty("playas") val playas: String?,
    @JsonProperty("sources") val sources: List<Source>?
)

data class Season(
    @JsonProperty("id") val id: Int?,
    @JsonProperty("title") val title: String?,
    @JsonProperty("episodes") val episodes: List<Episode>?
)
