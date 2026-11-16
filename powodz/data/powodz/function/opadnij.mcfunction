# Usuwa najwyższą warstwę wody powodziowej wokół graczy i obniża poziom o 1
scoreboard players set #licznik powodz 0
execute if score #poziom powodz <= #start powodz run return 0
execute store result storage powodz:osusz y int 1 run scoreboard players get #poziom powodz
execute store result storage powodz:osusz r int 1 run scoreboard players get #promien powodz
execute as @a at @s if dimension minecraft:overworld run function powodz:osusz_jedna with storage powodz:osusz
scoreboard players remove #poziom powodz 1
execute if score #poziom powodz <= #start powodz run tellraw @a [{"text":"[Powódź] ","color":"blue"},{"text":"Woda całkowicie opadła.","color":"green"}]
