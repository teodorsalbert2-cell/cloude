# Włącza/wyłącza tryb pogodowy
execute if score #auto powodz matches 1 run return run function powodz:auto_wylacz
scoreboard players set #auto powodz 1
scoreboard players set #aktywna powodz 0
scoreboard players set #licznik powodz 0
execute if entity @s[type=player] store result score #start powodz run data get entity @s Pos[1]
scoreboard players operation #poziom powodz = #start powodz
tellraw @a [{"text":"[Powódź] ","color":"blue"},{"text":"Tryb pogodowy WŁĄCZONY. ","color":"aqua"},{"text":"Deszcz = woda rośnie, burza = 2x szybciej, słońce = woda opada. Poziom bazowy Y=","color":"gray"},{"score":{"name":"#start","objective":"powodz"},"color":"yellow"}]
