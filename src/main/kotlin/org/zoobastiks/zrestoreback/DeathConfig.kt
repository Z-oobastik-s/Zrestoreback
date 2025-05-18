package org.zoobastiks.zrestoreback

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin

class DeathConfig {
    // Настройки для миров
    private val enabledWorlds = mutableListOf<String>()
    private val disabledWorlds = mutableListOf<String>()
    
    // Настройки экономики
    var teleportCost: Double = 100.0
    var itemsReturnCost: Double = 250.0
    
    // Сообщения
    var deathMessageHeader: String = "<dark_red>===== <red>Точки смерти</red> =====</dark_red>"
    var deathMessage: String = "<yellow>Смерть #%index%: <white>%world% <gray>(%x%, %y%, %z%)</gray></white></yellow>"
    var deathMessageFooter: String = "<dark_red>=====================</dark_red>"
    var teleportButtonText: String = "[Телепорт]"
    var itemsButtonText: String = "[Вещи]"
    var teleportSuccessMessage: String = "<green>Вы были телепортированы на место смерти за %cost% монет.</green>"
    var teleportFailMessage: String = "<red>У вас недостаточно средств для телепортации. Требуется: %cost% монет.</red>"
    var itemsReturnSuccessMessage: String = "<green>Ваши вещи были возвращены за %cost% монет.</green>"
    var itemsReturnFailMessage: String = "<red>У вас недостаточно средств для возврата вещей. Требуется: %cost% монет.</red>"
    var noDeathPointsMessage: String = "<red>У вас нет сохраненных точек смерти.</red>"
    var invalidDeathPointMessage: String = "<red>Указанная точка смерти не существует.</red>"
    var insufficientFundsTeleportMessage: String = "&cУ вас недостаточно денег для телепортации. Требуется: %cost%$"
    var insufficientFundsItemsMessage: String = "&cУ вас недостаточно денег для восстановления предметов. Требуется: %cost%$"
    
    // Звуки
    var deathSoundEnabled: Boolean = true
    var deathSound: String = "ENTITY_WITHER_DEATH"
    var teleportSoundEnabled: Boolean = true
    var teleportSound: String = "ENTITY_ENDERMAN_TELEPORT"
    var itemsReturnSoundEnabled: Boolean = true
    var itemsReturnSound: String = "ENTITY_PLAYER_LEVELUP"
    
    // Команды
    var executeCommandsOnDeath: Boolean = false
    var deathCommands: List<String> = listOf("say %player% умер в мире %world% на координатах %x% %y% %z%")
    
    // Лимиты
    var maxDeathsStored: Int = 5
    var maxDeathsShown: Int = 5
    
    // Настройки сохранения данных
    var autoSaveInterval: Int = 10 // Интервал автосохранения в минутах
    var showSaveMessages: Boolean = true // Показывать сообщения о сохранении
    
    fun load(plugin: JavaPlugin) {
        val config = plugin.config
        
        // Загружаем общие настройки
        enabledWorlds.clear()
        enabledWorlds.addAll(config.getStringList("settings.enabled_worlds"))
        if (enabledWorlds.isEmpty()) {
            enabledWorlds.addAll(listOf("world", "lobby_nether", "lobby_the_end", "lobby_kattersstructures_deep_blue"))
        }
        
        disabledWorlds.clear()
        disabledWorlds.addAll(config.getStringList("settings.disabled_worlds"))
        if (disabledWorlds.isEmpty()) {
            disabledWorlds.addAll(listOf("lobby", "oneblock_world"))
        }
        
        // Загружаем настройки сохранения
        autoSaveInterval = config.getInt("settings.auto_save_interval", 10)
        showSaveMessages = config.getBoolean("settings.show_save_messages", true)
        
        // Загружаем настройки экономики
        teleportCost = config.getDouble("economy.teleport_cost", 100.0)
        itemsReturnCost = config.getDouble("economy.items_return_cost", 250.0)
        
        // Загружаем сообщения
        loadMessages(config)
        
        // Загружаем настройки звуков
        loadSounds(config)
        
        // Загружаем настройки команд
        executeCommandsOnDeath = config.getBoolean("commands.execute_on_death", false)
        deathCommands = config.getStringList("commands.on_death")
        if (deathCommands.isEmpty()) {
            deathCommands = listOf("say %player% умер в мире %world% на координатах %x% %y% %z%")
        }
        
        // Загружаем лимиты
        maxDeathsStored = config.getInt("settings.max_deaths_stored", 5)
        maxDeathsShown = config.getInt("settings.max_deaths_shown", 5)
        
        // Сохраняем конфигурацию по умолчанию
        saveDefaults(plugin)
        
        // Загружаем сообщения о недостатке средств
        insufficientFundsTeleportMessage = config.getString("messages.teleport_fail", insufficientFundsTeleportMessage) ?: insufficientFundsTeleportMessage
        insufficientFundsItemsMessage = config.getString("messages.items_return_fail", insufficientFundsItemsMessage) ?: insufficientFundsItemsMessage
    }
    
    private fun loadMessages(config: FileConfiguration) {
        deathMessageHeader = config.getString("messages.header", deathMessageHeader) ?: deathMessageHeader
        deathMessage = config.getString("messages.death_point", deathMessage) ?: deathMessage
        deathMessageFooter = config.getString("messages.footer", deathMessageFooter) ?: deathMessageFooter
        teleportButtonText = config.getString("messages.teleport_button", teleportButtonText) ?: teleportButtonText
        itemsButtonText = config.getString("messages.items_button", itemsButtonText) ?: itemsButtonText
        teleportSuccessMessage = config.getString("messages.teleport_success", teleportSuccessMessage) ?: teleportSuccessMessage
        teleportFailMessage = config.getString("messages.teleport_fail", teleportFailMessage) ?: teleportFailMessage
        itemsReturnSuccessMessage = config.getString("messages.items_return_success", itemsReturnSuccessMessage) ?: itemsReturnSuccessMessage
        itemsReturnFailMessage = config.getString("messages.items_return_fail", itemsReturnFailMessage) ?: itemsReturnFailMessage
        noDeathPointsMessage = config.getString("messages.no_death_points", noDeathPointsMessage) ?: noDeathPointsMessage
        invalidDeathPointMessage = config.getString("messages.invalid_death_point", invalidDeathPointMessage) ?: invalidDeathPointMessage
    }
    
    private fun loadSounds(config: FileConfiguration) {
        deathSoundEnabled = config.getBoolean("sounds.death.enabled", true)
        deathSound = config.getString("sounds.death.sound", deathSound) ?: deathSound
        
        teleportSoundEnabled = config.getBoolean("sounds.teleport.enabled", true)
        teleportSound = config.getString("sounds.teleport.sound", teleportSound) ?: teleportSound
        
        itemsReturnSoundEnabled = config.getBoolean("sounds.items_return.enabled", true)
        itemsReturnSound = config.getString("sounds.items_return.sound", itemsReturnSound) ?: itemsReturnSound
    }
    
    private fun saveDefaults(plugin: JavaPlugin) {
        val config = plugin.config
        
        // Миры
        if (!config.isSet("worlds.enabled")) {
            config.set("worlds.enabled", enabledWorlds)
        }
        
        if (!config.isSet("worlds.disabled")) {
            config.set("worlds.disabled", disabledWorlds)
        }
        
        // Экономика
        if (!config.isSet("economy.teleport_cost")) {
            config.set("economy.teleport_cost", teleportCost)
        }
        
        if (!config.isSet("economy.items_return_cost")) {
            config.set("economy.items_return_cost", itemsReturnCost)
        }
        
        // Сообщения
        saveDefaultMessages(config)
        
        // Звуки
        saveDefaultSounds(config)
        
        // Команды
        if (!config.isSet("commands.execute_on_death")) {
            config.set("commands.execute_on_death", executeCommandsOnDeath)
        }
        
        if (!config.isSet("commands.on_death")) {
            config.set("commands.on_death", deathCommands)
        }
        
        // Лимиты
        if (!config.isSet("settings.max_deaths_stored")) {
            config.set("settings.max_deaths_stored", maxDeathsStored)
        }
        
        if (!config.isSet("settings.max_deaths_shown")) {
            config.set("settings.max_deaths_shown", maxDeathsShown)
        }
        
        // Настройки сохранения
        if (!config.isSet("settings.auto_save_interval")) {
            config.set("settings.auto_save_interval", autoSaveInterval)
        }
        
        if (!config.isSet("settings.show_save_messages")) {
            config.set("settings.show_save_messages", showSaveMessages)
        }
        
        plugin.saveConfig()
    }
    
    private fun saveDefaultMessages(config: FileConfiguration) {
        if (!config.isSet("messages.header")) {
            config.set("messages.header", deathMessageHeader)
        }
        
        if (!config.isSet("messages.death_point")) {
            config.set("messages.death_point", deathMessage)
        }
        
        if (!config.isSet("messages.footer")) {
            config.set("messages.footer", deathMessageFooter)
        }
        
        if (!config.isSet("messages.teleport_button")) {
            config.set("messages.teleport_button", teleportButtonText)
        }
        
        if (!config.isSet("messages.items_button")) {
            config.set("messages.items_button", itemsButtonText)
        }
        
        if (!config.isSet("messages.teleport_success")) {
            config.set("messages.teleport_success", teleportSuccessMessage)
        }
        
        if (!config.isSet("messages.teleport_fail")) {
            config.set("messages.teleport_fail", teleportFailMessage)
        }
        
        if (!config.isSet("messages.items_return_success")) {
            config.set("messages.items_return_success", itemsReturnSuccessMessage)
        }
        
        if (!config.isSet("messages.items_return_fail")) {
            config.set("messages.items_return_fail", itemsReturnFailMessage)
        }
        
        if (!config.isSet("messages.no_death_points")) {
            config.set("messages.no_death_points", noDeathPointsMessage)
        }
        
        if (!config.isSet("messages.invalid_death_point")) {
            config.set("messages.invalid_death_point", invalidDeathPointMessage)
        }
    }
    
    private fun saveDefaultSounds(config: FileConfiguration) {
        if (!config.isSet("sounds.death.enabled")) {
            config.set("sounds.death.enabled", deathSoundEnabled)
        }
        
        if (!config.isSet("sounds.death.sound")) {
            config.set("sounds.death.sound", deathSound)
        }
        
        if (!config.isSet("sounds.teleport.enabled")) {
            config.set("sounds.teleport.enabled", teleportSoundEnabled)
        }
        
        if (!config.isSet("sounds.teleport.sound")) {
            config.set("sounds.teleport.sound", teleportSound)
        }
        
        if (!config.isSet("sounds.items_return.enabled")) {
            config.set("sounds.items_return.enabled", itemsReturnSoundEnabled)
        }
        
        if (!config.isSet("sounds.items_return.sound")) {
            config.set("sounds.items_return.sound", itemsReturnSound)
        }
    }
    
    fun isWorldEnabled(worldName: String): Boolean {
        if (disabledWorlds.contains(worldName)) {
            return false
        }
        
        return enabledWorlds.contains(worldName) || enabledWorlds.contains("*")
    }
} 