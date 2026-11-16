# 🌊 Powódź – datapack do Minecraft 1.21+

Woda stopniowo podnosi się w całym świecie (wokół wszystkich graczy w Overworldzie), towarzyszy jej burza.

## Instalacja
1. Pobierz `powodz.zip` (albo skopiuj folder `powodz`).
2. Wrzuć do `.minecraft/saves/<twój_świat>/datapacks/`.
3. W grze wpisz `/reload` (potrzebne cheaty / uprawnienia OP).

## Komendy
| Komenda | Działanie |
|---|---|
| `/function powodz:start` | start powodzi od poziomu Twoich stóp |
| `/function powodz:stop` | zatrzymanie podnoszenia wody |
| `/function powodz:wznow` | wznowienie od obecnego poziomu |
| `/function powodz:szybciej` / `wolniej` | 2× szybciej / wolniej |
| `/function powodz:osusz` | usuwa wodę powodziową wokół Ciebie |
| `/function powodz:reset` | ustawienia domyślne |
| `/function powodz:pomoc` | lista komend w grze |

## Ustawienia
`/scoreboard players set <nazwa> powodz <wartość>`

| Nazwa | Domyślnie | Opis |
|---|---|---|
| `#predkosc` | 200 | co ile ticków woda rośnie o 1 blok (20 = 1 s) |
| `#max` | 200 | maksymalny poziom wody (Y) |
| `#promien` | 32 | promień zalewania wokół gracza (max 48) |
| `#poziom` | – | obecny poziom wody (można zmienić ręcznie) |

## Jak to działa
Co `#predkosc` ticków poziom rośnie o 1, a co 2 sekundy datapack wypełnia wodą powietrze, trawę, kwiaty itp.
w kwadracie wokół każdego gracza (3 najwyższe warstwy powodzi). Dzięki temu woda pojawia się też tam,
gdzie gracz dopiero dojdzie. Bloki stałe nie są niszczone.
