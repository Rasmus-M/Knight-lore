xas99.py -R -i -L knightlore.lst Source/knightlore.a99 -o kl1
@IF %ERRORLEVEL% NEQ 0 GOTO :end

java -jar tools/ea5tocart.jar kl1 "KNIGHT LORE"

copy /b kl18.bin + /b binary-data\sprite-rom.bin + /b binary-data\start-screen-patterns.bin + /b binary-data\start-screen-colors.bin knightlore8.bin

java -jar tools/CopyHeader.jar knightlore8.bin 60

:end