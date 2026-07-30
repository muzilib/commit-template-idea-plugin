# Git Commit Template Plugin for IntelliJ IDEA

[中文文档](docs/插件说明文档.md) · [English Documentation](docs/Plugin-Documentation.md)

Git Commit Template is an IntelliJ IDEA plugin for creating, formatting, and validating structured Git commit messages. It also provides AI-generated commit-message suggestions from the changes selected in IDEA's **Included Changes** list.

This project is maintained and extended from the original [MobileTribe/commit-template-idea-plugin](https://github.com/MobileTribe/commit-template-idea-plugin).

## Features

- **Structured Commit Messages** — Create Conventional Commits-style messages from a template:

  ```text
  <type>(<scope>): <subject>

  <body>

  <footer>
  ```

- **Configurable Rules** — Configure commit types, scope requirements, and local validation rules before committing.
- **Global and Project Settings** — Use global template defaults and override them for individual projects when needed.
- **IDE Commit Integration** — Access the template editor directly from the Git Commit tool window; the default shortcut is `Alt + Shift + Q`.
- **AI Commit Suggestions** — Generate suggestions from the files currently selected in **Included Changes**. AI only suggests a Commit Message; it never commits, pushes, stages files, or changes source code.
- **Multiple AI Providers** — Supports Qwen, ChatGPT/OpenAI, DeepSeek, and custom OpenAI-Compatible Chat Completions endpoints.
- **Streaming and Review Modes** — Review the complete request before sending, or stream a readable draft directly into the Commit Message and validate the final result after streaming ends.
- **Privacy Controls** — Sends only filtered Included Changes; excludes binary and sensitive files, applies user exclusion patterns, uses project-relative paths, and asks for consent before the first real Diff transfer.
- **Secure Credentials** — Stores one independent API key per provider in IntelliJ Password Safe rather than ordinary plugin settings.
- **Qwen Options** — Provides optional Qwen sampling, thinking, search, streaming-usage, and data-inspection settings.
- **Localization** — Includes multilingual UI resources.

## Documentation

Complete installation, configuration, privacy, troubleshooting, architecture, development, and Provider-extension guidance is available in both languages:

| Language | Documentation |
| --- | --- |
| 简体中文 | [插件说明文档](docs/插件说明文档.md) |
| English | [Plugin Documentation](docs/Plugin-Documentation.md) |

## Installation

Install the plugin from **Settings / Preferences → Plugins** by searching for **Git Commit Template**. You can also install a locally built plugin ZIP from disk.

## Quick Start

1. Open **Settings / Preferences → Tools → Commit Template Idea Plugin** and configure commit rules or a template.
2. Open the Git **Commit** tool window and select files in **Included Changes**.
3. Use the template action, or configure an AI provider under the **AI Model** tab and click **AI Generate Commit Message**.
4. Review the generated message, edit it if necessary, then commit through IDEA as usual.

## Links

- [Project repository](https://github.com/muzilib/commit-template-idea-plugin)
- [Issues](https://github.com/muzilib/commit-template-idea-plugin/issues)
- [Original project](https://github.com/MobileTribe/commit-template-idea-plugin)

## License

Licensed under the Apache License, Version 2.0. See [LICENSE](LICENSE) for details.
