# Stress-test serwera Minecraft (Node.js + mineflayer)

Podłącza do **Twojego** serwera wiele botów naraz i sprawdza, jak serwer to znosi.

Każdy bot:
- loguje się w AuthMe, czyli sam wysyła `/register` albo `/login`,
- chodzi losowo i skacze,
- co jakiś czas pisze na czacie.

Na koniec skrypt wypisuje podsumowanie:
- ile botów weszło na serwer,
- kicki i błędy,
- ping każdego bota,
- szacowany TPS serwera.

Działa tylko z adresami lokalnymi: `localhost`, `127.x`, `10.x`, `172.16–31.x` i `192.168.x`.

## Uruchomienie

```
cd stress-test
npm install
npm start
```

Parametry z linii komend nadpisują `config.json`:

```
node index.js --bots 20 --duration 300 --delay 2000 --port 25565
```

## Konfiguracja (`config.json`)

| pole | znaczenie |
|---|---|
| `host`, `port`, `version` | adres serwera i wersja (domyślnie `127.0.0.1:25565`, `1.20.1`) |
| `bots` | liczba botów |
| `nickPrefix` | początek nicka botów, np. `StressBot_1`, `StressBot_2`… |
| `joinDelayMs` | odstęp między wejściami kolejnych botów |
| `durationSec` | ile trwa test po wejściu ostatniego bota |
| `authmePassword` | hasło do `/register` i `/login` |
| `walk`, `chat`, `chatEverySec`, `chatMessages` | ruch botów i wiadomości na czacie |
| `reportEverySec` | jak często wypisywać raport z liczbą botów online i TPS |

Log każdego przebiegu trafia do `logs/run-<data>.log`.

## Wskazówki dla Paper

- Ustaw `online-mode=false`, bo boty łączą się bez kont Mojang.
- Konfiguracja AuthMe może blokować wiele kont z jednego IP: `maxRegPerIp` i `maxJoinPerIp`. Na czas testu podnieś te limity albo dodaj `127.0.0.1` do wyjątków.
- Jeśli kicki mówią o „connection throttle”, ustaw `connection-throttle: -1` w `bukkit.yml` albo zwiększ `joinDelayMs`.
