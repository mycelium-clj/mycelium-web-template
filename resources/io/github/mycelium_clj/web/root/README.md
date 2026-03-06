# {{raw-name}}

{{description}}

## Getting Started

Run the application:

    clojure -M:run

Or from the REPL:

```clojure
(dev-prep!)
(go)         ;; start system
(reset)      ;; reload code + restart
(halt)       ;; stop system
```

Visit [http://localhost:3000](http://localhost:3000).

## Running Tests

    clojure -M:test

## Project Structure

```
src/clj/         — Application source code
  core.clj         — App lifecycle (Integrant)
  config.clj       — Configuration loading
  web/
    handler.clj      — Ring handler setup
    middleware/
      core.clj         — Base middleware
    routes/
      pages.clj        — Page routes (mycelium workflows)
  cells/             — Mycelium cell definitions
  workflows/         — Mycelium workflow definitions
env/
  dev/clj/           — Development profile
  prod/clj/          — Production profile
resources/
  system.edn         — System configuration (Aero + Integrant)
  html/              — Selmer HTML templates
test/clj/            — Tests
```

## Learn More

- [Mycelium](https://github.com/mycelium-clj/mycelium) — Schema-enforced workflow framework
- [Kit](https://kit-clj.github.io/) — Modular web framework for Clojure
- [Integrant](https://github.com/weavejester/integrant) — Dependency injection
- [Reitit](https://github.com/metosin/reitit) — Data-driven routing
- [Selmer](https://github.com/yogthos/Selmer) — Django-style templating
