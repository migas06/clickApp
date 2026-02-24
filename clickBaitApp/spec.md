# Feature Specification: Light Ad Integration

**Feature Branch**: `feature/light-ad-integration`  
**Created**: 2026-02-24  
**Status**: Draft  
**Input**: "quero uma nova feature no projecto que é relacionado com ads, não quero que seja muito intrusivo, talvez lançar de hora a hora uma ad e um botão na store para ganhar recompensas por ver ads"

---

## Existing Infrastructure (Context)

O projecto já tem:
- **Banner Ad** (`AdBanner.kt`) — rodapé fixo com `ca-app-pub-3940256099942544/6300978111` (test ID)
- **IAP "No Ads"** (`BillingManager.kt`) — SKU `viral_no_ads`, flag `noAdsPurchased` em `GameState`, `DataStore` e `PlayerEntity`
- **Boost 2×** (`GameState.boostActiveUntil`) — multiplicador já funcional, ativável via `boostActiveUntil: Long?`
- **Placeholder no GameScreen** — botão `"📺 Watch Ad for 2× Boost"` com `onClick = { /* rewarded ad — wired to BillingManager in Phase 4 */ }`
- **AdMob SDK** — inicializado em `MainApplication.kt` (deferred)

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Rewarded Ad na Shop + Game Tab (Priority: P1)

Como jogador, quero clicar num botão para ver voluntariamente um anúncio em vídeo e receber um **boost 2× durante 30 minutos** como recompensa. Este botão já existe como placeholder no `GameScreen` e será duplicado na `UpgradeShop`.

**Why this priority**: É "opt-in" (o utilizador decide quando ver), tem alto valor percebido (boost 2×), e já existe infraestrutura parcial (`boostActiveUntil`, botão placeholder).

**Independent Test**: Abrir a app → navegar à Shop → clicar "Watch Ad for 2× Boost" → ver o vídeo teste do AdMob → verificar que o boost 2× fica ativo durante 30 minutos (ícone de boost visível no `StatsBar`/`BoostTimer`).

**Acceptance Scenarios**:

1. **Given** o jogador não tem boost ativo, **When** clica "Watch Ad" e completa o vídeo, **Then** `boostActiveUntil` é definido para `now + 30min`, o `BoostTimer` aparece, e todos os ganhos (click + passivo) ficam 2×.
2. **Given** o jogador já tem boost ativo, **When** vê outro anúncio, **Then** o boost é **estendido** em +30min a partir do `max(now, boostActiveUntil)` em vez de reiniciar.
3. **Given** o jogador completa o vídeo mas o callback do SDK falha (erro de rede depois do vídeo), **When** o SDK não confirma a recompensa, **Then** o boost NÃO é dado e aparece um toast/snackbar de erro.
4. **Given** o utilizador comprou "No Ads" (IAP), **When** abre a shop, **Then** o botão de Rewarded Ad **continua visível** (é opt-in, não intrusivo) mas o texto pode mudar para reforçar que é opcional.

---

### User Story 2 — Interstitial Ad Periódico (Priority: P2)

Como jogador, aceito ver um anúncio de ecrã inteiro de vez em quando (máximo 1× por hora), que aparece num **momento natural de transição** e não a meio de um combo ou ação.

**Why this priority**: Monetização passiva que complementa a receita sem "opt-in". Menos prioritário porque afeta diretamente a experiência (interrompe o fluxo de jogo) e é desnecessário se a abordagem de rewarded ads gerar receita suficiente.

**Independent Test**: Configurar temporariamente o cooldown para 60 seg → jogar normalmente → ao trocar de tab após os 60 seg, o interstitial aparece → verificar que o timer reinicia e não aparece outro antes do cooldown expirar.

**Acceptance Scenarios**:

1. **Given** o jogador joga há mais de 60 min desde o último interstitial (ou desde a instalação), **When** o jogador troca de tab (pager swipe) ou volta do background, **Then** um interstitial é apresentado **antes** da transição de ecrã.
2. **Given** o interstitial acabou de ser mostrado, **When** o jogador continua a jogar durante os próximos 59 min, **Then** nenhum interstitial é apresentado independentemente das ações do utilizador.
3. **Given** o utilizador comprou "No Ads" (IAP), **When** o cooldown expira, **Then** nenhum interstitial é apresentado — a funcionalidade é completamente desativada.
4. **Given** o utilizador fecha e reabre a app, **When** o cooldown persistido ainda não expirou, **Then** nenhum interstitial é apresentado ao reabrir.
5. **Given** o jogador está num **combo ativo** (`comboCount > 0`), **When** o cooldown do interstitial expira, **Then** o sistema **espera** até o combo acabar antes de mostrar o anúncio (para não destruir a experiência de jogo).

---

### Edge Cases

#### Rede e Falhas do SDK
- **Sem internet ao carregar Rewarded Ad**: O botão de rewarded ad fica **desativado** (visualmente dimmed) com texto "Ad not available". O sistema tenta pre-carregar novamente a cada 30 seg automaticamente.
- **Sem internet ao carregar Interstitial**: O timer do cooldown **não reinicia**. O sistema tenta mostrar o interstitial na próxima oportunidade quando o ad estiver carregado.
- **SDK timeout ou crash**: Logs do erro via `Timber`; a app NÃO crasha. O jogo volta ao estado normal silenciosamente.

#### Estado da App
- **App minimizada durante Rewarded Ad**: Se o utilizador vai para outra app durante o vídeo, o SDK do AdMob trata o callback (`onAdDismissedFullScreenContent`). Sem recompensa se não completou.
- **App minimizada e volta depois de horas**: O interstitial cooldown é baseado em **relógio real** (`System.currentTimeMillis()`), não em tempo de jogo. Garantir que ao voltar, se o cooldown passou, o ad é apresentado no próximo ponto de transição.
- **Rotação de ecrã durante Ad**: O ad é apresentado via `Activity`, que sobrevive a configuração changes. Nenhuma ação especial necessária.

#### Anti-Abuse / Anti-Exploit
- **Rewarded Ad spam**: Implementar um **cooldown de 3 minutos** entre rewarded ads para evitar que o utilizador veja 20 anúncios seguidos. UI mostra countdown até poder ver o próximo.
- **Clock manipulation (avanço do relógio do dispositivo)**: Guardar timestamps em `SystemClock.elapsedRealtime()` para o cooldown do rewarded, e `System.currentTimeMillis()` para o interstitial (que só é validado quando o ad é efetivamente carregado pelo SDK, que depende de rede).

#### Compra "No Ads"
- **Banner**: Já escondido quando `noAdsPurchased == true` (existente).
- **Interstitial**: Completamente desativado.
- **Rewarded Ad**: **Permanece ativo** — é opt-in e o jogador pode querer o boost mesmo tendo comprado "No Ads".
- **Compra "No Ads" enquanto interstitial cooldown está a decorrer**: O interstitial é imediatamente cancelado/nunca mostrado; qualquer ad pré-carregado é descartado.

#### Consent / GDPR
- O AdMob SDK gere o UMP (User Messaging Platform) para consent na UE. A inicialização do consent **já deveria existir** em `MainApplication`.
- [NEEDS CLARIFICATION: A aplicação já implementa o `ConsentInformation` e `ConsentForm` do UMP? Se não, isto é um requisito adicional antes de qualquer ad ser servido na UE.]

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: O sistema MUST implementar `RewardedAd.load()` com pre-caching automático.
- **FR-002**: O sistema MUST expor um composable `RewardedAdButton` reutilizável (usado no `GameScreen` e `UpgradeShop`).
- **FR-003**: O sistema MUST ativar o boost 2× por 30 minutos ao callback `onUserEarnedReward`.
- **FR-004**: O sistema MUST estender o boost em vez de reiniciar se já houver um boost ativo.
- **FR-005**: O sistema MUST implementar `InterstitialAd.load()` com pre-caching automático.
- **FR-006**: O sistema MUST apresentar o interstitial no máximo 1× por hora (60 min), baseado em relógio real.
- **FR-007**: O sistema MUST persistir o `lastInterstitialTimestamp` no `SettingsDataStore`.
- **FR-008**: O sistema MUST respeitar o flag `noAdsPurchased` — desativar interstitial; manter rewarded.
- **FR-009**: O sistema MUST ter um cooldown de 3 minutos entre rewarded ads.
- **FR-010**: O sistema MUST não apresentar interstitial durante um combo ativo.
- **FR-011**: O sistema MUST usar test ad unit IDs durante o desenvolvimento e apenas mudar para IDs reais via BuildConfig antes de release.

### Key Entities

- **AdManager** (Singleton, `@Inject`): Gere o ciclo de vida dos `RewardedAd` e `InterstitialAd`. Expõe `StateFlow<AdState>` para a UI saber se o ad está pronto, carregando, ou falhou.
- **AdState**: Sealed class (`Loading`, `Ready`, `Showing`, `NotAvailable`, `Cooldown(remainingMs)`)
- **SettingsDataStore**: Expandido com `lastInterstitialTimestamp: Long` e `lastRewardedTimestamp: Long`.

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: O botão de Rewarded Ad aparece e funciona tanto na `GameScreen` como no `UpgradeShop` com test ad units.
- **SC-002**: Após completar um rewarded ad, o boost 2× fica ativo durante exatamente 30 minutos (verificável pelo `BoostTimer` e pelos cálculos de pontos).
- **SC-003**: O interstitial nunca aparece com menos de 60 minutos de intervalo.
- **SC-004**: Com `noAdsPurchased == true`, nenhum interstitial é mostrado; rewarded continua disponível.
- **SC-005**: Sem internet, a app não crasha — o botão de rewarded fica inativo e o interstitial falha silenciosamente.
- **SC-006**: Nenhuma violação das políticas do Google Play relativas a anúncios (sem interstitials inesperados, sem bloquear conteúdo).
