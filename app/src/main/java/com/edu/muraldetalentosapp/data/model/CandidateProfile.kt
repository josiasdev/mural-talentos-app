package com.edu.muraldetalentosapp.data.model

data class CandidateProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val cpf: String = "",
    val phone: String = "",
    val about: String = "",
    val resumeUrl: String? = null,      // Link do PDF no Supabase
    val resumeFileName: String? = null  // Nome do arquivo para exibir na tela (ex: "curriculo.pdf")
) {
    // Converte para o formato do Firestore
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "email" to email,
            "cpf" to cpf,
            "phone" to phone,
            "about" to about,
            "resumeUrl" to resumeUrl,
            "resumeFileName" to resumeFileName
        )
    }
}