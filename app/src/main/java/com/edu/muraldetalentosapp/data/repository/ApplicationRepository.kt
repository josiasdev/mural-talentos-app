package com.edu.muraldetalentosapp.data.repository

import android.util.Log
import com.edu.muraldetalentosapp.data.model.Application
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ApplicationRepository {
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val applicationsCollection by lazy { firestore.collection("applications") }

    suspend fun applyToJob(application: Application) {
        val documentId = "${application.candidateId}_${application.jobId}"

        applicationsCollection.document(documentId).set(application).await()
    }

    suspend fun tryApplyToJob(application: Application, userRepository: UserRepository): Boolean {
        val candidateId = application.candidateId
        val profile = try {
            userRepository.getUserProfile(candidateId)
        } catch (_: Exception) {
            null
        }

        if (profile?.isComplete != true) {
            return false
        }

        applyToJob(application)
        return true
    }

    suspend fun getUserApplications(candidateId: String): List<Application> {
        return try {
            val snapshot = applicationsCollection
                .whereEqualTo("candidateId", candidateId)
                .get()
                .await()
            snapshot.documents.mapNotNull { it.toObject(Application::class.java)?.copy(id = it.id) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun getJobApplicationsCount(jobId: String): Int {
        return try {
            val snapshot = applicationsCollection
                .whereEqualTo("jobId", jobId)
                .get()
                .await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    suspend fun getApplicationsForJobs(jobIds: List<String>): List<Application> {
        if (jobIds.isEmpty()) return emptyList()

        return try {
            val applications = mutableListOf<Application>()
            jobIds.chunked(10).forEach { chunk ->
                val snapshot = applicationsCollection
                    .whereIn("jobId", chunk)
                    .get()
                    .await()
                applications.addAll(snapshot.documents.mapNotNull { it.toObject(Application::class.java)?.copy(id = it.id) })
            }
            applications
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun listenToUserAppliedJobIds(candidateId: String): Flow<Set<String>> = callbackFlow {
        if (candidateId.isBlank()) {
            trySend(emptySet())
            close()
            return@callbackFlow
        }

        val subscription = applicationsCollection
            .whereEqualTo("candidateId", candidateId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AppRepo", "listenToUserAppliedJobIds error: ${error.message}")
                    trySend(emptySet())
                    return@addSnapshotListener
                }
                val ids = snapshot?.documents?.mapNotNull { it.getString("jobId") }?.toSet() ?: emptySet()
                trySend(ids)
            }

        awaitClose { subscription.remove() }
    }

    // Novo: escuta contagens apenas para os jobIds fornecidos (permite cumprir regras de segurança)
    fun listenToApplicationCountsForJobIds(jobIds: List<String>): Flow<Map<String, Int>> = callbackFlow {
        if (jobIds.isEmpty()) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }

        val listeners = mutableListOf<com.google.firebase.firestore.ListenerRegistration>()
        val counts = mutableMapOf<String, Int>()

        // Cria um listener para cada chunk de até 10 ids (limite do whereIn)
        jobIds.chunked(10).forEach { chunk ->
            val registration = applicationsCollection
                .whereIn("jobId", chunk)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e("AppRepo", "listenToApplicationCountsForJobIds error: ${error.message}")
                        // Em caso de erro de permissão, envia mapa vazio para a UI e continua (não fecha o flow)
                        trySend(emptyMap())
                        return@addSnapshotListener
                    }

                    // Atualiza contagens a partir dos documentos retornados neste chunk
                    val grouped = snapshot?.documents
                        ?.groupingBy { it.getString("jobId") ?: "" }
                        ?.eachCount() ?: emptyMap()

                    // Atualiza o mapa global
                    synchronized(counts) {
                        // Remove previous keys from this chunk to avoid stale counts
                        chunk.forEach { counts.remove(it) }
                        counts.putAll(grouped.filterKeys { it.isNotBlank() })
                        trySend(counts.toMap())
                    }
                }

            listeners.add(registration)
        }

        awaitClose { listeners.forEach { it.remove() } }
    }

    fun listenToUserApplicationStatuses(candidateId: String): Flow<Map<String, String>> = callbackFlow {
        if (candidateId.isBlank()) {
            trySend(emptyMap())
            close()
            return@callbackFlow
        }

        val subscription = applicationsCollection
            .whereEqualTo("candidateId", candidateId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("AppRepo", "listenToUserApplicationStatuses error: ${error.message}")
                    trySend(emptyMap())
                    return@addSnapshotListener
                }

                val map = snapshot?.documents
                    ?.mapNotNull { doc ->
                        val jobId = doc.getString("jobId")
                        val status = doc.getString("status")
                        if (jobId != null && status != null) jobId to status else null
                    }?.toMap() ?: emptyMap()

                trySend(map)
            }

        awaitClose { subscription.remove() }
    }
}
