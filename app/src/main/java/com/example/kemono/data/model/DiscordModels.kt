package com.example.kemono.data.model

import com.google.gson.annotations.SerializedName

data class DiscordChannel(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

data class DiscordPost(
    @SerializedName("id") val id: String,
    @SerializedName("content") val content: String?,
    @SerializedName("added") val added: String?,
    @SerializedName("published") val published: String?,
    @SerializedName("edited") val edited: String?,
    @SerializedName("server") val server: String?,
    @SerializedName("channel") val channel: String?,
    @SerializedName("author") val author: DiscordAuthor?,
    @SerializedName("attachments") val attachments: List<DiscordAttachment> = emptyList(),
    @SerializedName("embeds") val embeds: List<DiscordEmbed> = emptyList()
)

data class DiscordAuthor(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("discriminator") val discriminator: String?
)

data class DiscordAttachment(
    @SerializedName("name") val name: String?,
    @SerializedName("path") val path: String?
)

data class DiscordEmbed(
    @SerializedName("title") val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("image") val image: DiscordAttachment?,
    @SerializedName("thumbnail") val thumbnail: DiscordAttachment?,
    @SerializedName("video") val video: DiscordAttachment?,
    @SerializedName("provider") val provider: DiscordProvider?,
    @SerializedName("author") val author: DiscordEmbedAuthor?
)

data class DiscordProvider(
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?
)

data class DiscordEmbedAuthor(
    @SerializedName("name") val name: String?,
    @SerializedName("url") val url: String?,
    @SerializedName("icon_url") val iconUrl: String?
)
