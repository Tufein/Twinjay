# TwinJay roadmap: extra functies en vervolgwerk

Opgesteld op 2026-11-08, na release 0.19.1-thor.1. Dit is een voorstel, geen belofte. Het is gebaseerd op de
changelog, de PR-historie (#4 t/m #22), de projectnotities en de bekende "niet getest op de Thor"-punten.
Er staan geen open issues of PR's. Formaat: S = enkele uren, M = een dag, L = meerdere dagen.

## Stand per 2026-11-09 (0.20.0)

- Gebouwd: 1, 2, 4, 9, 10, 13, 14, 18.
- Al gedekt: 3 (dim/wake-fix uit 0.19.1).
- Nog open: 5, 6, 7, 8, 11, 12, 15, 16, 17, 19, 20, 21. Voor 16 bestaat code voor automatische back-up
  (`StateBackup.startAutomaticBackup`), maar die staat uit (`shouldAutomaticBackup()` geeft altijd false).

## Wat al staat (kort)

Tweede scherm met presets, dimmen, klok, couch-modus en swipes. Downloads met venster, limiet en kwaliteit per
kanaal. SponsorBlock, A-B herhaling, slaaptimer, abonnementsgroepen, bladwijzers, statistieken, in-app updates,
QR-instellingen en een widget. Zie `CHANGELOG-THOR.md` voor de volledige lijst.

## Prioriteit 1: betrouwbaarheid en verificatie op het apparaat

De grootste winst zit nu niet in nieuwe knoppen. Bijna alles sinds 0.9.0 is alleen door CI getest.

| # | Item | Waarom | Omvang |
|---|------|--------|--------|
| 1 | Testrondje op de Thor met een vaste checklist (`docs/validation/`) | Swap screens, in-app installatie, dim/klok, widget en QR zijn nooit op het toestel bevestigd. Eén lijst met "werkt / werkt niet" maakt de volgende fixronde gericht. | S |
| 2 | Veilige terugval bij swap screens | Als het scherm verkeerd landt kan de app op het verkeerde display blijven hangen. Een harde reset (lang drukken op een knop of een bevestigde timeout die ook na herstart werkt) haalt dat risico weg. | M |
| 3 | Dim- en wake-gedrag als eigen testbare module | 0.15.1 en 0.19.1 repareerden allebei dim/wake. Toestandslogica los van Compose, met unit tests, voorkomt een derde ronde. | M |
| 4 | Foutmeldingen en logs per bron | De YouTube-plugin faalde eerder stil (#8). Toon bij elke bron waarom iets mislukt, met "Deel log" in één tik. | S |
| 5 | Automatische rooktest in CI voor de thor-flavor (lint, start van de app, instellingen openen) | Voorkomt kapotte releases zoals de lint-fouten in #6. | M |

## Prioriteit 2: afspelen en opstarten

| # | Item | Waarom | Omvang |
|---|------|--------|--------|
| 6 | Opstarttijd YouTube verder omlaag (ca. 8,5 s in de plugin) | Grootste dagelijkse irritatie. Opties: tokens en visitor data bewaren over app-starts heen, eerder voorverwarmen na boot. Eerder bleek token-persistentie in plugin-JS te zitten, dus eerst onderzoeken wat haalbaar is. | L |
| 7 | Prefetch van "volgende video" op basis van wachtrij en autoplay | Maakt overgangen vlak. Past bij de al bestaande prefetch. | M |
| 8 | Adaptieve buffer-instelling per netwerk (wifi/mobiel) | Bouwt voort op de netwerkpreset (0.14.0). Minder haperingen onderweg. | M |
| 9 | Stats-overlay uitbreiden met buffer, dropped frames en bitrate-historie | Helpt bij elke volgende haperingsmelding zonder adb. | S |

## Prioriteit 3: tweede scherm en bediening

| # | Item | Waarom | Omvang |
|---|------|--------|--------|
| 10 | Lay-outprofielen koppelen aan situatie (thuis/onderweg/laden) | Profielen en netwerkpreset bestaan al, maar zijn los van elkaar. Eén automatisch profiel is minder gedoe. | M |
| 11 | Gamepad-hints en een herbindbare knoppenmap | De Thor is een handheld. Eigen knoppen per scherm/modus maken bediening sneller. | M |
| 12 | Tweede scherm als zoek- en wachtrijscherm (typen met toetsenbord, wachtrij slepen) | Het tweede scherm is nu vooral bediening. Zoeken daar maakt het echt tweeschermig. | L |
| 13 | Widget uitbreiden (play/pauze, volgende) | De widget toont nu alleen de laatste video. | S |

## Prioriteit 4: bibliotheek en downloads

| # | Item | Waarom | Omvang |
|---|------|--------|--------|
| 14 | Slimme opruiming met voorstel ("deze 12 gezien en >30 dagen oud") | Er zijn limieten en een lijst met grootste downloads. Eén bevestigscherm maakt het af. | S |
| 15 | Offline-afspeellijsten (hele lijst downloaden en bijhouden) | Natuurlijke stap na auto-download per kanaal. | M |
| 16 | Backup naar bestand of cloud op schema | Nu handmatig of via QR/tekst. Een automatische dagelijkse export beschermt tegen dataverlies. | M |
| 17 | Zoeken over alles heen (geschiedenis, downloads, ondertitels, bladwijzers) | Deze data bestaat al apart. Eén zoekbalk verbindt het. | M |

## Prioriteit 5: onderhoud en lange termijn

| # | Item | Waarom | Omvang |
|---|------|--------|--------|
| 18 | Upstream-sync plan (Bluejay/FutoVideo) | De fork wijkt steeds verder af. Leg vast wat we overnemen en hoe vaak. | S |
| 19 | Opruimen van oude releases en `release-assets/*` branches | Staat al lang open. Wacht op een besluit van Stefan. | S |
| 20 | Pluginbeheer: update-melding en veilige terugval naar vorige versie | De app leunt zwaar op de YouTube-plugin. Terugdraaien bij een kapotte update voorkomt een dode app. | M |
| 21 | Toegankelijkheid en tekstgrootte-controle op beide schermen | Er is een tekstgrootte-instelling, maar geen systematische controle. | S |

## Voorgestelde volgorde

1. Items 1, 4 en 9: snel, geven inzicht in wat echt kapot is.
2. Items 2 en 3: halveren het risico op nieuwe dim- en swap-problemen.
3. Items 6 en 7: grootste merkbare winst voor dagelijks gebruik.
4. Daarna naar smaak uit prioriteit 3 en 4.

## Open vragen voor Stefan

- Welke van de recente functies gebruik je echt? Wat ongebruikt blijft kan verborgen of geschrapt worden.
- Moet de app ooit buiten de Thor draaien (andere telefoon), of blijft het Thor-only?
- Mag het opruimen van oude releases (punt 19) nu door?

## Kanttekening

Ik heb de repo en de historie gelezen, maar niets op een toestel gedraaid. Omvang is een grove schatting.
