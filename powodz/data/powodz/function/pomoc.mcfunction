tellraw @s {"text":"=== Powódź - komendy ===","color":"blue","bold":true}
tellraw @s [{"text":"/function powodz:auto","color":"aqua"},{"text":" - tryb pogodowy (deszcz/burza = woda rośnie, słońce = opada)","color":"gray"}]
tellraw @s [{"text":"/function powodz:start","color":"aqua"},{"text":" - start od poziomu Twoich stóp","color":"gray"}]
tellraw @s [{"text":"/function powodz:stop","color":"aqua"},{"text":" - zatrzymaj podnoszenie wody","color":"gray"}]
tellraw @s [{"text":"/function powodz:wznow","color":"aqua"},{"text":" - wznów od obecnego poziomu","color":"gray"}]
tellraw @s [{"text":"/function powodz:szybciej","color":"aqua"},{"text":" / ","color":"gray"},{"text":"wolniej","color":"aqua"},{"text":" - zmień tempo","color":"gray"}]
tellraw @s [{"text":"/function powodz:osusz","color":"aqua"},{"text":" - usuń wodę powodziową wokół Ciebie","color":"gray"}]
tellraw @s [{"text":"/function powodz:reset","color":"aqua"},{"text":" - przywróć ustawienia domyślne","color":"gray"}]
tellraw @s [{"text":"Ustawienia: ","color":"gray"},{"text":"/scoreboard players set #predkosc|#max|#promien powodz <liczba>","color":"yellow"}]
