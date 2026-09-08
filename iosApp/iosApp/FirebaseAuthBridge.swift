import Foundation
import UIKit
import FirebaseAuth
import GoogleSignIn

/// Bridges Firebase Auth between Kotlin/Native (ComposeApp framework) and the native
/// Firebase iOS SDK + Google Sign-In SDK.
///
/// Communication contract (dictionaries through `NotificationCenter`):
/// - Kotlin posts `AuthOperationRequest`  (requestId, action, email?, password?)
/// - Swift answers with `AuthOperationResponse` (requestId, status, errorCode?, message?)
/// - Auth state changes are pushed with `AuthStateChanged` (flat user payload)
/// - Kotlin can ask for the current state with `AuthGetCurrentUserRequest`
///
/// All user payloads are flat String-typed dictionaries so Kotlin can read them
/// without any type-casting ambiguity.
@objcMembers
final class FirebaseAuthBridge: NSObject {

    static let shared = FirebaseAuthBridge()

    private static let requestNotificationName = Notification.Name("AuthOperationRequest")
    private static let responseNotificationName = Notification.Name("AuthOperationResponse")
    private static let stateNotificationName = Notification.Name("AuthStateChanged")
    private static let getCurrentUserNotificationName = Notification.Name("AuthGetCurrentUserRequest")

    private static let googleSignInErrorDomain = "com.google.GIDSignIn"
    private static let googleSignInCancelledCode = -5

    private let center = NotificationCenter.default

    private override init() {
        super.init()
        center.addObserver(
            self,
            selector: #selector(handleOperationRequest(_:)),
            name: Self.requestNotificationName,
            object: nil
        )
        center.addObserver(
            self,
            selector: #selector(handleGetCurrentUser(_:)),
            name: Self.getCurrentUserNotificationName,
            object: nil
        )
        Auth.auth().addStateDidChangeListener { [weak self] _, user in
            self?.postAuthState(user: user)
        }
    }

    /// Call after `FirebaseApp.configure()` (e.g. from the app delegate).
    func start() {
        postAuthState(user: Auth.auth().currentUser)
    }

    // MARK: - Auth state

    @objc private func handleGetCurrentUser(_ notification: Notification) {
        postAuthState(user: Auth.auth().currentUser)
    }

    private func postAuthState(user: User?) {
        var info: [String: String] = [:]
        if let user = user {
            info["uid"] = user.uid
            info["email"] = user.email ?? ""
            info["displayName"] = user.displayName ?? ""
            info["photoUrl"] = user.photoURL?.absoluteString ?? ""
            info["isEmailVerified"] = String(user.isEmailVerified)
            info["isAnonymous"] = String(user.isAnonymous)
            info["createdAt"] = String(Int((user.metadata.creationDate?.timeIntervalSince1970 ?? 0) * 1000))
            info["providers"] = user.providerData
                .map(\.providerID)
                .filter { $0 != "firebase" }
                .joined(separator: "|")
        }
        center.post(name: Self.stateNotificationName, object: nil, userInfo: info)
    }

    // MARK: - Operation requests

    @objc private func handleOperationRequest(_ notification: Notification) {
        guard let userInfo = notification.userInfo,
              let requestId = userInfo["requestId"] as? String,
              let action = userInfo["action"] as? String else { return }

        let email = (userInfo["email"] as? String).orEmpty
        let password = (userInfo["password"] as? String).orEmpty

        Task { @MainActor [weak self] in
            guard let self else { return }
            do {
                switch action {
                case "signInWithEmail":
                    _ = try await Auth.auth().signIn(withEmail: email, password: password)
                    self.respondSuccess(requestId: requestId)

                case "signUpWithEmail":
                    _ = try await Auth.auth().createUser(withEmail: email, password: password)
                    self.respondSuccess(requestId: requestId)

                case "signOut":
                    try Auth.auth().signOut()
                    self.respondSuccess(requestId: requestId)

                case "sendPasswordReset":
                    try await Auth.auth().sendPasswordReset(withEmail: email)
                    self.respondSuccess(requestId: requestId)

                case "sendEmailVerification":
                    if let user = Auth.auth().currentUser {
                        try await user.sendEmailVerification()
                        self.respondSuccess(requestId: requestId)
                    } else {
                        self.respondError(requestId: requestId, code: "unknown", message: "No hay sesión iniciada")
                    }

                case "reauthenticate":
                    if let user = Auth.auth().currentUser, let userEmail = user.email {
                        let credential = EmailAuthProvider.credential(withEmail: userEmail, password: password)
                        try await user.reauthenticate(with: credential)
                        self.respondSuccess(requestId: requestId)
                    } else {
                        self.respondError(requestId: requestId, code: "unknown", message: "No hay sesión iniciada")
                    }

                case "deleteAccount":
                    if let user = Auth.auth().currentUser {
                        try await user.delete()
                        self.respondSuccess(requestId: requestId)
                    } else {
                        self.respondError(requestId: requestId, code: "unknown", message: "No hay sesión iniciada")
                    }

                case "refreshUser":
                    if let user = Auth.auth().currentUser {
                        try await user.reload()
                        self.respondSuccess(requestId: requestId)
                    } else {
                        self.respondSuccess(requestId: requestId)
                    }

                case "signInWithGoogle", "linkWithGoogle":
                    try await self.handleGoogle(requestId: requestId, link: action == "linkWithGoogle")

                default:
                    self.respondError(requestId: requestId, code: "unknown", message: "Acción no soportada: \(action)")
                }
            } catch {
                if Self.isGoogleCancellation(error) {
                    self.respondCancelled(requestId: requestId)
                } else {
                    let mapped = Self.mapError(error)
                    self.respondError(requestId: requestId, code: mapped.code, message: mapped.message)
                }
            }
        }
    }

    // MARK: - Google Sign-In

    /// Presents the Google Sign-In sheet and signs in (or links) with the resulting ID token.
    @MainActor
    private func handleGoogle(requestId: String, link: Bool) async throws {
        guard let topViewController = topViewController() else {
            respondError(requestId: requestId, code: "unknown", message: "No hay controlador de vista disponible")
            return
        }
        let result = try await GIDSignIn.sharedInstance.signIn(withPresenting: topViewController)
        guard let idToken = result.user.idToken?.tokenString else {
            respondError(requestId: requestId, code: "unknown", message: "No se pudo obtener el token de Google")
            return
        }
        let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: "")
        if link {
            guard let user = Auth.auth().currentUser else {
                respondError(requestId: requestId, code: "unknown", message: "No hay sesión iniciada")
                return
            }
            _ = try await user.link(with: credential)
        } else {
            _ = try await Auth.auth().signIn(with: credential)
        }
        respondSuccess(requestId: requestId)
    }

    @MainActor
    private func topViewController() -> UIViewController? {
        let scene = UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .first { $0.activationState == .foregroundActive }
        var top = scene?.keyWindow?.rootViewController
        while let presented = top?.presentedViewController {
            top = presented
        }
        return top
    }

    private static func isGoogleCancellation(_ error: Error) -> Bool {
        let nsError = error as NSError
        return nsError.domain == googleSignInErrorDomain && nsError.code == googleSignInCancelledCode
    }

    /// Normalizes an NSError into the same lowercase-hyphen codes used by the Android side.
    private static func mapError(_ error: Error) -> (code: String, message: String) {
        let nsError = error as NSError
        let code: String
        if let authError = AuthErrorCode(rawValue: nsError.code) {
            switch authError {
            case .invalidEmail: code = "invalid-email"
            case .wrongPassword: code = "wrong-password"
            case .userNotFound: code = "user-not-found"
            case .emailAlreadyInUse: code = "email-already-in-use"
            case .weakPassword: code = "weak-password"
            case .requiresRecentLogin: code = "requires-recent-login"
            case .networkError: code = "network-request-failed"
            case .userDisabled: code = "user-disabled"
            case .operationNotAllowed: code = "operation-not-allowed"
            case .tooManyRequests: code = "too-many-requests"
            default: code = "unknown"
            }
        } else {
            code = "unknown"
        }
        return (code, nsError.localizedDescription)
    }

    // MARK: - Responses

    private func respondSuccess(requestId: String) {
        center.post(
            name: Self.responseNotificationName,
            object: nil,
            userInfo: ["requestId": requestId, "status": "success"]
        )
        // Push the resulting auth state right after every successful operation.
        postAuthState(user: Auth.auth().currentUser)
    }

    private func respondCancelled(requestId: String) {
        center.post(
            name: Self.responseNotificationName,
            object: nil,
            userInfo: ["requestId": requestId, "status": "cancelled"]
        )
    }

    private func respondError(requestId: String, code: String, message: String) {
        center.post(
            name: Self.responseNotificationName,
            object: nil,
            userInfo: [
                "requestId": requestId,
                "status": "error",
                "errorCode": code,
                "message": message,
            ]
        )
    }
}

private extension Optional where Wrapped == String {
    var orEmpty: String { self ?? "" }
}