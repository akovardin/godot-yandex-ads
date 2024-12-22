# Godot Yandex Ads

Плагин под Godot 4.2+ для интеграции Яндекс рекламы с медиацией

Подробности про использование Yandex Ads SDK в андроид [описаны в документации](https://ads.yandex.com/helpcenter/en/dev/android/quick-start)

## Настройка рекламы

Реклама настраивается в партнерском кабинете яндекса. Всю информацию по настройке рекламы можно найти в официальной [документации Яндекса](https://ads.yandex.com/helpcenter/ru/quick-start)

Важно отметить, что плагин поддерживает работу с медиацией. Вы можете подключать другие сетки в кабинете Яндекса и они будут работать через этот плагин

## Интеграция

Для интеграции нужно скачать архив со [страницы релизов](https://gitflic.ru/project/kovardin/godot-yandex-ads/release?sort=TIME&direction=DESC) и скопировать плагин в папку *addons/GodotYandexAds*

Необходимо включить плагин через меню _Project > Project Settings_, выбрать вкладку _Plugins_ и отметить как **Enable** плагин с названием "GodotYandexAds"

![](plugin.png?r=1)

## Использование

Плагин позволяет интегрировать разные форматы рекламы в приложение:

- Адаптивный inline-баннер — гибкий формат баннерной рекламы, обеспечивающий максимальную эффективность за счет оптимизации размера рекламы для каждого устройства.
- Адаптивный sticky-баннер — небольшое, автоматически обновляемое рекламное объявление, которое располагается внизу или вверху экрана приложения. Баннер не перекрывает основной контент приложения и часто используется в приложениях-играх.
- Межстраничная реклама (interstitial) — полноэкранный формат рекламы, встраиваемый в контент приложения во время естественных пауз, таких как переход между уровнями игры или окончание выполнения целевого действия.
- Реклама с вознаграждением (rewarded) — популярный полноэкранный формат объявления, за просмотр которого пользователь получает поощрение.
- Реклама при открытии приложения (appopen) — специальный формат рекламы для монетизации экранов загрузки своих приложений. Такие объявления могут быть закрыты в любое время и предназначены для показа, когда пользователи выводят ваше приложение на передний план (foreground), либо при запуске, либо при возврате в него из фонового режима (background).

Пример как в коде использовать плагин:

```gdscript
extends Node2D

var _plugin_name = "GodotYandexAds"

@onready var interstitial_button = $CanvasLayer/VBoxContainer/Interstitial
@onready var rewared_button = $CanvasLayer/VBoxContainer/Rewarded
@onready var banner_button = $CanvasLayer/VBoxContainer/Banner


func _ready():
    rewared_button.pressed.connect(_on_rewarded_button_pressed)
    interstitial_button.pressed.connect(_on_interstitial_button_pressed)
    banner_button.pressed.connect(_on_banner_button_pressed)

    print(Engine.has_singleton(_plugin_name))

    if Engine.has_singleton(_plugin_name):
        var ads = Engine.get_singleton(_plugin_name)

        ads.banner_loaded.connect(_on_ad_loaded)
        ads.banner_on_impression.connect(_on_impression)

        ads.interstitial_loaded.connect(_on_ad_loaded)
        ads.interstitial_ad_shown.connect(_on_ad_shown)
        ads.interstitial_on_impression.connect(_on_impression)

        ads.rewarded_loaded.connect(_on_ad_loaded)
        ads.rewarded_ad_shown.connect(_on_ad_shown)
        ads.rewarded_rewarded.connect(_on_rewarded)
        ads.rewarded_on_impression.connect(_on_impression)

        ads.appopen_loaded.connect(_on_ad_loaded)
        ads.appopen_ad_shown.connect(_on_ad_shown)
        ads.appopen_on_impression.connect(_on_impression)

        ads.loadBanner("demo-banner-yandex", {"size_type": "sticky", "width": 300, "position":0})
        ads.loadInterstitial("demo-interstitial-yandex")
        ads.loadRewarded("demo-rewarded-yandex")
        ads.loadAppopen("demo-appopenad-yandex")


func _process(delta):
    pass


func _on_banner_button_pressed():
    if Engine.has_singleton(_plugin_name):
        var ads = Engine.get_singleton(_plugin_name)
        ads.showBanner("demo-banner-yandex")


func _on_interstitial_button_pressed():
    if Engine.has_singleton(_plugin_name):
        var ads = Engine.get_singleton(_plugin_name)
        ads.showInterstitial("demo-interstitial-yandex")


func _on_rewarded_button_pressed():
    if Engine.has_singleton(_plugin_name):
        var ads = Engine.get_singleton(_plugin_name)
        ads.showRewarded("demo-rewarded-yandex")


func _on_rewarded(id: String, data: Dictionary):
    print("_rewarded: " + id)
    print(data)


func _on_ad_loaded(id: String):
    print("_ad_loaded: " + id)


func _on_ad_shown(id: String):
    print("_ad_shown: " + id)


func _on_impression(id: String, data: String):
    print("_on_impression: " + id)
    print(data)
```

В этом примере используются демонстрацонные идентификаторы для юнитов разных форматов: demo-banner-yandex, demo-interstitial-yandex, demo-rewarded-yandex

### Доступные методы

#### Общие методы

- **init()** - ручная инициализация SDK рекламы
- **enableLogging(value: Boolean)** - принудительное включение/выключения логирования
- **setUserConsent(value: Boolean)** - включение/выключение учета пользовательского контекста при подборе рекламы
- **setLocationConsent(value: Boolean)** -  включение/выключение учета локации при подборе рекламы
- **setAgeRestrictedUser(value: Boolean)** - включение/выключение учета возрастного ограничения при подборе рекламы

#### Методы для работы с баннерами

- **loadBanner(id: String, params: Dictionary)** - загрузка баннера по идентификатору
- **showBanner(id: String)** - показ загруженного баннера
- **removeBanner(id: String)** - удаление баннера по идентификатору
- **hideBanner(id: String)** - метод для скрытия баненра по идентификатору 

#### Методы для работы со стишелами

- **loadInterstitial(id: String)** - загрузка и подготовка стишела по идентификатору
- **showInterstitial(id: String)** - показ стишела по идентификатору
- **removeInterstitial(id: String)** - удаление стишела по идентификатору

#### Методы для работы с ревардед

- **loadRewarded(id: String)** - загрузка ревордед по идентификатору
- **showRewarded(id: String)** - показ ревардед по идентификатору
- **removeRewarded(id: String)** - удаление ревардед по идентификатору

#### Методы для работы с аппопен

- **loadAppopen(id: String)** - загрузка аппопен юнита по идентификатору
- **removeAppopen()** - удаление аппопен

### Доступные настройки для баннера

Список настроек для баннера:

- **position** - можно указывать где будет баннер: `1` - сверху, `0` - внизу. Значение по умолчанию - `0`
- **size_type** - указывает тип баннреа, может иметь значение `"sticky"` или `"inline"`
- **safe_area** - нужно ли учитывать безопасную зону экрана. Принимает значение `true` или `false` по умолчанию `true`
- **width** - ограничение баннер по ширине
- **height** - ограничение баннера по высоте. Имеет значение только для sticky баннера

### Доступные сигналы

#### Общие сигналы

- **ads_initialized** - срабатывает при инициализации плагина

#### Сигналы для баннеров

- **banner_loaded(id: String)** - срабатывает при загрузке баннера. В параметрах передается `id`
- **banner_failed_to_load(id: String, error: Int)** - срабатывает при проблеме с загрузкой баннера. В папарметрах передается идентификатор `id` баннера и код ошбики `error` 
- **banner_ad_clicked(id: String)** - сигнал для обработки клика по баннеру. В апарметрах передается идентификатор баннера `id`
- **banner_left_application(id: String)** - срабатывает когда пользователь уходит из приложения, например при переходу в браузер при по клике на баннеру. В параметрах передается идентификатор баннера `id`
- **banner_returned_to_application(id: String)** - срабатывает когда пользователь возвращается в приложение после клика по баннеру. В параметрах передается индетификатор баннера `id`
- **banner_on_impression(id: String, impression: ImpressionData)** - срабатывает во время зучета показа рекламы. В параметрах передается идентификатор баннера `id` и данные показа `impression`

#### Сигналы для интерстишелов

- **interstitial_loaded(id: String)** - срабатывает при загрузке стишела. В параметрах передается `id`
- **interstitial_failed_to_load(id: String, error: Int)** - срабатывает при проблеме с загрузкой стишела. В параметрах передается идентификатор `id` стишела и код ошбики `error`
- **interstitial_failed_to_show(id: String, error: Int)** - срабатывает при ошибке во время попытки показать стишел. В параметрах передается идентификатор `id` стишела и код ошбики `error`
- **interstitial_ad_shown(id: String)** - срабатывает при показе стишела. В параметрах передается идентификатор `id`
- **interstitial_ad_dismissed(id: String)** - срабатыает при закрытии стишела. В параметрах передается идентификатор `id`
- **interstitial_ad_clicked(id: String)** - срабатывает при клике по стишелу. В параметрах передается идентификатор `id`
- **interstitial_on_impression(id: String, impression: ImpressionData)** - срабатывает во время учета показа рекламы. В параметрах передается идентификатор баннера `id` и данные показа `impression`

#### Сигналы для ревардед

- **rewarded_loaded(id: String)** - срабатывает при загрузке ревардед. В параметрах передается `id`
- **rewarded_failed_to_load(id: String, error: Int)** - срабатывает при проблеме с загрузкой ревардед. В параметрах передается идентификатор `id` ревардед и код ошбики `error`
- **rewarded_ad_shown(id: String)** - срабатывает при показе ревордед. В параметрах передается идентификатор `id`
- **rewarded_ad_dismissed(id: String)** - срабатыает при закрытии ревардед. В параметрах передается идентификатор `id`
- **rewarded_rewarded(id: String, reward: Dictionary)** - сигнализирует о получении пользователем вознаграждения. В параметрах передается идентификатор `id` и информация о вознаграждении `reward`
- **rewarded_ad_clicked(id: String)** - срабатывает при клике по ревардед. В параметрах передается идентификатор `id`
- **rewarded_on_impression(id: String, impression: ImpressionData)** - срабатывает во время учета показа рекламы. В параметрах передается идентификатор баннера `id` и данные показа `impression`
