# Tasks: Light Ad Integration

**Input**: `spec.md`, `plan.md`  
**Branch**: `feature/light-ad-integration`  
**Status**: Phase 1 & 2 COMPLETE — Phase 3 COMPLETE — Phase 4 COMPLETE — Phase 5 COMPLETE

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: AdMob SDK já existia; esta fase adiciona as dependências de gestão de estado e DI.

- [x] T001 Confirmar AdMob SDK inicializado em `MainApplication.kt`
- [x] T002 Confirmar `BillingManager.kt` expõe `noAdsPurchased` via `GameState` e `DataStore`
- [x] T003 [P] Confirmar `GameState.boostActiveUntil: Long?` já funcional no ViewModel

**Checkpoint**: ✅ Infraestrutura base validada — nenhuma dependência nova necessária.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core `AdManager` e extensão do `DataStore` — bloqueiam ambas as User Stories.

- [x] T004 Expandir `SettingsDataStore.kt` com `LAST_INTERSTITIAL_TIMESTAMP` e `LAST_REWARDED_TIMESTAMP`
  - Ficheiro: `app/src/main/java/com/example/viralclicker/data/datastore/SettingsDataStore.kt`
  - Adicionar keys `longPreferencesKey`, flows e setters correspondentes

- [x] T005 Criar `AdManager.kt` (Singleton `@Inject`) em `app/src/main/java/com/example/viralclicker/ads/`
  - Expor `StateFlow<RewardedAdState>` e `StateFlow<InterstitialAdState>`
  - Pre-caching automático com retry a cada 30s em caso de falha
  - `showRewardedAd(activity, onReward)` — callback seguro apenas em `onUserEarnedReward`
  - `showInterstitialIfReady(activity, comboCount)` — guard: noAds, cooldown 60min, comboCount == 0
  - Cooldown rewarded: 3 min via `SystemClock.elapsedRealtime()`
  - Cooldown interstitial: 60 min via `System.currentTimeMillis()` (wall-clock, persisten)

- [x] T006 Injetar `AdManager` no `GameViewModel.kt` via constructor DI
  - Expor `val rewardedAdState = adManager.rewardedAdState`
  - Expor `val interstitialAdState = adManager.interstitialAdState`

**Checkpoint**: ✅ Foundation ready — ambas as User Stories podem agora ser implementadas.

---

## Phase 3: User Story 1 — Rewarded Ad (Priority: P1) 🎯 MVP

**Goal**: Jogador clica botão opt-in → vê anúncio → recebe boost 2× por 30 min.

**Independent Test**: Shop → clicar "Watch Ad" → vídeo AdMob test → boost ativo 30 min no StatsBar.

### Implementação US1

- [x] T007 [US1] Refatorizar `onBoostRewarded()` em `GameViewModel.kt` para **estender** boost em vez de reiniciar
  - `newEnd = max(now, currentEnd) + 30min`

- [x] T008 [P] [US1] Adicionar `fun showRewardedAd(activity: Activity)` ao `GameViewModel`
  - Delega para `adManager.showRewardedAd(activity) { onBoostRewarded() }`

- [x] T009 [P] [US1] Criar `RewardedAdCard` composable em `UpgradeShop.kt`
  - Primeiro item da `LazyColumn` na Shop
  - Estados visuais: Ready (glow + WATCH button), Loading (spinner), Cooldown (countdown mm:ss), NotAvailable (dimmed "—")
  - Pulsing scale animation quando `Ready`

- [x] T010 [US1] Aceitar `rewardedAdState` e `onWatchAd` como params de `UpgradeShop()`
  - Defaults: `RewardedAdState.NotAvailable` e `{}`

- [x] T011 [US1] Ligar botão placeholder no `GameTab` (GameScreen.kt) ao `viewModel.showRewardedAd(activity)`
  - Substituir `onClick = { /* rewarded ad — wired to BillingManager in Phase 4 */ }` pelo call real

- [x] T012 [US1] Passar `rewardedAdState` e `onWatchAd` ao `UpgradeShop` em `GameScreen.kt`

**Checkpoint**: ✅ US1 completa — Rewarded Ad funcional em GameTab e UpgradeShop.

---

## Phase 4: User Story 2 — Interstitial Periódico (Priority: P2)

**Goal**: Interstitial aparece no máximo 1×/hora em momentos de transição naturais.

**Independent Test**: Reduzir cooldown a 60s → swipe de tab → interstitial aparece → confirmar não repete nos próximos 59s.

### Implementação US2

- [x] T013 [US2] Adicionar `fun tryShowInterstitial(activity: Activity)` ao `GameViewModel`
  - Delega para `adManager.showInterstitialIfReady(activity, _comboCount)`

- [x] T014 [US2] Ligar `tryShowInterstitial()` ao `LaunchedEffect(pagerState.currentPage)` em `GameScreen.kt`
  - Guard: só dispara se `currentPage != 0` (skip na abertura inicial)

**Checkpoint**: ✅ US2 completa — Interstitial dispara em swipe de tab com todos os guards ativos.

---

## Phase 5: Polish & Cross-Cutting Concerns

- [x] T015 [P] Adicionar `Timber.e()` nos callbacks de erro do `AdManager` (substituir TODO/silêncio)
  - `onAdFailedToLoad`, `onAdFailedToShowFullScreenContent`

- [x] T016 Verificar implementação UMP/Consent em `MainApplication.kt` para conformidade GDPR
  - Se ausente: adicionar `ConsentInformation.requestConsentInfoUpdate()` antes de carregar ads
  - Ver spec.md `[NEEDS CLARIFICATION]` — confirmar com equipa se mercado UE é alvo

- [x] T017 [P] Substituir test ad unit IDs por `BuildConfig` fields para produção
  - Criar `REWARDED_AD_UNIT_ID` e `INTERSTITIAL_AD_UNIT_ID` em `build.gradle` por flavor/buildType
  - Atualizar `AdManager.kt` para ler de `BuildConfig`

- [x] T018 Verificar manualmente todos os cenários de aceitação em dispositivo físico ou emulador Google Play
  - US1: Boost acumula corretamente (ver spec SC-001, SC-002)
  - US2: Cooldown de 60 min respeitado; reboot da app não reseta (ver spec SC-003)
  - No-Ads: Interstitial desativa; rewarded mantém (ver spec SC-004)
  - Offline: Sem crash; botão dimmed (ver spec SC-005)

---

## Dependency Order

```
T001-T003 (Setup)
    ↓
T004-T006 (Foundation — DataStore + AdManager + ViewModel inject)
    ↓
T007-T012 (US1 — Rewarded Ad, can parallel T009 & T008)
    ↓
T013-T014 (US2 — Interstitial trigger)
    ↓
T015-T018 (Polish)
```

## Notes

- `[P]` = can run in parallel (ficheiros diferentes, sem dependências)
- `[US1]` / `[US2]` = rastreabilidade à user story
- Tests AdMob SDK são necessariamente **manuais** em device (sem mock possível para FullScreen callbacks)
- Antes de release: T016 (GDPR) e T017 (real ad unit IDs) são **obrigatórios**
