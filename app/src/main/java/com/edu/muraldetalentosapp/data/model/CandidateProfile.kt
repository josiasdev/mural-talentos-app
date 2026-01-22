package com.edu.muraldetalentosapp.data.model

data class CandidateProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val cpf: String = "",
    val resumeUrl: String? = null,
    val resumeFileName: String? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "email" to email,
            "cpf" to cpf,
            "resumeUrl" to resumeUrl,
            "resumeFileName" to resumeFileName
        )
    }
}