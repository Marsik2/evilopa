# EvelineVisuals (Fabric 1.21.4)

Клиентский визуальный мод: 34 функции (HUD, эффекты, инфо, мировые визуалы).
Меню: Right Shift. Зум: C (удерживать). Друзья: /friend add|remove|list <ник>.

## Сборка jar
1) Нужна Java 21 и Gradle 8.12+ (или IntelliJ IDEA: Open -> Import Gradle).
2) В папке проекта: `gradle build`
3) Готовый мод: build/libs/evelinevisuals-1.0.0.jar -> в папку mods (нужны Fabric Loader и Fabric API для 1.21.4).

Без компьютера: загрузи папку в GitHub-репозиторий — Actions (.github/workflows/build.yml) сам соберёт jar, он будет в Artifacts.
Если версия Fabric API из gradle.properties не найдётся — возьми актуальную для 1.21.4 на fabricmc.net/develop.
