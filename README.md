# QA automation practical assignment

Small Java 17 test suite for [SauceDemo](https://www.saucedemo.com/) and
[Restful Booker](https://restful-booker.herokuapp.com/apidoc/index.html), using
Selenide, REST Assured, JUnit 5 and Maven.

## Prerequisites

**Local execution:** JDK 17+, Maven 3.9+, and an installed Chrome browser for UI tests.
API tests do not require a browser. The first UI run uses Selenium Manager to obtain
a compatible browser driver, requiring internet access and a writable cache.

**Docker execution:** only Docker Engine / Docker Desktop, running in Linux-container
mode. Java, Maven, Chromium and its matching driver are installed inside the image.
Both amd64 and arm64 are supported by the chosen base packages. Allow sufficient
memory for Chromium (4 GB for Docker is a reasonable starting point).

Both approaches require internet access to the public applications. Building and the
first local execution also require access to Maven Central and image/package registries.

Run commands below from the directory containing `pom.xml` and `Dockerfile`.

## Local execution

```bash
# All three tests
mvn test

# Two UI tests
mvn test -Dgroups=ui

# One API test
mvn test -Dgroups=api

# Show the browser locally (default: headless)
mvn test -Dgroups=ui -Dui.headless=false
```

JUnit tags select suites through Maven Surefire's `groups` option. Every test runs
independently; no test order is required. A failed test returns a nonzero exit code.
Use `mvn clean test` when a report directory containing only the latest run is wanted.

## Docker execution

```bash
docker build -t qa-automation-tests .
docker run --rm qa-automation-tests

# Select a suite; arguments are appended to the Maven test entrypoint
docker run --rm qa-automation-tests -Dgroups=ui
docker run --rm qa-automation-tests -Dgroups=api
```

The build downloads dependencies and compiles tests; it does not execute tests against
the public services. Each container run executes the tests. Chromium and ChromeDriver
come from the same Debian repository to keep their versions compatible. No manual
browser installation or separate Selenium container is needed.

To retain reports after the container exits (POSIX shell):

```bash
mkdir -p reports
docker run --rm -v "$(pwd)/reports:/tests/target" qa-automation-tests
```

Docker runs headless. The disposable container runs as root, so Chrome's sandbox is
disabled there; this setting is not enabled in normal local runs. The
`--disable-dev-shm-usage` browser option avoids dependence on Docker's small default
shared-memory allocation. Do not use this image as a general-purpose browser service.

## Configuration

Defaults are in `src/test/resources/test.properties`. Override them with JVM system
properties, for example:

```bash
mvn test -Dgroups=ui -Dui.timeoutMs=15000
docker run --rm qa-automation-tests -Dgroups=api -Dapi.timeoutMs=45000
```

| Property | Default | Purpose |
| --- | --- | --- |
| `ui.baseUrl` | `https://www.saucedemo.com` | UI origin, without trailing slash |
| `ui.username` | `standard_user` | Public demo login |
| `ui.password` | `secret_sauce` | Public demo password |
| `ui.headless` | `true` | Headless local browser |
| `ui.timeoutMs` | `10000` | Selenide condition timeout |
| `api.baseUrl` | `https://restful-booker.herokuapp.com` | API origin |
| `api.timeoutMs` | `30000` | HTTP connect and socket timeout |
| `api.username` | `admin` | Public demo account for cleanup |
| `api.password` | `password123` | Public demo password for cleanup |

These are published demo credentials, not production secrets. Avoid putting real
credentials in source control or command-line history. `CHROME_BIN` can select a
browser executable; the Dockerfile sets it to `/usr/bin/chromium`.

## Test approach

### UI

`SauceDemoTest` contains two business scenarios:

1. Log in, verify the inventory URL and Products title, and verify a nonempty catalog.
2. Log in, capture the catalog's prices, sort the expected values numerically with
   `BigDecimal`, apply ascending price sorting and wait for that exact price order.
   Add the first product, capture its name and price, then verify that the cart
   contains exactly that product, price and quantity one.

Page objects (`LoginPage`, `ProductsPage`, `CartPage`) encapsulate selectors and UI
operations. `Product` is a small immutable snapshot of the selected product.
`BaseUiTest` configures and closes the browser. Each test receives a fresh browser
session because the previous driver is closed in `@AfterEach`.

Selectors use SauceDemo's `data-test` attributes, including scoped add/remove buttons.
They avoid styling classes, positional XPath and hardcoded product names. Selecting
the first product is intentional scenario behavior; its identity is captured at
runtime rather than assumed.

Selenide conditions wait for visibility, URLs, titles, list size, the exact sorted
price sequence and the cart badge. There are no fixed sleeps or test retries.
Comparing the full sorted list also checks that sorting preserves the catalog's prices.

### API

`BookingClient` encapsulates HTTP configuration and create/get/delete requests without
mutating REST Assured's global defaults. The Accept header is explicitly
`application/json`: the service rejects the expanded media-type list produced by
REST Assured's `ContentType.JSON` Accept shortcut with HTTP 418. `BookingApiTest` owns assertions and test data.
Java records model the JSON, including nested dates; Jackson handles serialization.
Boxed number/boolean fields preserve missing values rather than silently using zero/false.

The test creates a booking with a unique last name and future dates, asserts HTTP 200,
JSON content type and a positive booking ID, and compares the POST echo to the request.
It then GETs the exact returned ID, asserts HTTP 200 and JSON content type, and compares
all fields. Record equality covers first name, last name, total price, deposit paid,
both dates and additional needs. GET does not return an ID field; identity is verified
through the POST ID and the GET path plus unique returned data.

`@AfterEach` deletes only the booking ID created by this test using the documented
Basic authentication. Restful Booker's DELETE example returns HTTP 201; cleanup checks
this status and failures are visible in the test result. Request/response validation
failures log details, with Authorization redacted; record mismatches show expected and
actual data through JUnit. The test does not depend on pre-existing bookings.

### Reports

- `target/surefire-reports/`: JUnit XML and text results.
- `target/selenide-reports/`: screenshots and page source on Selenide assertion failures.
- A bind mount is necessary to keep Docker reports when using `--rm`.

No Allure service is required. The built-in reports keep the exercise small.

## Layout

```text
pom.xml
Dockerfile
src/test/resources/test.properties
src/test/java/com/example/qa/
  config/TestConfig.java
  ui/BaseUiTest.java
  ui/SauceDemoTest.java
  ui/pages/LoginPage.java
  ui/pages/ProductsPage.java
  ui/pages/CartPage.java
  ui/pages/Product.java
  api/Booking.java
  api/BookingClient.java
  api/BookingApiTest.java
```

## Assumptions and limitations

- Both systems are public, shared demos; downtime, resets, rate limits or another
  user changing data can cause failures. No retries hide such failures.
- Cleanup needs the published demo admin credentials and a functioning DELETE API.
  If creation succeeds but its response is lost, the test cannot recover that ID for cleanup.
- Sorting assumes the current USD price format (`$29.99`) and at least two products.
- Tests run sequentially. Parallel execution and additional negative scenarios are
  deliberately outside this small assignment's scope.
- Maven dependencies and plugin versions are pinned. Debian packages and base-image
  tags can receive updates, so image rebuilds are not bit-for-bit reproducible.
  For release-level reproducibility, retain the built image by digest.
- SauceDemo and Restful Booker are unrelated applications, so there is no meaningful
  API-to-UI test-data integration between them.
