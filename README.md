# mycelium-web-template

A [deps-new](https://github.com/seancorfield/deps-new) template for creating [Mycelium](https://github.com/mycelium-clj/mycelium) web applications. The generated project follows the [Kit](https://kit-clj.github.io/) framework architecture, using [Integrant](https://github.com/weavejester/integrant) for component lifecycle management.

## Creating a New Project

```bash
clojure -Sdeps \
  '{:deps {io.github.seancorfield/deps-new
           {:git/sha "48bf01edfa6a959a970d7dcab3d882a93607a7f6"}
           io.github.mycelium-clj/mycelium-web-template
           {:git/url "https://github.com/mycelium-clj/mycelium-web-template"
            :git/sha "SHA"}}}' \
  -X org.corfield.new/create \
  :template '"io.github.mycelium-clj/web"' \
  :name '"yourname/yourapp"'
```

This creates a `yourapp/` directory with a ready-to-run web application.

For local development of the template itself, replace the git coordinate with `:local/root`:

```bash
clojure -Sdeps \
  '{:deps {io.github.seancorfield/deps-new
           {:git/sha "48bf01edfa6a959a970d7dcab3d882a93607a7f6"}
           io.github.mycelium-clj/mycelium-web-template
           {:local/root "."}}}' \
  -X org.corfield.new/create \
  :template '"io.github.mycelium-clj/web"' \
  :name '"yourname/yourapp"'
```

## What's in the Generated Project

### Stack

| Library | Role |
|---------|------|
| [Mycelium](https://github.com/mycelium-clj/mycelium) | Schema-enforced workflow engine for request handling |
| [Integrant](https://github.com/weavejester/integrant) | Component lifecycle and dependency injection |
| [Kit](https://kit-clj.github.io/) | kit-core (Aero config loading) + kit-jetty (HTTP server component) |
| [Reitit](https://github.com/metosin/reitit) | Data-driven HTTP routing |
| [Selmer](https://github.com/yogthos/Selmer) | Django-style HTML templating |
| [Ring](https://github.com/ring-clojure/ring) | HTTP abstraction + ring-defaults middleware |

### Project Structure

```
yourapp/
├── deps.edn
├── resources/
│   ├── system.edn                      # System configuration (Aero + Integrant)
│   ├── logback.xml                     # Logging configuration
│   └── html/
│       └── home.html                   # Selmer HTML template
├── src/clj/yourname/yourapp/
│   ├── core.clj                        # Application entry point
│   ├── config.clj                      # Config loading
│   ├── web/
│   │   ├── handler.clj                 # Ring handler (Integrant components)
│   │   ├── middleware/
│   │   │   └── core.clj                # Base middleware
│   │   └── routes/
│   │       └── pages.clj               # Page routes with mycelium workflows
│   ├── cells/
│   │   └── home.clj                    # Mycelium cell definitions
│   └── workflows/
│       └── home.clj                    # Mycelium workflow definitions
├── env/
│   ├── dev/clj/
│   │   ├── user.clj                    # REPL development helpers
│   │   └── yourname/yourapp/
│   │       ├── env.clj                 # Dev profile defaults
│   │       └── dev_middleware.clj       # Dev-only middleware
│   └── prod/clj/
│       └── yourname/yourapp/
│           └── env.clj                 # Prod profile defaults
└── test/clj/yourname/yourapp/
    └── core_test.clj
```

### Running the Application

Start the server:

```bash
clojure -M:dev:run
```

Visit [http://localhost:3000](http://localhost:3000). Try [http://localhost:3000/?name=YourName](http://localhost:3000/?name=YourName) to see the workflow in action.

### REPL-Driven Development

Start an nREPL and connect your editor:

```bash
clojure -M:dev -m nrepl.cmdline
```

Then in the REPL:

```clojure
(dev-prep!)                ;; prepare the system config
(go)                       ;; start all components
(reset)                    ;; reload code and restart
(halt)                     ;; stop the system
```

The dev profile recompiles the Reitit router on every request, so route changes take effect immediately after `(reset)`.

### Running Tests

```bash
clojure -M:test
```

## Architecture Overview

### System Configuration

The application is configured via `resources/system.edn` using [Aero](https://github.com/juxt/aero) reader tags for profile-based config and Integrant references for dependency wiring:

```edn
{:server/http
 {:port    #long #or [#env PORT 3000]
  :handler #ig/ref :handler/ring}

 :handler/ring
 {:router #ig/ref :router/core ...}

 :router/routes
 {:routes #ig/refset :reitit/routes}   ;; collects all route sets

 :reitit.routes/pages {}}              ;; page routes (mycelium workflows)
```

The component dependency chain: **server** -> **handler** -> **router** -> **routes**. Adding new route sets (e.g., `:reitit.routes/api`) is as simple as adding a new entry to `system.edn` and defining the corresponding `ig/init-key`.

### How Mycelium Integrates

Each HTTP request is handled by a **mycelium workflow** — a directed graph of **cells** (pure functions with schema contracts).

**Cells** are registered via `defmethod` and define their input/output schemas:

```clojure
(defmethod cell/cell-spec :request/parse-home [_]
  {:id      :request/parse-home
   :handler (fn [_resources data]
              (let [params (get-in data [:http-request :query-params])
                    name   (or (get params "name") "World")]
                (assoc data :name name)))
   :schema  {:input  [:map [:http-request :map]]
             :output [:map [:name :string]]}})
```

**Workflows** compose cells into pipelines:

```clojure
(def workflow-def
  {:cells    {:start  :request/parse-home
              :render :page/render-home}
   :pipeline [:start :render]})

(def compiled (myc/pre-compile workflow-def))
```

**Routes** wire compiled workflows to HTTP endpoints using `mycelium.middleware/workflow-handler`:

```clojure
(defn page-routes [_opts]
  [["/" {:get {:handler (mw/workflow-handler home/compiled {})}}]])
```

The workflow handler automatically passes the Ring request as `{:http-request req}` and extracts the `:html` key from the result as the response body.

### Adding a New Page

1. **Define cells** in `src/clj/.../cells/` — each cell receives `[resources data]` and returns an updated data map
2. **Define a workflow** in `src/clj/.../workflows/` — compose cells with `:pipeline` (linear) or `:edges` + `:dispatches` (branching)
3. **Add a route** in `src/clj/.../web/routes/pages.clj` — wire the compiled workflow to a path
4. **Add a template** in `resources/html/` — Selmer template rendered by the UI cell

### Adding Resources (Database, etc.)

Cells can declare resource dependencies via `:requires`. Pass resources through the workflow handler:

```clojure
;; In your cell:
(defmethod cell/cell-spec :todo/list [_]
  {:id       :todo/list
   :requires [:db]
   :handler  (fn [{:keys [db]} data]
               (assoc data :todos (query-todos db)))
   ...})

;; In your route (assuming :db is passed from system.edn):
(defmethod ig/init-key :reitit.routes/pages
  [_ {:keys [db]}]
  (fn []
    ["" [["/" {:get {:handler (mw/workflow-handler compiled {:resources {:db db}})}}]]]))
```

Then add `:db #ig/ref :your/db-component` to the `:reitit.routes/pages` entry in `system.edn`.

## Template Development

The template source lives under `resources/io/github/mycelium_clj/web/`:

| Directory | Target in generated project | Notes |
|-----------|---------------------------|-------|
| `root/` | Project root | Auto-copied (`.gitignore`, `README.md`) |
| `build/` | Project root | `deps.tmpl` -> `deps.edn` |
| `src-clj/` | `src/clj/{{ns}}/` | Application source |
| `test-clj/` | `test/clj/{{ns}}/` | Tests |
| `env-dev-user/` | `env/dev/clj/` | `user.clj` (REPL helpers) |
| `env-dev/` | `env/dev/clj/{{ns}}/` | Dev profile code |
| `env-prod/` | `env/prod/clj/{{ns}}/` | Prod profile code |
| `resources-app/` | `resources/` | Uses `<<>>` delimiters to preserve Selmer `{{}}` |

Template files use `{{top/ns}}`, `{{main/ns}}`, `{{top/file}}`, `{{main/file}}`, and `{{raw-name}}` for project name substitution.

## License

Copyright 2025 Mycelium Contributors. Distributed under the MIT License.
