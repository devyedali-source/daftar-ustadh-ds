package com.elyedali.daftarustadh.auth

import android.content.Context
import com.elyedali.daftarustadh.AppRole
import com.elyedali.daftarustadh.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

/**
 * الدخول بحساب Google (مشروع Firebase الخاص بـ DS). حساب واحد للتطبيقين؛
 * يُسجَّل المستخدم في «ds_users» مع التطبيق الذي دخل منه (الإدارة أو الأستاذ). لا يُطلب رقم واتساب.
 */
object DsAuth {
    const val OWNER_EMAIL = "elyedalimoctar@gmail.com"

    val user: FirebaseUser? get() = FirebaseAuth.getInstance().currentUser
    val isOwner: Boolean get() = user?.email.equals(OWNER_EMAIL, ignoreCase = true)

    fun googleClient(context: Context) = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    )

    /** إكمال الدخول بعد اختيار الحساب: Firebase ثم تسجيل المستخدم في الخادم. */
    suspend fun signInWithIdToken(idToken: String): FirebaseUser? {
        val result = FirebaseAuth.getInstance().signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
        result.user?.let { registerUser(it) }
        return result.user
    }

    /** سجل المستخدم: الاسم والبريد، والتطبيق الذي يستعمله، وآخر دخول. */
    suspend fun registerUser(u: FirebaseUser) {
        val app = if (AppRole.isAdmin) "admin" else "teacher"
        val data = hashMapOf<String, Any>(
            "uid" to u.uid,
            "email" to (u.email ?: ""),
            "name" to (u.displayName ?: ""),
            "apps.$app" to true,
            "lastSeen_$app" to FieldValue.serverTimestamp()
        )
        try {
            val ref = FirebaseFirestore.getInstance().collection("ds_users").document(u.uid)
            // الحقول المنقوطة تُكتب بـ update؛ وإن لم يوجد السجل يُنشأ أولًا
            ref.set(hashMapOf("uid" to u.uid, "email" to (u.email ?: ""), "name" to (u.displayName ?: ""),
                "createdAt" to FieldValue.serverTimestamp()), SetOptions.mergeFields("uid", "email", "name")).await()
            ref.update(data as Map<String, Any>).await()
            val snap = ref.get().await()
            if (snap.get("createdAt") == null) ref.update("createdAt", FieldValue.serverTimestamp()).await()
        } catch (e: Exception) {
            // بلا إنترنت: يُعاد التسجيل في الدخول القادم
        }
    }

    fun signOut(context: Context) {
        FirebaseAuth.getInstance().signOut()
        try { googleClient(context).signOut() } catch (e: Exception) { }
    }
}
