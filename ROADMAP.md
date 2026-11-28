# TwinJay roadmap, ronde 7

Opgesteld op 2026-11-28, na release 0.21.0-thor.1. De vorige roadmap (21 punten, 0.20.0) is grotendeels
gebouwd; wat daarvan nog openstond staat hieronder opnieuw. Dit is een voorstel, geen belofte.
Formaat: S = enkele uren, M = een dag, L = meerdere dagen. Alles is alleen door CI getest, niet op de Thor.

## Gebouwd in 0.23.0

| # | Item | Wat het doet |
|---|------|--------------|
| 1 | Wachtrij blijft bewaard na herstart | De wachtrij wordt opgeslagen (`QueueStore`) en bij het starten teruggezet. Er start niets vanzelf. |
| 2 | Wachtrij husselen | Opties > Queue > "Shuffle upcoming": alles na de huidige video in willekeurige volgorde. |
| 3 | Slaaptimer verlengen | Opties > Sleep timer > "+15 min" zolang een timer met minuten loopt. |

## Nog open uit de vorige roadmap

| # | Item | Status |
|---|------|--------|
| 4 | YouTube sneller starten (was punt 6) | Wacht op een afspeellog van de Thor (Diagnostics > Share playback log). Zonder log is gokken. |
| 5 | Oude releases opruimen (was punt 19) | Wacht op akkoord van Stefan, want verwijderen kan niet terug. De workflow `cleanup-releases.yml` staat klaar. |
| 6 | Testrondje op het toestel | `docs/validation/thor-device-checklist.md` bevat nu ook 0.23.0. Alles sinds 0.9.0 is ongetest op de Thor. |

## Voorstel voor volgende rondes

| # | Item | Waarom | Omvang |
|---|------|--------|--------|
| 7 | Downloads opnieuw proberen met toenemende wachttijd | Mislukte downloads blijven nu liggen tot je ze handmatig herstart. | M |
| 8 | Wachtrij opslaan als afspeellijst | De wachtrij overleeft nu een herstart, maar niet als vaste lijst. | S |
| 9 | Hoofdstuk vooruit/terug op de schouderknoppen | Hoofdstukken bestaan al; alleen de knoppenkoppeling ontbreekt. | S |
| 10 | Wachtrij op het tweede scherm: husselen en wissen | Zelfde acties als in het optiesheet, maar op het bedieningsscherm. | S |
| 11 | Kleur van het tweede scherm uit de thumbnail | Retro-arcade gevoel; raakt de Compose-thema's, dus eerst op het toestel proberen. | M |
| 12 | Kijkgeschiedenis exporteren (CSV) | Past bij de bestaande Takeout-import en backup. | S |
| 13 | Echte volume-normalisatie met loudness-data | Nu een schatting; echte data bestaat alleen als de bron het levert. | L |

## Volgorde

1. Eerst het testrondje (6) en het afspeellog voor punt 4; dat bepaalt wat echt stuk is.
2. Daarna 7, 8, 9 en 10: klein en laag risico.
3. 11 en 13 pas na het testrondje.

## Open vragen voor Stefan

- Mag het opruimen van oude releases (punt 5) nu door?
- Welke van de nieuwe functies gebruik je echt? Wat ongebruikt blijft kan weg.
