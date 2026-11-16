scoreboard players set #licznik powodz 0
execute if score #poziom powodz >= #max powodz run return run function powodz:szczyt
scoreboard players add #poziom powodz 1
execute as @a at @s if dimension minecraft:overworld run playsound minecraft:ambient.underwater.enter ambient @s ~ ~ ~ 0.6 0.7
function powodz:zalej
