scoreboard players set #2 powodz 2
scoreboard players operation #predkosc powodz *= #2 powodz
execute if score #predkosc powodz matches 12001.. run scoreboard players set #predkosc powodz 12000
tellraw @s [{"text":"[Powódź] ","color":"blue"},{"text":"Woda rośnie o 1 blok co ","color":"gray"},{"score":{"name":"#predkosc","objective":"powodz"},"color":"yellow"},{"text":" ticków","color":"gray"}]
