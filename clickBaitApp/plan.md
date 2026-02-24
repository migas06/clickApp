# Implementation Plan: Light Ad Integration

**Branch**: `feature/light-ad-integration` | **Date**: 2026-02-24 | **Spec**: [spec.md](file:///Users/miguelmartins/Documents/clickbaitapp/clickBaitApp/spec.md)

## Summary

Implementar monetização de ads com baixa intrusão: **Rewarded Ads** (opt-in, boost 2× por 30 min) e **Interstitial Ads** (max 1× por hora, em momentos de transição). O projecto já tem: `AdBanner.kt`, `BillingManager.kt` (IAP), `boostActiveUntil` no GameState, e `onBoostRewarded()` no ViewModel.

## Technical Context

**Language/Version**: Kotlin 1.9+, Jetpack Compose  
**Primary Dependencies**: AdMob SDK (google-services), Hilt, Room, DataStore  
**Storage**: DataStore (timestamps), Room (player entity)  
**Testing**: JUnit (unit), Manual (ad SDK — cannot unit-test real ad callbacks)  
**Target Platform**: Android 8+ (API 26+)  
**Project Type**: Mobile app (Android)

---

## Proposed Changes

### Component 1: Ad Infrastructure

#### [NEW] [AdManager.kt](file:///Users/miguelmartins/Documents/clickbaitapp/clickBaitApp/app/src/main/java/com/example/viralclicker/ads/AdManager.kt)

Singleton (`@Singleton`, `@Inject`) que gere o ciclo de vida dos `RewardedAd` e `InterstitialAd`:

```kotlin
@Singleton
class AdManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
)
```

**Responsabilidades:**
- Pre-carregar `RewardedAd` e `InterstitialAd` no `init`
- Expor `StateFlow<RewardedAdState>` (`Loading`, `Ready`, `Showing`, `NotAvailable`, `Cooldown(remainingMs)`)
- Expor `StateFlow<InterstitialAdState>` (`Loading`, `Ready`, `NotAvailable`, `Disabled`)
- `showRewardedAd(activity: Activity, onReward: () -> Unit)` — mostra rewarded + callback
- `showInterstitialIfReady(activity: Activity)` — mostra interstitial se cooldown expirou
- `shouldShowInterstitial(comboCount: Int): Boolean` — verifica cooldown + combo + noAds
- Retry automático a cada 30s se o load falhar
- Cooldown de **3 min** entre rewarded ads
- Cooldown de **60 min** entre interstitial ads
- Test ad unit IDs via `BuildConfig`

---

#### [MODIFY] [SettingsDataStore.kt](file:///Users/miguelmartins/Documents/clickbaitapp/clickBaitApp/app/src/main/java/com/example/viralclicker/data/datastore/SettingsDataStore.kt)

Adicionar duas novas preferências:

```diff
+val LAST_INTERSTITIAL_TIMESTAMP = longPreferencesKey("last_interstitial_timestamp")
+val LAST_REWARDED_TIMESTAMP = longPreferencesKey("last_rewarded_timestamp")
+
+val lastInterstitialTimestamp: Flow<Long>
+val lastRewardedTimestamp: Flow<Long>
+
+suspend fun setLastInterstitialTimestamp(value: Long)
+suspend fun setLastRewardedTimestamp(value: Long)
```

---

#### [MODIFY] [AppModule.kt](file:///Users/miguelmartins/Documents/clickbaitapp/clickBaitApp/app/src/main/java/com/example/viralclicker/di/AppModule.kt)

O `AdManager` é `@Singleton` com `@Inject constructor`, logo Hilt resolve automaticamente. **Nenhuma alteração necessária** se usarmos `@Inject` no constructor do `AdManager`.

---

### Component 2: Rewarded Ad UI

#### [MODIFY] [GameViewModel.kt](file:///Users/miguelmartins/Documents/clickbaitapp/clickBaitApp/app/src/main/java/com/example/viralclicker/ui/game/GameViewModel.kt)

- Injetar `AdManager` no constructor
- Expor `val rewardedAdState: StateFlow<RewardedAdState>` do AdManager
- Expor `val interstitialAdState: StateFlow<InterstitialAdState>` do AdManager
- Modificar `onBoostRewarded()` para **estender** o boost em vez de reiniciar:

```diff
 fun onBoostRewarded() {
     viewModelScope.launch {
         val boostDurationMs = 30 * 60 * 1000L
-        _player = _player.copy(boostActiveUntil = System.currentTimeMillis() + boostDurationMs)
+        val now = System.currentTimeMillis()
+        val currentEnd = _player.boostActiveUntil ?: 0L
+        val newEnd = maxOf(now, currentEnd) + boostDurationMs
+        _player = _player.copy(boostActiveUntil = newEnd)
         repository.savePlayer(_player)
         emitState()
     }
 }
```

- Adicionar `fun showRewardedAd(activity: Activity)` que chama `adManager.showRewardedAd(activity) { onBoostRewarded() }`
- Adicionar `fun tryShowInterstitial(activity: Activity)` que verifica combo e chama `adManager.showInterstitialIfReady(activity)`

---

#### [MODIFY] [GameScreen.kt](file:///Users/miguelmartins/Documents/clickbaitapp/clickBaitApp/app/src/main/java/com/example/viralclicker/ui/game/GameScreen.kt)

- Ligar o botão placeholder `"📺 Watch Ad for 2× Boost"` ao `viewModel.showRewardedAd(activity)`:
  - Obter `Activity` via `LocalContext.current as Activity`
  - Observar `rewardedAdState` para disabled/enabled/cooldown do botão
- Chamar `viewModel.tryShowInterstitial(activity)` quando o pager muda de tab (se cooldown expirado e combo = 0)

---

#### [MODIFY] [UpgradeShop.kt](file:///Users/miguelmartins/Documents/clickbaitapp/clickBaitApp/app/src/main/java/com/example/viralclicker/ui/game/components/UpgradeShop.kt)

Adicionar um cartão/botão estilizado "Watch Ad for 2× Boost" no topo da lista (antes dos upgrades):

- Aceitar `rewardedAdState: RewardedAdState` e `onWatchAd: () -> Unit` como parâmetros
- Mostrar estado do botão baseado em `RewardedAdState`:
  - `Ready` → Botão ativo com glow
  - `Loading` → Botão com loading spinner
  - `Cooldown(remainingMs)` → Botão com countdown
  - `NotAvailable` → "Ad not available" dimmed

---

### Component 3: Interstitial Ad Logic

Integrado no `AdManager` + `GameViewModel`. O trigger é no page swipe do `HorizontalPager` em `GameScreen.kt`.

Condições para mostrar:
1. `noAdsPurchased == false`
2. `lastInterstitialTimestamp + 60min < now`
3. `comboCount == 0` (combo não ativo)
4. `InterstitialAd` está carregado (`Ready`)

---

## Verification Plan

### Automated Tests

O AdMob SDK não pode ser testado com unit tests reais (requer dispositivo + rede). Contudo:

**Teste unitário existente:**
```bash
./gradlew test --tests "com.example.viralclicker.ViralPointsFormatterTest"
```
*(Verifica que não quebramos nada existente)*

**Build completo:**
```bash
./gradlew compileDebugKotlin
```
*(Verifica que todas as novas classes e injeções compilam sem erros)*

### Manual Verification

> [!IMPORTANT]
> A verificação dos anúncios requer execução num **dispositivo Android real ou emulador** com Google Play Services. O AdMob SDK usa **test ad unit IDs** durante o desenvolvimento.

**Teste 1 — Rewarded Ad (Shop):**
1. Abrir a app no dispositivo/emulador
2. Navegar para a tab **SHOP**
3. Clicar no botão "Watch Ad for 2× Boost"
4. Completar o vídeo de teste do AdMob
5. ✅ Verificar que o `BoostTimer` aparece no `GameScreen` com ~30 min
6. ✅ Verificar que os ganhos por click/segundo duplicaram

**Teste 2 — Rewarded Ad Extension:**
1. Com boost já ativo (~29 min restantes), ver outro rewarded ad
2. ✅ Verificar que o boost estendeu para ~59 min (não reiniciou para 30 min)

**Teste 3 — Rewarded Ad Cooldown:**
1. Após ver um rewarded ad, voltar à shop
2. ✅ Verificar que o botão mostra countdown de 3 min antes de poder ver outro

**Teste 4 — Interstitial (com cooldown reduzido a 1 min para teste):**
1. Jogar normalmente durante > 1 min
2. Trocar de tab (swipe no pager)
3. ✅ Verificar que o interstitial aparece
4. Trocar de tab imediatamente depois
5. ✅ Verificar que NÃO aparece outro interstitial

**Teste 5 — No Ads IAP:**
1. Simular `noAdsPurchased = true` (ou comprar "No Ads" in-app)
2. ✅ Verificar que interstitials NUNCA aparecem
3. ✅ Verificar que o botão de rewarded ad CONTINUA ativo na shop

**Teste 6 — Sem Internet:**
1. Desligar Wi-Fi/dados
2. ✅ Verificar que o botão de rewarded ad fica disabled ("Ad not available")
3. ✅ Verificar que a app não crasha

---

## File Summary

| Action | File | Description |
|--------|------|-------------|
| **NEW** | `ads/AdManager.kt` | Singleton — load, cache, show Rewarded + Interstitial |
| **MODIFY** | `data/datastore/SettingsDataStore.kt` | +2 timestamp preferences |
| **MODIFY** | `ui/game/GameViewModel.kt` | Inject AdManager, expose ad states, boost extension |
| **MODIFY** | `ui/game/GameScreen.kt` | Wire rewarded ad button, interstitial on page change |
| **MODIFY** | `ui/game/components/UpgradeShop.kt` | Add rewarded ad card at top of shop |
