# BattDeck

<img src="icon.png" width="128">

**BattDeck** — Android-застосунок для обліку комплектів батарей БПЛА.

Мета проста: швидко бачити, які батареї заряджені, яка батарея зараз активна, які комплекти вже використані, і в якій черзі їх брати.

Це не складна ERP-система і не “розумний хмарний сервіс”. Це компактний офлайн-інструмент для бойового використання.

BattDeck працює повністю офлайн за принципом local-first. Дані зберігаються лише на пристрої; для перенесення між пристроями доступні ручні імпорт та експорт JSON через стандартні Android file picker і share sheet.

## Основна ідея

- список комплектів батарей;
- номер кожного комплекту;
- редаговане маркування та колір комплекту;
- поточна напруга;
- відсоток заряду, розрахований зі шкали напруги;
- дата останньої зміни заряду;
- активна батарея;
- ручна зміна заряду;
- швидке скидання використаної батареї;
- зміна порядку черги;
- локальний імпорт та експорт JSON через системний файловий діалог і меню поширення.

## Стек

Базова рекомендація для нової версії:

- Kotlin;
- Jetpack Compose;
- локальний JSON-файл;
- Material 3 як технічна база, але з кастомним tactical/pixel UI.

## Документація

- [Purpose](docs/PURPOSE.md)
- [Product Specification](docs/PRODUCT_SPEC.md)
- [User Interface](docs/UI_SPEC.md)
- [Data Model](docs/DATA_MODEL.md)
- [Battery Rules](docs/BATTERY_RULES.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Project Structure](docs/STRUCTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [Codex Notes](docs/CODEX_NOTES.md)

## Принцип

Застосунок має бути швидкий, простий, офлайн і зрозумілий з першого погляду.

Оператор не повинен думати, де що натискати. Відкрив — побачив стан батарей — взяв правильний комплект.

## Приватність

BattDeck не використовує:

- акаунти або авторизацію;
- інтернет чи віддалений сервер;
- хмарну синхронізацію;
- аналітику або tracking SDK;
- рекламу чи покупки.

Застосунок не запитує `INTERNET`, broad storage permissions або доступ до списку встановлених застосунків. Усі робочі дані залишаються у приватному локальному сховищі Android, доки користувач сам не експортує JSON.

## Збірка і запуск

Потрібні Android Studio з JDK 17 та Android SDK 35.

Найпростіший спосіб — запустити інтерактивний раннер:

```bash
./runner.sh
```

Або виконати конкретну дію без меню:

```bash
./runner.sh doctor      # перевірити середовище і підключення
./runner.sh build-run   # зібрати, встановити й запустити на телефоні
./runner.sh release     # зібрати release APK та AAB у build/
./runner.sh test        # запустити unit-тести
./runner.sh clean       # очистити результати збірки
./runner.sh deep-clean  # також прибрати локальний кеш Gradle
./runner.sh help        # усі доступні команди
```

На телефоні потрібно ввімкнути «Для розробників» → «Налагодження через USB» і підтвердити доступ для цього комп’ютера. Якщо підключено кілька пристроїв, раннер запропонує потрібний.

Команди Gradle напряму:

```bash
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew test
```

Debug APK буде створено в `app/build/outputs/apk/debug/`. Без `keystore.properties` release-команда створює unsigned APK, придатний для підпису F-Droid. Для запуску відкрийте корінь репозиторію в Android Studio, дочекайтеся синхронізації Gradle та запустіть конфігурацію `app` на пристрої або емуляторі з Android 7.0 чи новішим.

### Підпис release-збірки

Release APK та AAB підписуються ключем `keystore/battdeck-upload.jks`. Скопіюйте локальний шаблон і внесіть справжні дані ключа:

```bash
cp keystore.properties.example keystore.properties
```

Заповніть `storePassword`, `keyAlias` і `keyPassword`, після чого виконайте `./runner.sh release`. Файл `keystore.properties` і самі keystore-файли виключені з Git. Підписані результати зʼявляться як `build/BattDeck-release.apk` та `build/BattDeck-release.aab`.

## Розповсюдження

Підписані APK можна розповсюджувати напряму через GitHub Releases або інший довірений канал. Користувач має перевіряти джерело та підпис APK перед встановленням.

Репозиторій підготовлений для F-Droid: draft recipe знаходиться в `metadata/com.catemup.battdeck.yml`, а локалізовані описи — у `fastlane/metadata/android/`. Офіційне включення до каталогу F-Droid потребує публічного tag, доступного source archive та окремого merge request до `fdroiddata`.

## F-Droid repository

BattDeck можна встановити з власного F-Droid-репозиторію:

```text
https://zarant77.github.io/batt-deck/fdroid/repo/
```

Інструкції зі створення, підпису та публікації репозиторію: [docs/FDROID_REPO.md](docs/FDROID_REPO.md).

## Іконка застосунку

Файл `icon.png` у корені репозиторію є єдиним джерелом для Android launcher icons та іконки F-Droid-репозиторію. Щоб згенерувати всі іконки без Android Studio, встановіть ImageMagick і запустіть скрипт:

```bash
brew install imagemagick
./tools/generate_launcher_icons.sh
```

Також можна обрати `Generate launcher icons` в інтерактивному `./runner.sh`.

## Реалізовано у v0.3.0

- пʼять MVP-екранів українською мовою;
- локальне збереження всіх даних в одному JSON-файлі;
- зміна заряду, маркування й назви батареї;
- активація та скидання свайпами;
- безпечна зміна кількості комплектів і шкали напруги.
- обмін резервними JSON-файлами між пристроями без хмари чи акаунтів.

## License

BattDeck is licensed under the Apache License 2.0. See [LICENSE](LICENSE).
