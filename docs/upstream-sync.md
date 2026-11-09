# Upstream-synchronisatie

TwinJay is een Thor-gerichte fork van Bluejay (FutoVideo), met Grayjay-plugins.

## Afspraken
- Controleer upstream eens per maand op: beveiligingsfixes, wijzigingen aan de plugin-API en Media3-updates.
- Neem alleen over wat de Thor-flavor raakt. UI-vernieuwingen van upstream niet blind overnemen: het tweede scherm
  en de Thor-instellingen hangen aan eigen bestanden (`CompanionContent`, `CompanionSleep`, `SwapScreens`).
- Doe elke sync in een eigen PR met een korte lijst "overgenomen / bewust overgeslagen".
- Laat CI (`build-thor.yml`) de unit-tests draaien voordat je merget, en doe daarna de Thor-testlijst
  (`docs/validation/thor-device-checklist.md`).

## Wat er niet mee moet
Package-id `com.tufein.bluejay.thor` en de signing-sleutels blijven ongewijzigd, anders kan de app niet meer updaten.
