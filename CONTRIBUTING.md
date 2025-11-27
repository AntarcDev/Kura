# Contributing to Kura

First off, thanks for taking the time to contribute! 🎉

The following is a set of guidelines for contributing to Kura. These are mostly guidelines, not rules. Use your best judgment, and feel free to propose changes to this document in a pull request.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Pull Requests](#pull-requests)
- [Development Setup](#development-setup)
- [Styleguides](#styleguides)

## 🤝 Code of Conduct

This project and everyone participating in it is governed by a Code of Conduct. By participating, you are expected to uphold this code. Please report unacceptable behavior to the project maintainers.

## 🚀 How Can I Contribute?

### Reporting Bugs

This section guides you through submitting a bug report. Following these guidelines helps maintainers and the community understand your report, reproduce the behavior, and find related reports.

- **Use a clear and descriptive title** for the issue to identify the problem.
- **Describe the exact steps to reproduce the problem** in as many details as possible.
- **Provide specific examples** to demonstrate the steps.
- **Describe the behavior you observed** after following the steps and point out what exactly is the problem with that behavior.
- **Explain which behavior you expected to see instead and why.**
- **Include screenshots or screen recordings** if possible.

### Suggesting Enhancements

This section guides you through submitting an enhancement suggestion, including completely new features and minor improvements to existing functionality.

- **Use a clear and descriptive title** for the issue to identify the suggestion.
- **Provide a step-by-step description of the suggested enhancement** in as many details as possible.
- **Explain why this enhancement would be useful** to most users.

### Pull Requests

The process described here has several goals:

- Maintain Kura's quality.
- Fix problems that are important to users.
- Engage the community in working toward the best possible app.

1.  **Fork the repo** and create your branch from `main`.
2.  **Clone the project** to your local machine.
3.  **Create a new branch** for your feature or bug fix: `git checkout -b feature/amazing-feature`.
4.  **Commit your changes** with a descriptive commit message.
5.  **Push to your branch**: `git push origin feature/amazing-feature`.
6.  **Submit a Pull Request** to the `main` branch.

## 💻 Development Setup

1.  **Android Studio**: Ensure you have Android Studio Hedgehog or newer installed.
2.  **JDK**: Use JDK 17 or newer.
3.  **Build**: Open the project and let Gradle sync. Run `./gradlew assembleDebug` to build.

## 🎨 Styleguides

### Kotlin Styleguide

- We follow the [official Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Use `val` over `var` whenever possible.
- Keep functions small and focused.

### Jetpack Compose

- Follow [Compose API guidelines](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md).
- State should be hoisted whenever possible.
- Use `Modifier` as the first optional parameter in Composable functions.

### Git Commit Messages

- Use the present tense ("Add feature" not "Added feature").
- Use the imperative mood ("Move cursor to..." not "Moves cursor to...").
- Limit the first line to 72 characters or less.
- Reference issues and pull requests liberally after the first line.

---

Thank you for contributing! 🚀
