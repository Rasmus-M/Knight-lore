IF EXIST DOA.dsk GOTO :dsk_ok
xdm99.py knightlore.dsk --initialize DSSD -n KNIGHTLORE
:dsk_ok

xas99.py -R -L knightlore.lst Source/knightlore.a99
@IF %ERRORLEVEL% NEQ 0 GOTO :end

xas99.py -R -i Source/knightlore.a99 -o kl1

xdm99.py knightlore.dsk -a kl1 -n KL1
xdm99.py knightlore.dsk -a kl2 -n KL2
xdm99.py knightlore.dsk -a kl3 -n KL3
xdm99.py knightlore.dsk -a kl4 -n KL4

:end