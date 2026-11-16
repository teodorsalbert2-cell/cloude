# Przygotuj argumenty makra: górna i dolna warstwa oraz promień
scoreboard players set #odswiez powodz 0
execute if score #promien powodz matches 49.. run scoreboard players set #promien powodz 48
execute if score #promien powodz matches ..0 run scoreboard players set #promien powodz 1
scoreboard players operation #dol powodz = #poziom powodz
scoreboard players remove #dol powodz 2
execute if score #dol powodz < #start powodz run scoreboard players operation #dol powodz = #start powodz
execute store result storage powodz:dane gora int 1 run scoreboard players get #poziom powodz
execute store result storage powodz:dane dol int 1 run scoreboard players get #dol powodz
execute store result storage powodz:dane r int 1 run scoreboard players get #promien powodz
execute as @a[gamemode=!spectator] at @s if dimension minecraft:overworld run function powodz:zalej_gracz with storage powodz:dane
