# Poziom startowy = Y gracza wywołującego (albo #start, gdy z konsoli/bloku poleceń)
scoreboard players operation #poziom powodz = #start powodz
execute if entity @s[type=player] store result score #poziom powodz run data get entity @s Pos[1]
scoreboard players operation #start powodz = #poziom powodz
scoreboard players set #licznik powodz 0
scoreboard players set #aktywna powodz 1
weather thunder
tellraw @a [{"text":"[Powódź] ","color":"blue"},{"text":"Woda zaczyna się podnosić od Y=","color":"red"},{"score":{"name":"#poziom","objective":"powodz"},"color":"yellow"},{"text":"!","color":"red"}]
playsound minecraft:entity.elder_guardian.curse master @a ~ ~ ~ 1 0.6 1
function powodz:zalej
