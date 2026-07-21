@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion
set "PORT=8080"
set "KILLED="
set "FAILED="
set "SEEN="

for /f "tokens=5" %%P in ('netstat -ano ^| findstr ":%PORT%" ^| findstr "LISTENING"') do (
  echo !SEEN! | findstr /C:"[%%P]" >nul
  if errorlevel 1 (
    set "SEEN=!SEEN![%%P]"
    echo Stopping dashboard on port %PORT%: PID %%P
    taskkill /F /PID %%P
    if errorlevel 1 (
      set "FAILED=1"
      echo.
      echo Access denied / failed to kill PID %%P.
      echo Close the console where run-dashboard.bat is running ^(Ctrl+C^),
      echo or end that process from Task Manager.
      echo Do NOT kill all java.exe — Cursor Java Language Server will restart itself.
    ) else (
      set "KILLED=1"
    )
  )
)

if defined FAILED (
  exit /b 1
)

if defined KILLED (
  echo Done. Port %PORT% is free — можно делать mvn clean.
) else (
  echo Nothing listening on port %PORT%.
)
endlocal
