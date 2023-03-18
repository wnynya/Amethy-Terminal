@ECHO OFF

IF /i "%1"=="start" ( 
  wsl -e bash bot.sh start
) ELSE IF /i "%1"=="stop" ( 
  wsl -e bash bot.sh stop
) ELSE IF /i "%1"=="restart" ( 
  wsl -e bash bot.sh restart
) ELSE IF /i "%1"=="attach" ( 
  wsl -e bash bot.sh attach
)
