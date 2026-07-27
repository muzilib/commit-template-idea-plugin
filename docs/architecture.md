# Commit Template Plugin Architecture

## Product boundary

The plugin helps users compose and review Git commit messages in IntelliJ's Commit tool window. It writes text to the active `CommitMessageI`; it never performs `git commit` itself.

AI generation is an opt-in future capability. Generated messages must be previewed and explicitly applied by the user.

## Layering target

```text
ui/              Swing and IntelliJ Settings views
config/          Persistent settings and effective-settings resolution
domain/          Pure validation, parsing, formatting and AI result models
application/     Use cases that coordinate domain logic
platform/        IntelliJ VCS, notifications, background tasks and Git adapters
infrastructure/  HTTP, Password Safe, provider implementations and prompt rendering
```

Only `platform` may depend directly on IntelliJ VCS/Git APIs. Only `infrastructure` may know provider-specific HTTP protocols. Domain code must remain testable without an IntelliJ runtime.

## Configuration model

Configuration is resolved in this order:

```text
Project override > global default > built-in default
```

### Global default

`StoreCommitTemplateState` remains the global state and deliberately keeps its existing storage name:

```text
$APP_CONFIG$/StoreCommitTemplateState-settings.xml
```

This preserves existing user configuration. It owns global defaults and user/machine UI preferences such as window geometry.

### Project override

`ProjectCommitTemplateOverrideState` is stored at:

```text
.idea/commit-template.xml
```

Every nullable field means "inherit global default". It may contain commit rules and display preferences, but **must never contain API keys, passwords, tokens, cookies, or other credentials**.

The effective settings are obtained only through `CommitTemplateSettingsResolver` when a `Project` is available.

## AI provider direction

AI use cases will depend on an `AiProvider` abstraction rather than an SDK or model vendor. The first provider implementations should be:

1. `OpenAiCompatibleProvider` for OpenAI, Qwen, DeepSeek and compatible gateways.
2. `OllamaProvider` for local models.

Credentials will be stored exclusively with IntelliJ Password Safe. Team/project configuration can select a provider alias, prompt template and generation rules but cannot contain secrets.

## AI safety contract

Before any remote request:

- collect only currently included commit changes;
- filter binary, secret-like and excluded files;
- enforce per-file and total size limits;
- show the destination provider/model and a content summary;
- require explicit consent for remote Diff transfer;
- support cancellation and never log authorization headers or complete Diffs.

AI responses should use a validated structured format. The plugin, not the model, formats the final Conventional Commit message.
