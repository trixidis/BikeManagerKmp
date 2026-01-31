import SwiftUI
import shared

struct ContentView: View {
    @Binding var deepLinkRoute: Route?

    var body: some View {
        ComposeView(deepLinkRoute: deepLinkRoute)
            .ignoresSafeArea(.keyboard)
    }
}

struct ComposeView: UIViewControllerRepresentable {
    let deepLinkRoute: Route?

    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(deepLinkRoute: deepLinkRoute)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
