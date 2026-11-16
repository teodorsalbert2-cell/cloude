scoreboard players set #auto powodz 0
scoreboard players set #aktywna powodz 0
weather clear
tellraw @a [{"text":"[Powódź] ","color":"blue"},{"text":"Woda przestała się podnosić (Y=","color":"green"},{"score":{"name":"#poziom","objective":"powodz"},"color":"yellow"},{"text":").","color":"green"}]
