package com.edu.muraldetalentosapp.data.repository

import com.edu.muraldetalentosapp.data.model.JobPosting
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class JobPostingRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val jobsCollection by lazy { firestore.collection("jobs") }

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

    fun listenToActiveJobs(): Flow<List<JobPosting>> = callbackFlow {
        val subscription = jobsCollection
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
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
            emptyList()
        }
    }

    suspend fun deleteJobPosting(jobId: String) {
        jobsCollection.document(jobId).delete().await()
    }
}
