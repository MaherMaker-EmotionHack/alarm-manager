import Foundation

@objc public class Manager: NSObject {
    @objc public func echo(_ value: String) -> String {
        print(value)
        return value
    }
}
