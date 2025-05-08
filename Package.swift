// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "AlarmManager",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "AlarmManager",
            targets: ["ManagerPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "ManagerPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/ManagerPlugin"),
        .testTarget(
            name: "ManagerPluginTests",
            dependencies: ["ManagerPlugin"],
            path: "ios/Tests/ManagerPluginTests")
    ]
)