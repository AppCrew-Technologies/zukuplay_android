package dev.anilbeesetti.nextplayer.core.ads.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dev.anilbeesetti.nextplayer.core.ads.models.AdConfig
import dev.anilbeesetti.nextplayer.core.ads.models.AdManagerConfig
import dev.anilbeesetti.nextplayer.core.ads.models.AdPerformance
import dev.anilbeesetti.nextplayer.core.ads.models.AdPlacement
import dev.anilbeesetti.nextplayer.core.ads.models.AdType
import dev.anilbeesetti.nextplayer.core.ads.models.AdsData
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val ADS_COLLECTION = "ad_configs"
        private const val AD_MANAGER_CONFIG_DOC = "ad_manager_config"
        private const val AD_PERFORMANCE_COLLECTION = "ad_performance"
    }

    suspend fun getActiveAds(): List<AdConfig> {
        return try {
            val snapshot = firestore.collection(ADS_COLLECTION)
                .whereEqualTo("isActive", true)
                .orderBy("priority", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(AdConfig::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAdsByType(type: AdType): List<AdConfig> {
        return try {
            val snapshot = firestore.collection(ADS_COLLECTION)
                .whereEqualTo("type", type.value)
                .whereEqualTo("isActive", true)
                .orderBy("priority", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(AdConfig::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAdsByPlacement(placement: AdPlacement): List<AdConfig> {
        return try {
            val snapshot = firestore.collection(ADS_COLLECTION)
                .whereEqualTo("placement", placement.value)
                .whereEqualTo("isActive", true)
                .orderBy("priority", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(AdConfig::class.java)?.copy(id = document.id)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAdConfig(id: String): AdConfig? {
        return try {
            val document = firestore.collection(ADS_COLLECTION)
                .document(id)
                .get()
                .await()

            if (document.exists()) {
                document.toObject(AdConfig::class.java)?.copy(id = document.id)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAdManagerConfig(): AdManagerConfig? {
        return try {
            val document = firestore.collection(AD_MANAGER_CONFIG_DOC)
                .document("config")
                .get()
                .await()

            if (document.exists()) {
                document.toObject(AdManagerConfig::class.java)
            } else {
                // Return default config if none exists
                AdManagerConfig()
            }
        } catch (e: Exception) {
            AdManagerConfig()
        }
    }

    suspend fun updateAdPerformance(performance: AdPerformance) {
        try {
            firestore.collection(AD_PERFORMANCE_COLLECTION)
                .document(performance.adConfigId)
                .set(performance)
                .await()
        } catch (e: Exception) {
            // Handle error silently for performance tracking
        }
    }

    suspend fun getAdPerformance(adConfigId: String): AdPerformance? {
        return try {
            val document = firestore.collection(AD_PERFORMANCE_COLLECTION)
                .document(adConfigId)
                .get()
                .await()

            if (document.exists()) {
                document.toObject(AdPerformance::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllAdPerformance(): List<AdPerformance> {
        return try {
            val snapshot = firestore.collection(AD_PERFORMANCE_COLLECTION)
                .get()
                .await()

            snapshot.documents.mapNotNull { document ->
                try {
                    document.toObject(AdPerformance::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Add this method to get ads data
    suspend fun getAdsData(): AdsData? {
        return try {
            val document = firestore.collection("static_data")
                .document("ad_details")
                .get()
                .await()

            if (document.exists()) {
                document.toObject(AdsData::class.java)
            } else {
                // Return default ads data if document doesn't exist
                AdsData()
            }
        } catch (e: Exception) {
            val roror =e.message.toString()
            val roror1 =e.message.toString()
            //Timber.e(e, "Error fetching ads data from Firestore")
            // Return default ads data on error
            AdsData()
        }
    }

    // Add this method for real-time updates
    fun getAdsDataLiveData(): LiveData<AdsData> {
        val liveData = MutableLiveData<AdsData>()

        firestore.collection("static_data")
            .document("static_data")
            .addSnapshotListener { snapshot, error ->
                error?.let {
                   // Timber.e(it, "Error listening to ads data")
                    liveData.value = AdsData() // Return default on error
                    return@addSnapshotListener
                }

                snapshot?.let { document ->
                    if (document.exists()) {
                        val adsData = document.toObject(AdsData::class.java)
                        adsData?.let {
                            liveData.value = it
                           // Timber.d("Real-time AdsData update: $it")
                        }
                    } else {
                        liveData.value = AdsData() // Return default if document doesn't exist
                    }
                }
            }

        return liveData
    }
}

