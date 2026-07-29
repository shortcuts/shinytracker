# Contributing

## Before Opening a PR

1. Read [AGENTS.md](AGENTS.md) — code standards, modules, arch rules, agent tooling.
2. One feature/fix per PR.
3. New feature: open issue first.

## Required Checks

All 3 must pass:

```bash
make format
make lint
make test
```

Fix all lint **error**s before done. Warnings ok; errors not. Never suppress `Errors` rules. Never `@file:Suppress`.

## Reference Docs

| Topic | File |
|-------|------|
| Architecture & modules | [docs/architecture.md](docs/architecture.md) |
| Code style rules | [docs/code-style.md](docs/code-style.md) |
| Testing strategy | [docs/testing.md](docs/testing.md) |
| Constants | [docs/constants.md](docs/constants.md) |
| Permissions | [docs/permissions.md](docs/permissions.md) |
