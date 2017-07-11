xas99.py -R -i -L knightlore.lst Source/knightlore.a99 -o kl1
@IF %ERRORLEVEL% NEQ 0 GOTO :end

java -jar tools/ea5tocart.jar kl1 "KNIGHT LORE"

copy /b kl18.bin + /b binary-data\sprite-rom.bin knightlore8.bin

:end