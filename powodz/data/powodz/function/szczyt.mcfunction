execute if score #auto powodz matches 1 run return 0
scoreboard players set #aktywna powodz 0
tellraw @a [{"text":"[Powódź] ","color":"blue"},{"text":"Woda osiągnęła maksymalny poziom Y=","color":"dark_aqua"},{"score":{"name":"#max","objective":"powodz"},"color":"yellow"}]
