package com.x10.photo.maker.di

import android.content.Context
import com.x10.photo.maker.RealmManager
import com.x10.photo.maker.ads.banner.BannerAdsManager
import com.x10.photo.maker.PreferencesManager
import com.x10.photo.maker.ads.nativeads.*
import com.x10.photo.maker.ads.openapp.OpenAppAdsManager
import com.x10.photo.maker.navigation.NavigationManager
import com.x10.photo.maker.ads.rewarded.RewardedAdsManager
import com.x10.photo.maker.base.ConnectionLiveData
import com.x10.photo.maker.base.FirebaseManager
import com.x10.photo.maker.download.DownloadWallpaperManager
import com.alo.wall.maker.tracking.EventTrackingManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    /**Database local*/
    @Singleton
    @Provides
    fun providePreferencesManager(
        @ApplicationContext context: Context
    ) = PreferencesManager(context)

    @Singleton
    @Provides
    fun provideRealmManager(
        @ApplicationContext context: Context
    ) = RealmManager(context)

    /**Navigation*/
    @Singleton
    @Provides
    fun provideNavigationManager(
        @ApplicationContext context: Context
    ) = NavigationManager(context)

    /**Ads*/
    @Singleton
    @Provides
    fun providerNativeAdsInHomeManager(
        @ApplicationContext context: Context,
        eventTrackingManager: EventTrackingManager
    ) = NativeAdsInHomeManager(context, eventTrackingManager)

    @Singleton
    @Provides
    fun providerNativeAdsSetSuccessManager(
        @ApplicationContext context: Context,
        eventTrackingManager: EventTrackingManager
    ) = NativeAdsSetSuccessManager(context, eventTrackingManager)

    @Singleton
    @Provides
    fun providerNativeAdsInFrameSaving(
        @ApplicationContext context: Context,
        eventTrackingManager: EventTrackingManager
    ) = NativeAdsInFrameSaving(context, eventTrackingManager)

    @Singleton
    @Provides
    fun providerBannerAds(
        @ApplicationContext context: Context,
        eventTrackingManager: EventTrackingManager
    ) = BannerAdsManager(context, eventTrackingManager)

    @Singleton
    @Provides
    fun providerOpenAppAdsManager(
        @ApplicationContext context: Context,
        eventTrackingManager: EventTrackingManager
    ) = OpenAppAdsManager(context, eventTrackingManager)

    @Singleton
    @Provides
    fun providerRewardedAdsManager(
        @ApplicationContext context: Context,
        eventTrackingManager: EventTrackingManager
    ) = RewardedAdsManager(context, eventTrackingManager)

    @Singleton
    @Provides
    fun provideDownloadWallpaperManager(
        @ApplicationContext context: Context
    ) = DownloadWallpaperManager(context)

    @Singleton
    @Provides
    fun providerConnectionLiveData(
        @ApplicationContext context: Context
    ) = ConnectionLiveData(context)

    @Singleton
    @Provides
    fun providerFirebaseManager(
        @ApplicationContext context: Context
    ) = FirebaseManager(context)

    @Singleton
    @Provides
    fun providerEventTrackingManager(
        @ApplicationContext context: Context
    ) = EventTrackingManager(context)
}