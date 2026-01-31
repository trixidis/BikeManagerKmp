import SwiftUI
import shared
import FirebaseCore
import GoogleSignIn
import UserNotifications

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    @State private var deepLinkRoute: Route? = nil

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
            ContentView(deepLinkRoute: $deepLinkRoute)
                .onOpenURL { url in
                    // Handle Google Sign-In callback URL
                    GIDSignIn.sharedInstance.handle(url)
                }
                // TODO: Réactiver après avoir ajouté NotificationDelegate.swift au projet Xcode
                // .onAppear {
                //     // Configure notification delegate handler
                //     appDelegate.notificationDelegate.deepLinkHandler = { route in
                //         deepLinkRoute = route
                //     }
                // }
        }
    }
}

// AppDelegate pour configurer les notifications
class AppDelegate: NSObject, UIApplicationDelegate {
    // TODO: Réactiver après avoir ajouté NotificationDelegate.swift au projet Xcode
    // let notificationDelegate = NotificationDelegate()

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // TODO: Réactiver après avoir ajouté NotificationDelegate.swift au projet Xcode
        // // Configurer le delegate des notifications
        // UNUserNotificationCenter.current().delegate = notificationDelegate
        // print("[BikeManager] Notification delegate configuré")
        return true
    }
}
