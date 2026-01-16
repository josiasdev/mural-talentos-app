package com.edu.muraldetalentosapp.data.repository

import com.edu.muraldetalentosapp.data.model.User
import com.edu.muraldetalentosapp.ui.components.AccountType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val usersCollection by lazy { firestore.collection("users") }

    suspend fun signIn(email: String, password: String): FirebaseUser? {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user
    }

    suspend fun signUp(user: User, password: String): FirebaseUser? {
        val result = auth.createUserWithEmailAndPassword(user.email, password).await()
        val firebaseUser = result.user ?: return null

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(user.name)
            .build()
        firebaseUser.updateProfile(profileUpdates).await()

        val userWithUid = user.copy(uid = firebaseUser.uid)
        saveUser(userWithUid)

        return firebaseUser
    }

    suspend fun saveUser(user: User) {
        val userMap = mapOf(
            "uid" to user.uid,
            "name" to user.name,
            "email" to user.email,
            "type" to user.type.name,
            "phone" to user.phone,
            "about" to user.about
        )
        usersCollection.document(user.uid).set(userMap).await()
    }

    suspend fun getUserProfile(uid: String): User? {
        return try {
            val document = usersCollection.document(uid).get().await()
            if (document.exists()) {
                val data = document.data
                User(
                    uid = data?.get("uid") as? String ?: "",
                    name = data?.get("name") as? String ?: "",
                    email = data?.get("email") as? String ?: "",
                    type = AccountType.valueOf(data?.get("type") as? String ?: "CANDIDATE"),
                    phone = data?.get("phone") as? String ?: "",
                    about = data?.get("about") as? String ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getUsersByIds(userIds: List<String>): List<User> {
        if (userIds.isEmpty()) return emptyList()

        return try {
            val users = mutableListOf<User>()
            userIds.distinct().chunked(10).forEach { chunk ->
                val snapshot = firestore.collection("users")
                    .whereIn("uid", chunk)
                    .get()
                    .await()
                users.addAll(snapshot.toObjects(User::class.java))
            }
            users
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }
}
