echo off
set binname=%0

for %%F in ("%binname%") do set dirname=%%~dpF

set basename=%dirname%\..
set dirname=%dirname%\..\web\lib

setLocal EnableDelayedExpansion
set CLASSPATH="
for /R %dirname% %%a in (*.jar) do (
  set CLASSPATH=!CLASSPATH!;%%a
)
set CLASSPATH=!CLASSPATH!"

java -cp "%CLASSPATH%" bigs.core.BIGS --basedir %basename% %1 %2 %3 %4 %6 %6 %7 %8



