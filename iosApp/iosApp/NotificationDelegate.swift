import Foundation
import UserNotifications
import shared

/**
 * Delegate pour gérer les interactions avec les notifications iOS.
 * Gère le tap sur une notification et la navigation vers l'écran correspondant.
 */
class NotificationDelegate: NSObject, UNUserNotificationCenterDelegate {

    // Référence au deepLinkRoute pour déclencher la navigation
    var deepLinkHandler: ((MaintenancesRoute) -> Void)?

    // Appelé quand l'app est au premier plan et une notification arrive
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        // Afficher la notification même si l'app est au premier plan
        completionHandler([.banner, .sound, .badge])
    }

    // Appelé quand l'utilisateur tape sur une notification
    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        let userInfo = response.notification.request.content.userInfo

        guard let bikeId = userInfo["bikeId"] as? String,
              let bikeName = userInfo["bikeName"] as? String,
              let countingMethodStr = userInfo["countingMethod"] as? String,
              let openTab = userInfo["openTab"] as? String else {
            print("[NotificationDelegate] Données de deep link manquantes")
            completionHandler()
            return
        }

        // Vérifier que la bike existe toujours
        // Note: En iOS, on ne peut pas facilement injecter BikeRepository ici
        // On va donc naviguer et laisser l'écran gérer l'erreur si la bike n'existe plus

        let initialTab = openTab == "todo" ? 1 : 0

        print("[NotificationDelegate] Deep link parsé : bikeId=\(bikeId), bikeName=\(bikeName), countingMethod=\(countingMethodStr), initialTab=\(initialTab)")

        // Créer la route de maintenance
        let route = MaintenancesRoute(
            bikeId: bikeId,
            bikeName: bikeName,
            countingMethodString: countingMethodStr,
            initialTab: Int32(initialTab)
        )

        // Appeler le handler de deep link (sera configuré dans iOSApp)
        deepLinkHandler?(route)

        completionHandler()
    }
}

// Extension pour créer facilement une MaintenancesRoute
extension MaintenancesRoute {
    convenience init(bikeId: String, bikeName: String, countingMethodString: String, initialTab: Int32) {
        self.init(
            bikeId: bikeId,
            bikeName: bikeName,
            countingMethodString: countingMethodString,
            initialTab: initialTab
        )
    }
}
