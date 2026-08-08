# 0.3.0

Released: 2026-08-08

The model becomes a configuration setting.

## Upgrading

Nothing is required. A `config.yml` written by an earlier version has no `model`
key, and a missing key falls back to the platform's default — which is what the
previous version used anyway.

To change it, add:

```yaml
model: "~deepseek/deepseek-v4-flash-latest"
```

## Added

- **`model` in `config.yml`.** The default is
  `~deepseek/deepseek-v4-flash-latest`, an OpenRouter slug that always redirects
  to the newest DeepSeek V4 Flash release, so it follows the family without an
  edit. Leave the key blank to use the platform's default.
- **A mismatch warning.** OpenRouter namespaces every model
  (`~deepseek/deepseek-v4-flash-latest`) while OpenAI, DeepSeek, and Anthropic
  take bare names (`gpt-4o-mini`). Configuring one shape against the other is
  rejected by the vendor in a way that reads exactly like a bad API key, so the
  plugin says so at startup with both values and the fix. It is a warning, never
  a refusal — a slug this code has not heard of still works.

## Changed

- `ChatSettings.model` is a real setting rather than a value derived from the
  platform.
- `Models` is now the table of per-platform **defaults** rather than the fixed
  answer. OpenRouter's default changed from `openai/gpt-4o-mini` to
  `~deepseek/deepseek-v4-flash-latest`; the other three are unchanged.

## Known gaps

- The warning catches a shape mismatch, not a typo. A valid-shaped slug that does
  not exist still fails at request time looking like an auth error; only a live
  catalog lookup would catch that, and it is not worth a startup network call.
- `platforms/neoforge` and `platforms/fabric` still carry no loader code.
