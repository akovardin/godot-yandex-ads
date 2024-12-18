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

		ads.banner_loaded.connect(_ad_loaded)
		ads.banner_on_impression.connect(_on_impression)

		ads.interstitial_loaded.connect(_ad_loaded)
		ads.interstitial_ad_shown.connect(_ad_shown)
		ads.interstitial_on_impression.connect(_on_impression)

		ads.rewarded_rewarded.connect(_rewarded)
		ads.rewarded_ad_shown.connect(_ad_shown)
		ads.rewarded_on_impression.connect(_on_impression)

		ads.loadBanner("demo-banner-yandex", {"size_type": "sticky", "width": 300, "position":0})
		ads.loadInterstitial("demo-interstitial-yandex")
		ads.loadRewarded("demo-rewarded-yandex")


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


func _rewarded(id: String, data: Dictionary):
	print("_rewarded: " + id)
	print(data)


func _ad_loaded(id: String):
	print("_ad_loaded: " + id)


func _ad_shown(id: String):
	print("_ad_shown: " + id)


func _on_impression(id: String, data: String):
	print("_on_impression: " + id)
	print(data)