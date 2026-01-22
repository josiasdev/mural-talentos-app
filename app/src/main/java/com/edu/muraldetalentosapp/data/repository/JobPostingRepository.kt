package com.edu.muraldetalentosapp.data.repository

import android.util.Log
import com.edu.muraldetalentosapp.data.model.JobPosting
import com.google.firebase.firestore.FirebaseFirestore
import com.edu.muraldetalentosapp.data.supabase.SupabaseManager
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class JobPostingRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val jobsCollection by lazy { firestore.collection("jobs") }

    private val storageBucket = SupabaseManager.client.storage.from("job-images")

    suspend fun uploadJobImage(imageBytes: ByteArray): String {
        val fileName = "job_${System.currentTimeMillis()}.jpg"

        storageBucket.upload(fileName, imageBytes, upsert = true)

        return storageBucket.publicUrl(fileName)
    }

    suspend fun saveJobPosting(job: JobPosting): String {
        val jobMap = mapOf(
            "title" to job.title,
            "company" to job.company,
            "companyId" to job.companyId,
            "description" to job.description,
            "location" to job.location,
            "type" to job.type,
            "contractType" to job.contractType,
            "salaryRange" to job.salaryRange,
            "isSalaryNegotiable" to job.isSalaryNegotiable,
            "publishedAt" to job.publishedAt,
            "datePosted" to job.datePosted,
            "expirationDate" to job.expirationDate,
            "isApplied" to job.isApplied,
            "imageUrl" to job.imageUrl,
            "latitude" to job.latitude,
            "longitude" to job.longitude,
            "isActive" to job.isActive
        )

        return if (job.id.isEmpty()) {
            val docRef = jobsCollection.add(jobMap).await()
            docRef.id
        } else {
            jobsCollection.document(job.id).set(jobMap).await()
            job.id
        }
    }

    suspend fun publishJob(
        title: String,
        description: String,
        location: String,
        contractType: String,
        salary: String,
        isSalaryNegotiable: Boolean,
        imageUrl: String?,
        latitude: Double?,
        longitude: Double?,
        companyId: String,
        companyName: String
    ): String {
        val currentTimestamp = System.currentTimeMillis()
        val thirtyDaysInMillis = 30L * 24 * 60 * 60 * 1000
        val expirationTimestamp = currentTimestamp + thirtyDaysInMillis
        val publishedAt = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date(currentTimestamp))

        val job = JobPosting(
            id = "",
            title = title,
            company = companyName,
            companyId = companyId,
            description = description,
            location = location,
            type = contractType,
            contractType = contractType,
            salaryRange = if (isSalaryNegotiable) "A combinar" else salary,
            isSalaryNegotiable = isSalaryNegotiable,
            publishedAt = publishedAt,
            datePosted = currentTimestamp,
            expirationDate = expirationTimestamp,
            isApplied = false,
            imageUrl = imageUrl,
            latitude = latitude ?: -4.9685,
            longitude = longitude ?: -39.0150,
            isActive = true
        )

        return saveJobPosting(job)
    }

    fun listenToActiveJobs(): Flow<List<JobPosting>> = callbackFlow {
        val subscription = jobsCollection
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Log the permission or other error but keep the flow alive for retries
                    Log.e("JobRepo", "listenToActiveJobs error: ${error.message}")
                    // send an empty list so UI can react but do not close the flow to avoid bubbling the exception
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val jobs = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(JobPosting::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(jobs)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun getAllActiveJobPostings(): List<JobPosting> {
        return try {
            val snapshot = jobsCollection
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(JobPosting::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            android.util.Log.e("JobRepo", "getAllActiveJobPostings failed", e)
            emptyList()
        }
    }

    suspend fun getJobPostingsByCompany(companyId: String): List<JobPosting> {
        return try {
            val snapshot = jobsCollection
                .whereEqualTo("companyId", companyId)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(JobPosting::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            android.util.Log.e("JobRepo", "getJobPostingsByCompany failed for companyId=$companyId", e)
            emptyList()
        }
    }

    suspend fun closeJob(jobId: String) {
        jobsCollection.document(jobId)
            .update("isActive", false)
            .await()
    }
}
