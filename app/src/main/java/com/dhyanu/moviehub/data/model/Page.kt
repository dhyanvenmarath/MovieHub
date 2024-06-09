package com.dhyanu.moviehub.data.model


import com.google.gson.annotations.SerializedName

data class Page(
    @field:SerializedName("content-items") val contentItems: ContentItems,
    @field:SerializedName("page-num") val pageNum: String,
    @field:SerializedName("page-size") val pageSize: String,
    val title: String,
    @field:SerializedName("total-content-items") val totalContentItems: String
)