# Deszcz: +1 na tick, burza: +2 na tick (2x szybciej), bez opadów: -1 na tick (woda opada)
execute if predicate powodz:burza run scoreboard players add #licznik powodz 2
execute unless predicate powodz:burza if predicate powodz:deszcz run scoreboard players add #licznik powodz 1
execute unless predicate powodz:deszcz if score #poziom powodz > #start powodz run scoreboard players remove #licznik powodz 1
execute unless predicate powodz:deszcz if score #poziom powodz <= #start powodz run scoreboard players set #licznik powodz 0
execute if score #licznik powodz >= #predkosc powodz run function powodz:podnies
scoreboard players operation #ujemna powodz = #predkosc powodz
scoreboard players operation #ujemna powodz *= #-1 powodz
execute if score #licznik powodz <= #ujemna powodz run function powodz:opadnij
# odświeżanie wody wokół graczy co 2 s
scoreboard players add #odswiez powodz 1
execute if score #odswiez powodz matches 40.. if score #poziom powodz > #start powodz run function powodz:zalej
execute if score #odswiez powodz matches 40.. run scoreboard players set #odswiez powodz 0
# pasek akcji
execute if predicate powodz:burza run return run title @a actionbar [{"text":"⛈ Burza! Woda szybko rośnie: Y=","color":"red"},{"score":{"name":"#poziom","objective":"powodz"},"color":"white"}]
execute if predicate powodz:deszcz run return run title @a actionbar [{"text":"🌧 Pada deszcz, woda rośnie: Y=","color":"aqua"},{"score":{"name":"#poziom","objective":"powodz"},"color":"white"}]
execute if score #poziom powodz > #start powodz run title @a actionbar [{"text":"☀ Woda opada: Y=","color":"green"},{"score":{"name":"#poziom","objective":"powodz"},"color":"white"}]
