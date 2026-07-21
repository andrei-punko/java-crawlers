@echo off
chcp 65001 >nul
cd /d "%~dp0"

set "JAR=target\tabor.ru-crawler-1.0-SNAPSHOT-jar-with-dependencies.jar"
if not exist "%JAR%" (
  echo Fat jar not found. Run: mvn -pl tabor.ru-crawler -am package
  exit /b 1
)

rem Stop previous dashboard on this port, if any (unique PIDs)
setlocal EnableDelayedExpansion
set "SEEN="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":8080" ^| findstr "LISTENING"') do (
  echo !SEEN! | findstr /C:"[%%P]" >nul
  if errorlevel 1 (
    set "SEEN=!SEEN![%%P]"
    echo Stopping previous process on port 8080: PID %%P
    taskkill /F /PID %%P
  )
)
endlocal

rem Open browser without a second console window
start /b powershell -NoProfile -WindowStyle Hidden -Command "Start-Sleep -Seconds 2; Start-Process 'http://localhost:8080/'"

echo Starting dashboard ^(Ctrl+C to stop^). Do not kill all java.exe — that restarts Cursor's Java LS.
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 ^
  -cp "%JAR%" ^
  by.andd3dfx.tabor.dashboard.TaborDashboardServer ^
  --profiles profiles.json ^
  --photos photos ^
  --hidden hidden.json ^
  --favorites favorites.json ^
  --port 8080

if errorlevel 1 (
  echo.
  echo Dashboard failed to start. Often port 8080 is already busy.
  echo Check: netstat -ano ^| findstr :8080
  echo Or run: stop-dashboard.bat
  pause
)
