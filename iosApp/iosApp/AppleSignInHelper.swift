import SwiftUI
import FirebaseAuth
import AuthenticationServices

/**
 * Helper Swift natif pour Apple Sign In
 * Communication avec Kotlin via NSNotification
 */
class AppleSignInHelper: NSObject, ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding {

    static let shared = AppleSignInHelper()

    private override init() {
        super.init()
        // Écouter la notification pour démarrer le sign in
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleStartSignIn),
            name: NSNotification.Name("StartAppleSignIn"),
            object: nil
        )
    }

    @objc private func handleStartSignIn() {
        print("🍎 Starting Apple Sign In...")
        Task {
            await signIn()
        }
    }

    /**
     * Démarre le processus Apple Sign In
     */
    @MainActor
    private func signIn() async {
        let provider = ASAuthorizationAppleIDProvider()
        let request = provider.createRequest()
        request.requestedScopes = [.fullName, .email]

        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.presentationContextProvider = self
        controller.performRequests()
    }

    // MARK: - ASAuthorizationControllerDelegate

    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential,
              let identityTokenData = appleIDCredential.identityToken,
              let idTokenString = String(data: identityTokenData, encoding: .utf8) else {
            print("❌ Failed to get Apple ID credential")
            NotificationCenter.default.post(
                name: NSNotification.Name("AppleSignInFailure"),
                object: nil,
                userInfo: ["error": "Credential invalide"]
            )
            return
        }

        print("✅ Got Apple ID token, signing in to Firebase...")

        // Créer le credential Firebase
        let credential = OAuthProvider.credential(
            withProviderID: "apple.com",
            idToken: idTokenString,
            rawNonce: ""
        )

        // Authentifier avec Firebase
        Auth.auth().signIn(with: credential) { authResult, error in
            if let error = error {
                print("❌ Firebase sign in error: \(error.localizedDescription)")
                NotificationCenter.default.post(
                    name: NSNotification.Name("AppleSignInFailure"),
                    object: nil,
                    userInfo: ["error": error.localizedDescription]
                )
            } else {
                print("✅ Firebase sign in success!")
                NotificationCenter.default.post(
                    name: NSNotification.Name("AppleSignInSuccess"),
                    object: nil
                )
            }
        }
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        let authError = error as NSError

        print("❌ Apple Sign In error:")
        print("   Domain: \(authError.domain)")
        print("   Code: \(authError.code)")
        print("   Description: \(error.localizedDescription)")
        print("   UserInfo: \(authError.userInfo)")

        // Code 1001 = user cancelled
        if authError.code == 1001 {
            print("ℹ️ User cancelled Apple Sign In")
            NotificationCenter.default.post(
                name: NSNotification.Name("AppleSignInCancelled"),
                object: nil
            )
        } else {
            let errorMessage = "[\(authError.code)] \(error.localizedDescription)"
            NotificationCenter.default.post(
                name: NSNotification.Name("AppleSignInFailure"),
                object: nil,
                userInfo: ["error": errorMessage]
            )
        }
    }

    // MARK: - ASAuthorizationControllerPresentationContextProviding

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = scene.windows.first else {
            fatalError("No window found")
        }
        return window
    }
}
