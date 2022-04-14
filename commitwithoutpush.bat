:init
cls

@echo off
title Commitador da Boot IDE

echo Commitador da Boot IDE
echo.

set /p name="Nome do Commit: "

echo.

git status

echo.
set /p Input="Tem certeza de quer commitar? O commit "%name%" vai alterar os arquivos acima. (s/n) "

if /I "%Input%" == "s" goto yes

goto no

:yes
    git add .
    git commit -m "%name%"
    
    exit

:no
    set /p question="Deseja reescrever o commit? (s/n) "
    
    if /I "%question%" == "s" goto init
    
    exit
