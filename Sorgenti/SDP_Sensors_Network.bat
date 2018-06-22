@echo off
setlocal enabledelayedexpansion

set /p sink="Who start sink?" 
set /p freq="Sink frequency?"
set /p pir_ovest_battery="PIR Ovest Battery level?"
set /p pir_est_battery="PIR Est Battery level?"
set /p light_battery="Light Battery level?"
set /p temperature_battery="Temperature Battery level?"

for %%a in (pir_ovest pir_est light temperature) do (
	if not %sink%==%%a (
		start cmd /k java -jar SDPNode.jar %%a !%%a_battery! no %freq%
	)
)

for %%a in (pir_ovest pir_est light temperature) do (
	if %sink%==%%a (
		java -jar SDPNode.jar %%a !%%a_battery! yes %freq%
	)
)