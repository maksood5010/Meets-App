package com.meetsportal.meets.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.meetsportal.meets.networking.Api
import com.meetsportal.meets.networking.ApiClient
import com.meetsportal.meets.networking.meetup.MeetUpService
import com.meetsportal.meets.networking.places.PlaceService
import com.meetsportal.meets.networking.post.PostService
import com.meetsportal.meets.networking.post.datasource.PostPagingDataSource
import com.meetsportal.meets.networking.profile.ProfileService
import com.meetsportal.meets.networking.registration.RegistrationApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    var api: Api? = null
    var storageReference: StorageReference? = null
    var postPagingDataSource: PostPagingDataSource? = null

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }


    @Provides
    @Singleton
    fun provideFireStore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance();
    }

    @Provides
    @Singleton
    fun provideFireStorage(): StorageReference {
        storageReference = FirebaseStorage.getInstance().reference;
        return storageReference!!
    }

    @Provides
    @Singleton
    fun provideApi(): Api {
        api = ApiClient.getRetrofit()
        return api!!
    }

    @Provides
    @Singleton
    fun provideRegistrationApiService(): RegistrationApiService {
        return RegistrationApiService()
    }

    @Provides
    @Singleton
    fun provideProfileApiService(): ProfileService {
        return ProfileService(storageReference!!)
    }


    /*@Provides
    @Singleton
    fun providePostPagingDataSource(): PostPagingDataSource{
        postPagingDataSource = PostPagingDataSource()
        return postPagingDataSource!!

    }*/

    @Provides
    @Singleton
    fun providePostApiService(): PostService {
        return PostService()
    }

    @Provides
    @Singleton
    fun providePlaceApiService(): PlaceService {
        return PlaceService()
    }

    @Provides
    @Singleton
    fun provideMeetUpApiService(): MeetUpService {
        return MeetUpService()
    }

}