$fill ~-$(r) $(y) ~-$(r) ~$(r) $(y) ~$(r) minecraft:air replace minecraft:water
scoreboard players remove #y powodz 1
execute if score #y powodz <= #start powodz run return 0
execute store result storage powodz:osusz y int 1 run scoreboard players get #y powodz
function powodz:osusz_warstwa with storage powodz:osusz
