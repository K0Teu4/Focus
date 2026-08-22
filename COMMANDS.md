# FocusFlow — консольные команды

## Сборка и установка
.\gradlew.bat assembleDebug                 # собрать debug APK
.\gradlew.bat installDebug                  # собрать + установить на телефон
.\gradlew.bat installRelease                # release (подписан debug-ключом, для теста скорости)
.\gradlew.bat assembleRelease               # собрать release APK без установки

## Gradle
.\gradlew.bat --stop                        # остановить все daemon'ы
.\gradlew.bat --status                      # список daemon'ов
Remove-Item -Recurse -Force app\build       # ручная очистка build (если clean занят)
.\gradlew.bat clean                         # очистка (только если daemon остановлен)

## Тесты
.\gradlew.bat test                          # unit-тесты (JVM, быстро)
.\gradlew.bat connectedAndroidTest          # instrumented-тесты (нужен телефон по USB)
.\gradlew.bat testDebugUnitTest --tests "*PomodoroTest*"   # один класс тестов

## Логи и отладка
adb logcat                                  # все логи
adb logcat *:E                              # только ошибки
adb logcat --pid=$(adb shell pidof com.example.focusflow)  # логи приложения
adb devices                                 # список устройств
adb shell am force-stop com.example.focusflow              # убить приложение

## Лицензионные коды (Premium)
.\generate_license.ps1 -Payload "CLIENT1" # сгенерировать код активации
# Встроенные промо-коды: FOCUS-2026-FREE, POMODORO-PRO, K0TEU4-PREMIUM

## Иконка (одноразово, уже выполнено)
# Скрипт обрезки центра 1664x928 -> 512x512 в mipmap-xxxhdpi/ic_launcher.png
# (Add-Type -AssemblyName System.Drawing; см. историю чата)

## Подготовка к RuStore (позже)
# 1. keytool -genkey -v -keystore focusflow.keystore -alias focusflow -keyalg RSA -keysize 2048 -validity 10000
# 2. В app/build.gradle.kts: signingConfig release = свой keystore, isMinifyEnabled = true
# 3. .\gradlew.bat bundleRelease           # AAB для загрузки в консоль RuStore
