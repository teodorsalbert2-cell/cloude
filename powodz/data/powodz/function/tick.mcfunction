execute unless score #aktywna powodz matches 1 run return 0
scoreboard players add #licznik powodz 1
execute if score #licznik powodz >= #predkosc powodz run function powodz:podnies
# co 2 sekundy dolewamy wodę wokół graczy (żeby zalewało też nowe tereny, po których chodzą)
scoreboard players add #odswiez powodz 1
execute if score #odswiez powodz matches 40.. run function powodz:zalej
# pasek akcji z poziomem wody
execute as @a at @s if dimension minecraft:overworld run title @s actionbar [{"text":"🌊 Poziom wody: Y=","color":"aqua"},{"score":{"name":"#poziom","objective":"powodz"},"color":"white"}]
