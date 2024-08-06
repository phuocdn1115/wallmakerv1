package com.x10.photo.maker.network

import com.x10.photo.maker.data.response.DataHomeResponse
import com.x10.photo.maker.data.response.DetailWallpaperResponse
import com.x10.photo.maker.data.response.InstallationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Url

interface Api {
    @GET
    suspend fun getDataHomeScreen(
        @Url url: String?,
        @Query("ownerItemNumber") ownerItemNumber: Int?,
        @Query("pageNumber") pageNumber: Int?
    ): Response<DataHomeResponse?>

    @GET
    suspend fun getDetailWallpaper(
        @Url url: String?
    ): Response<DetailWallpaperResponse?>

    @POST
    suspend fun checkingInstall(
        @Url url: String?,
        @Query("utm_source") utmSource: String?,
        @Query("utm_campaign") utmCampaign: String?,
        @Query("utm_content") utmContent: String?,
        @Query("utm_medium") utmMedium: String?,
        @Query("utm_term") utmTerm: String?,
    ): Response<InstallationResponse?>
}