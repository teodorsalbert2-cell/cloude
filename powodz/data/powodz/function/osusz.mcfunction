# Usuwa wodę od poziomu powodzi w dół do poziomu startowego (+1) wokół gracza
execute store result storage powodz:osusz y int 1 run scoreboard players get #poziom powodz
execute store result storage powodz:osusz r int 1 run scoreboard players get #promien powodz
scoreboard players operation #y powodz = #poziom powodz
function powodz:osusz_warstwa with storage powodz:osusz
tellraw @s [{"text":"[Powódź] ","color":"blue"},{"text":"Osuszono teren wokół Ciebie.","color":"green"}]
