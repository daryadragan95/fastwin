package com.striklewin.apps.data.web

import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class FirestoreWebConfigRepository : WebConfigRepository {

    override suspend fun getWebViewUrl(): String? {
        return suspendCancellableCoroutine { continuation ->
            FirebaseFirestore.getInstance()
                .collection("config")
                .document("app")
                .get()
                .addOnSuccessListener { document ->
                    if (continuation.isActive) {
                        continuation.resume(document?.getString("url"))
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
        }
    }
}
