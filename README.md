# Структура проекта StoreKeep

Офлайн Android-приложение учета товара (Java, SQLite, Material 3). Корень репозитория:

```text
StoreKeep/
├── app/
│   ├── build.gradle.kts          # модуль приложения, зависимости
│   ├── proguard-rules.pro
│   └── src/
│       ├── androidTest/java/     # инструментальные тесты
│       ├── test/java/            # unit-тесты
│       └── main/
│           ├── AndroidManifest.xml # activity, launcher, тема
│           ├── java/com/example/storekeep/
│           │   ├── LoginActivity.java
│           │   ├── RegisterActivity.java
│           │   ├── HomeActivity.java       # нижняя навигация, контейнер фрагментов
│           │   ├── CatalogActivity.java  # экран «Склад» (список как каталог)
│           │   ├── AddEditProductActivity.java
│           │   ├── SaleActivity.java
│           │   ├── DashboardFragment.java
│           │   ├── ProductsFragment.java
│           │   ├── HistoryFragment.java
│           │   ├── ReportsFragment.java
│           │   ├── data/
│           │   │   ├── DatabaseHelper.java  # SQLite: users, products, operations
│           │   │   └── SessionManager.java  # SharedPreferences, сессия входа
│           │   ├── model/
│           │   │   ├── Product.java
│           │   │   └── StockOperation.java
│           │   ├── ui/
│           │   │   ├── ProductAdapter.java
│           │   │   └── OperationAdapter.java
│           │   └── util/
│           │       └── MoneyFormat.java     # отображение сум в сўм
│           └── res/
│               ├── layout/         # XML-разметки экранов и item’ов списков
│               ├── menu/           # нижнее меню навигации
│               ├── values/         # строки, цвета, тема (светлая)
│               ├── values-night/   # тема и цвета тёмной темы
│               ├── drawable/       # векторные иконки навигации и т.п.
│               ├── mipmap*/        # иконки лаунчера
│               └── xml/            # backup / data extraction rules
├── gradle/
│   └── libs.versions.toml        # каталог версий (при использовании)
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
├── build.gradle.kts              # корневой Gradle
└── PROJECT_STRUCTURE.md          # этот файл
```

## Назначение слоёв

- **Activity** — полноэкранные шаги: вход, регистрация, главная оболочка с `BottomNavigationView`, вспомогательные экраны (склад, продажа, форма товара).
- **Fragment** — вкладки внутри `HomeActivity`: главная панель, каталог с поиском и фильтром категории, история операций, отчёты с графиками.
- **data** — доступ к SQLite и кратковременному флагу «вошёл ли пользователь».
- **model** — простые данные для списков и форм.
- **ui** — адаптеры `RecyclerView`.
- **util** — форматирование валюты для UI.
- **res** — всё, что отображается без логики в Java: макеты, строки, стили, иконки.

## Сборка

Сборка выполняется из корня командой Gradle (`assembleDebug` / `assembleRelease`). Папки `build/` и файл `local.properties` не хранятся в репозитории (см. `.gitignore`).
