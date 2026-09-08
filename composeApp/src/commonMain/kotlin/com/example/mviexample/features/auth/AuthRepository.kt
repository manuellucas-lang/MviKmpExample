package com.example.mviexample.features.auth

import kotlinx.coroutines.flow.StateFlow

/**
 * Unified authentication contract on top of the native Firebase SDKs.
 *
 * The [authState] flow is the single source of truth for the current Firebase user:
 * it is updated by each platform implementation whenever the auth state changes
 * (sign in, sign out, profile update, session expiry, ...).
 *
 * Platform implementations:
 * - Android: `com.example.mviexample.features.auth.AndroidAuthRepository`
 * - iOS: `com.example.mviexample.features.auth.IosAuthRepository`
 */
interface AuthRepository {

    /** Reactive current user. Initially emitted when the repository is created. */
    val authState: StateFlow<AppUser?>

    suspend fun signInWithEmail(email: String, password: String): AuthResult

    suspend fun signUpWithEmail(email: String, password: String): AuthResult

    /** Launches the platform Google Sign-In flow and signs the user in with Firebase. */
    suspend fun signInWithGoogle(): AuthResult

    suspend fun signOut(): AuthResult

    suspend fun sendPasswordReset(email: String): AuthResult

    /** Sends a verification email to the current user's address (email provider only). */
    suspend fun sendEmailVerification(): AuthResult

    /** Links the currently signed-in user with Google (if not already linked). */
    suspend fun linkWithGoogle(): AuthResult

    /** Deletes the current account. May fail with [AuthErrorCode.REQUIRES_RECENT_LOGIN]. */
    suspend fun deleteAccount(): AuthResult

    /** Re-authenticates the current email/password user (used before destructive actions). */
    suspend fun reauthenticate(password: String): AuthResult

    /** Reloads the current user profile from the server. */
    suspend fun refreshUser(): AuthResult
}