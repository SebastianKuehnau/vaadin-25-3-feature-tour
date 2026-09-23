# Vaadin 25.3 Feature Tour

*A Vaadin DevRel demo app.*

A hands-on tour of [Vaadin 25.3](https://vaadin.com/blog/vaadin-25-3-release): every page is a live demo of one
release feature, with the Java code behind it and a link to the documentation.

Built with Vaadin Flow 25.3, Spring Boot 4.1 and the Aura theme.

## What's inside

| Area | Page | Feature | License |
|---|---|---|---|
| New components | Switch | On/off input with helper text, required state, small/reverse/icon variants | Free |
| | Table | Semantic HTML tables in Java, rows bound to a `ListSignal`, spanning header cells | Free |
| | Breadcrumbs | Trail built from the route hierarchy (see the app header), or defined manually | Free |
| Components | Date Picker | Disabled dates and weekdays, `DateMetadataProvider` with CSS parts, default time | Free |
| | Combo Box | `PartialMatchMode.FIRST_MATCH` / `ONLY_MATCH` | Free |
| | Message List | Bubble and one-to-one variants, `SELF` items, typing indicator (experimental) | Free |
| | Upload Validation | `UploadValidator` with metadata, header (magic bytes) and complete phases | Free |
| | Grid | No data for hidden columns, `GridI18n`, TreeGrid `selectAll()` with descendants | Free |
| | Accessibility | Keyboard Split Layout, `InputMode`, `HasAriaRole`, `HasAriaDescription`, heading levels | Free |
| Flow | Validation Groups | Draft vs. publish rules with `BeanValidationBinder` groups | Free |
| | Size Signal & whenAttached | `Element.sizeSignal()`, `Component.whenAttached()` | Free |
| | Service Event Bus | Live dashboard of session lock, RPC and data fetch events | Free |
| AI | AI Form Filler | `FormAIController` with source tracking, confidence and field markers | Commercial, preview |
| | AI Data Explorer | `GridAIController` and `ChartAIController` over a read-only database | Commercial, preview |
| | AI Assistant | Request interceptor guardrails, token usage, `ToolException` | Free, preview |
| Operations | Observability Kit 5 | Agent-less metrics, live `vaadin.*` meters | Commercial |
| | Under the Hood | TypeScript client engine, Dev Loop CLI, SSE push, Copilot, deprecations | – |

## Running the app

Requirements: Java 21 or later. Maven and Node.js are handled by the wrapper and the Vaadin plugin.

```bash
./mvnw
```

Then open http://localhost:8888. Set `PORT` to use a different port.

Commercial features (AI controllers, Charts, Observability Kit) ask for a Vaadin license in the browser the first
time you use them. A [free trial](https://vaadin.com/trial) works.

### AI demos

The AI pages talk to an LLM through Spring AI. OpenAI is the default:

```bash
export OPENAI_API_KEY=sk-...
./mvnw
```

To use Anthropic instead:

```bash
export AI_PROVIDER=anthropic
export ANTHROPIC_API_KEY=sk-ant-...
./mvnw
```

The models are set in `application.properties` (`gpt-5.4-mini`, `claude-sonnet-5`). Without a key, the AI pages
show a hint instead of the live demo. The AI features are a preview and are enabled in
`src/main/resources/vaadin-featureflags.properties`.

The data explorer runs the SQL written by the LLM as a read-only H2 user. The LLM sees the schema, never the rows.

## Tests

```bash
./mvnw test
```

Browserless tests cover the Switch, Table and Binder pages, the upload validator and the AI guardrails, and a smoke
test opens every page.

## Project structure

```
src/main/java/com/vaadin/devrel/featuretour
├── base/ui              MainLayout (side nav + breadcrumbs), ShowcasePage, Tier badges
└── showcase
    ├── home             Overview page
    ├── components       Switch, Table, Breadcrumbs, Date Picker, Combo Box, Message List, Upload, Grid, Accessibility
    ├── flow             Validation groups, size signal & whenAttached, service event bus
    ├── ai               Form filler, data explorer, assistant, LLM and database setup
    └── ops              Observability Kit 5, under the hood
src/main/resources/META-INF/resources
├── styles.css           App styles
└── vaadin-blue-inter.css  Aura theme configuration (Vaadin blue accent, Inter)
```

## Links

- [Release notes](https://github.com/vaadin/platform/releases/tag/25.3.0)
- [Release blog post](https://vaadin.com/blog/vaadin-25-3-release)
- [Vaadin documentation](https://vaadin.com/docs/latest)
