# Cartwise

An offline-first shopping app for Android, built with Kotlin and Jetpack Compose.
Browse a paged product catalog that keeps working without a network, and build a cart
priced by a tested promotions engine (multi-buy and order-level offers).

Product data comes from the public [DummyJSON](https://dummyjson.com/docs/products) API.

## Features

- **Offline-first catalog:** Paging 3 + `RemoteMediator`. The UI only reads Room; the network
  just fills Room. Cached pages show instantly on launch and stay usable offline.
- **Real connectivity state:** an offline banner driven by `NET_CAPABILITY_VALIDATED`, not just
  "has a network", so captive-portal Wi-Fi counts as offline.
- **Promotions engine:** buy 2 get 1 free, second item half price, and tiered order discounts,
  with a "spend $X more" nudge. Pure Kotlin, money in cents, every rule pinned by a unit test.
- **Persistent cart:** stored in Room, priced reactively as a `Flow`, and prices re-synced
  whenever the catalog refreshes.
- **Background sync:** WorkManager refreshes catalog prices every 12 h on unmetered networks.
- **Small release build:** R8 full mode + resource shrinking. Release APK is about 1.7 MB.

## Architecture

```
app/                 Compose UI, navigation, ViewModels (feature packages: catalog, detail, cart)
core/data/           Repositories, Room, Retrofit, RemoteMediator, WorkManager, Hilt modules
core/pricing/        Pure Kotlin (no Android): Money, Offer, PricingEngine + tests
```

- **MVVM + unidirectional data flow:** each screen exposes one `StateFlow<UiState>` (a sealed
  interface), collected with `collectAsStateWithLifecycle`.
- **Repositories behind interfaces** (`ProductRepository`, `CartRepository`, `OfferRepository`),
  bound with Hilt `@Binds`. Moving offers to a remote config changes one class.
- **`core:pricing` has no Android dependency**, so its tests run on the plain JVM in
  milliseconds.

### Pricing rules (see `PricingEngineTest`)

1. Multi-buy offers run in order. A unit used by one offer is never reused by another.
2. Eligible units are sorted most expensive first and grouped into (buy + get). The cheapest
   `get` units of each complete group are discounted.
3. The single best order-level offer applies to the total after item discounts. Order offers
   never stack.
4. Percent discounts round half-up to the cent.

## Tech stack

Kotlin · Jetpack Compose · Material 3 · Navigation (type-safe routes) · Hilt · Coroutines/Flow ·
Room · Paging 3 · WorkManager · Retrofit + kotlinx.serialization · OkHttp · Coil · JUnit ·
kotlinx-coroutines-test · GitHub Actions

## Build

Requires JDK 17+ and Android SDK 36.

```bash
./gradlew test             # unit tests
./gradlew installDebug     # run on a device or emulator
./gradlew assembleRelease  # minified build (signed with the debug key)
```

## Roadmap

- [ ] Search and category filters
- [ ] Baseline Profile + Macrobenchmark with measured cold-start numbers
- [ ] Room DAO and Compose UI tests
- [ ] UPI intent hand-off demo at checkout
