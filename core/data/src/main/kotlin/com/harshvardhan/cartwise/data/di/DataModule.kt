package com.harshvardhan.cartwise.data.di

import android.content.Context
import androidx.room.Room
import com.harshvardhan.cartwise.data.BuildConfig
import com.harshvardhan.cartwise.data.database.CartDao
import com.harshvardhan.cartwise.data.database.CartwiseDatabase
import com.harshvardhan.cartwise.data.network.ProductApi
import com.harshvardhan.cartwise.data.repository.CartRepository
import com.harshvardhan.cartwise.data.repository.LocalCartRepository
import com.harshvardhan.cartwise.data.repository.OfferRepository
import com.harshvardhan.cartwise.data.repository.OfflineFirstProductRepository
import com.harshvardhan.cartwise.data.repository.ProductRepository
import com.harshvardhan.cartwise.data.repository.StaticOfferRepository
import com.harshvardhan.cartwise.pricing.PricingEngine
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun okHttp(@ApplicationContext context: Context): OkHttpClient = OkHttpClient.Builder()
        .cache(Cache(File(context.cacheDir, "http"), 10L * 1024 * 1024))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
            }
        }
        .build()

    @Provides
    @Singleton
    fun productApi(client: OkHttpClient): ProductApi {
        val json = Json { ignoreUnknownKeys = true }
        return Retrofit.Builder()
            .baseUrl(ProductApi.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ProductApi::class.java)
    }

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): CartwiseDatabase =
        Room.databaseBuilder(context, CartwiseDatabase::class.java, "cartwise.db").build()

    @Provides
    fun cartDao(db: CartwiseDatabase): CartDao = db.cartDao()

    @Provides
    @Singleton
    fun pricingEngine() = PricingEngine()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun productRepository(impl: OfflineFirstProductRepository): ProductRepository

    @Binds
    abstract fun cartRepository(impl: LocalCartRepository): CartRepository

    @Binds
    abstract fun offerRepository(impl: StaticOfferRepository): OfferRepository
}
