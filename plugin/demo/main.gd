extends Node2D

# TODO: Update to match your plugin's name
var _plugin_name = "GodotYandexAds"

@onready var interstitial_button = $CanvasLayer/VBoxContainer/Interstitial
@onready var rewared_button = $CanvasLayer/VBoxContainer/Rewarded

# Called when the node enters the scene tree for the first time.
func _ready():
	rewared_button.pressed.connect(_on_rewarded_button_pressed)
	interstitial_button.pressed.connect(_on_interstitial_button_pressed)

	print(Engine.has_singleton(_plugin_name))

	if Engine.has_singleton(_plugin_name):
		var ads = Engine.get_singleton(_plugin_name)
		ads.rewarded_rewarded.connect(_rewarded)
		ads.rewarded_ad_shown.connect(_ad_shown)
		ads.rewarded_on_impression.connect(_on_impression)

		ads.loadInterstitial("demo-interstitial-yandex")


# Called every frame. 'delta' is the elapsed time since the previous frame.
func _process(delta):
	pass


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


func _ad_shown(id: String):
	print("_ad_shown: " + id)


func _on_impression(id: String, data: String):
	print("_on_impression: " + id)
	print(data)