import SwiftUI
import shared
import FirebaseCore
import GoogleSignIn

@main
struct iOSApp: App {

    init() {
        // Configure Firebase on app launch
        FirebaseApp.configure()

        // Initialize KMPAuth with the iOS Client ID from GoogleService-Info.plist
        if let clientId = FirebaseApp.app()?.options.clientID {
            print("[BikeManager] Found client ID: \(clientId)")
            do {
                KMPAuthInitializer.shared.initialize(clientId: clientId)
                print("[BikeManager] KMPAuth initialized successfully")
            } catch {
                print("[BikeManager] KMPAuth init error: \(error)")
            }
        } else {
            print("[BikeManager] ERROR: Could not get Firebase client ID")
        }

        print("[BikeManager] Init complete, starting UI...")

        #if DEBUG
        print("[BikeManager] Running in DEBUG mode (DEV Firebase)")
        #else
        print("[BikeManager] Running in RELEASE mode (PROD Firebase)")
        #endif
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    // Handle Google Sign-In callback URL
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
