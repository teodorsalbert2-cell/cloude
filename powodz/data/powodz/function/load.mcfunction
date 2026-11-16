scoreboard objectives add powodz dummy
execute unless score #init powodz matches 1 run function powodz:domyslne
tellraw @a [{"text":"[Powódź] ","color":"blue"},{"text":"Załadowano. Wpisz ","color":"gray"},{"text":"/function powodz:pomoc","color":"aqua"}]
