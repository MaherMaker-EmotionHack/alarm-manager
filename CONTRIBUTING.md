# Contributing to @mahermaker/android-alarm-manager

First off, thank you for considering contributing! Your help is appreciated.

This guide provides instructions for contributing to this Capacitor plugin. Please read it to help us keep contributions organized and effective.

## How to Contribute
There are many ways to contribute, from writing tutorials or blog posts, improving the documentation, submitting bug reports and feature requests or writing code which can be incorporated into the main project.

Please, don't use the issue tracker for support questions. For that, please refer to the plugin's README or seek help in relevant Capacitor community forums.

## Reporting Bugs
If you find a bug, please make sure to:
1.  **Check existing issues:** Someone else might have already reported the same bug.
2.  **Provide details:**
    *   A clear and descriptive title.
    *   Steps to reproduce the bug.
    *   What you expected to happen.
    *   What actually happened.
    *   Your environment (Android version, device model, Capacitor version, plugin version).
    *   Relevant code snippets.
    *   Screenshots or logs if applicable.

You can open a new issue [here](https://github.com/MaherMaker-EmotionHack/alarmmanager/issues). <!-- Confirm this is the correct issues link -->

## Suggesting Enhancements
If you have an idea for a new feature or an improvement to an existing one:
1.  **Check existing issues/discussions:** Your idea might have been discussed before.
2.  **Provide a clear proposal:**
    *   A clear and descriptive title.
    *   A detailed description of the proposed enhancement and its benefits.
    *   Any potential drawbacks or challenges.
    *   Example use cases.

Open a new issue or discussion to share your proposal.

## Pull Request Process

1.  **Fork the repository** and create your branch from `main` (or the relevant development branch).
2.  **Set up the development environment** (see below).
3.  **Make your changes.** Ensure you adhere to the existing code style.
4.  **Test your changes thoroughly.** Use the example app (`example-app/`) to test the plugin's functionality on an Android device or emulator.
5.  **Update the documentation** (`README.md`, JSDoc comments in `*.ts` files) if your changes affect the API or usage.
6.  **Ensure your code lints.** Run `npm run lint` and fix any issues.
7.  **Commit your changes** with a clear and descriptive commit message.
8.  **Push your branch** to your fork.
9.  **Open a pull request** to the main repository. Provide a clear description of your changes and link any relevant issues.

## Setting Up the Development Environment

1.  Fork and clone the repository:
    ```bash
    git clone https://github.com/YOUR_USERNAME/alarmmanager.git
    cd alarmmanager/alarm-manager
    ```
2.  Install the dependencies:
    ```bash
    npm install
    ```
3.  **Link the plugin for local development:**
    To test your changes in the `example-app/` or your own Capacitor project, you'll often want to link your local version of the plugin.
    *   In the `alarm-manager` directory:
        ```bash
        npm link
        npm run build # Or npm run watch for continuous building
        ```
    *   In your Capacitor app's root directory (e.g., `alarm-app/` or the `example-app/` if it's set up as a separate project):
        ```bash
        npm link @mahermaker/android-alarm-manager
        npx cap sync android
        ```
    This allows you to see your plugin changes reflected in the app when you rebuild and run it on Android.

4.  **Open the Android project:**
    Open `alarm-manager/android/` in Android Studio to work on the native Java code.

## Scripts

### `npm run build`
Build the plugin web assets (TypeScript to JavaScript) and generate plugin API documentation using `@capacitor/docgen`.

It compiles TypeScript from `src/` into ESM JavaScript in `dist/esm/`. These files are used in apps with bundlers. Rollup then bundles the code into `dist/plugin.js` for apps without bundlers.

### `npm run verify`
Build and validate the web and native projects. This is useful for CI.

### `npm run lint` / `npm run fmt`
Check formatting and code quality using ESLint and Prettier. `npm run fmt` will attempt to auto-fix issues.

## Code Style
Please try to follow the existing code style in the project. We use ESLint and Prettier for TypeScript/JavaScript, and standard Android Java conventions. Running `npm run lint` and `npm run fmt` can help maintain consistency.

## Publishing (For Maintainers)
There is a `prepublishOnly` hook in `package.json` which prepares the plugin before publishing (runs `npm run build`), so publishing is typically done by:
```shell
npm publish
```
> **Note**: The `files` array in `package.json` specifies which files get published. If you rename files/directories or add files elsewhere, you may need to update it.
