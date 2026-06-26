@echo off
echo PayBus Backend ishga tushyapti...
echo.
cd /d C:\PayBus\backend
python -m uvicorn app.main:app --host 0.0.0.0 --port 8001 --reload
pause
