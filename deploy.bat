@ECHO OFF

echo Cleaning the target directory...
call mvn -q clean
if not "%ERRORLEVEL%" == "0" exit /b

echo Building the project website and downloadable artifacts...
call mvn -q site
if not "%ERRORLEVEL%" == "0" exit /b

echo Cleaning the downloads directory...
RMDIR /S /Q %~dp0target\site\downloads\1.0\app
RMDIR /S /Q %~dp0target\site\downloads\1.0\musicplayer-win-installer

echo Uploading the project website...
mvn -q com.github.github:site-maven-plugin:0.12:site
if not "%ERRORLEVEL%" == "0" exit /b

echo The project was successfully released.

echo.
pause