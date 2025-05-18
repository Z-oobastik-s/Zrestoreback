package org.zoobastiks.zrestoreback

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import org.zoobastiks.zrestoreback.utils.FormatUtils
import org.zoobastiks.zrestoreback.utils.SoundUtils
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class ZRestoreBack : JavaPlugin(), Listener {
    private val deathLocations = ConcurrentHashMap<UUID, MutableList<DeathData>>()
    private var economy: Economy? = null
    private val mm = MiniMessage.miniMessage()
    private val config = DeathConfig()
    private lateinit var deathDataFile: File
    private lateinit var deathData: YamlConfiguration

    override fun onEnable() {
        // Сохраняем конфиг по умолчанию
        saveDefaultConfig()
        
        // Загружаем настройки
        config.load(this)
        
        // Инициализируем файл данных
        setupDataFile()
        
        // Загружаем сохраненные данные
        loadDeathData()
        
        // Выводим информацию о плагине
        printPluginInfo()
        
        // Инициализация Vault
        if (!setupEconomy()) {
            logger.severe("Vault не найден! Плагин будет отключен.")
            server.pluginManager.disablePlugin(this)
            return
        }
        
        // Регистрация обработчиков событий
        server.pluginManager.registerEvents(this, this)
        
        // Регистрация команд
        getCommand("deathpoints")?.setExecutor(DeathPointsCommand(this))
        
        // Запускаем автосохранение
        setupAutoSave()
        
        Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN.toString() + "Плагин ZRestoreBack успешно включен!")
    }
    
    /**
     * Выводит красивую информацию о плагине в консоль
     */
    private fun printPluginInfo() {
        val pluginInfo = arrayOf(
            "${ChatColor.GOLD}╔══════════════════════════════════════════════╗",
            "${ChatColor.GOLD}║                                              ║",
            "${ChatColor.GOLD}║  ${ChatColor.AQUA}ZRestoreBack ${ChatColor.WHITE}v${description.version}${ChatColor.RED} - Death Teleporter  ${ChatColor.GOLD}║",
            "${ChatColor.GOLD}║  ${ChatColor.WHITE}Author: ${ChatColor.GREEN}Zoobastiks                              ${ChatColor.GOLD}║",
            "${ChatColor.GOLD}║  ${ChatColor.WHITE}Support: ${ChatColor.BLUE}https://t.me/Zoobastiks             ${ChatColor.GOLD}║",
            "${ChatColor.GOLD}║                                              ║",
            "${ChatColor.GOLD}╚══════════════════════════════════════════════╝"
        )
        
        // Используем Bukkit.getConsoleSender().sendMessage() вместо logger.info()
        // для правильного отображения цветных сообщений в консоли
        pluginInfo.forEach { Bukkit.getConsoleSender().sendMessage(it) }
    }

    override fun onDisable() {
        // Сохраняем данные перед выключением
        saveDeathData()
        
        // Используем Bukkit.getConsoleSender().sendMessage() для цветного сообщения
        Bukkit.getConsoleSender().sendMessage(ChatColor.RED.toString() + "Плагин ZRestoreBack выключен!")
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        
        val rsp: RegisteredServiceProvider<Economy> = server.servicesManager.getRegistration(
            Economy::class.java
        ) ?: return false
        
        economy = rsp.provider
        return true
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.player
        val deathLocation = player.location
        val world = deathLocation.world.name
        
        // Проверяем, разрешен ли мир
        if (!config.isWorldEnabled(world)) {
            return
        }
        
        // Сохраняем информацию о смерти
        val deathItems = event.drops.toList()
        val deathData = DeathData(
            location = deathLocation,
            items = deathItems,
            timestamp = System.currentTimeMillis()
        )
        
        // Получаем или создаем список смертей для игрока
        val playerDeaths = deathLocations.computeIfAbsent(player.uniqueId) { mutableListOf() }
        
        // Добавляем новую смерть в начало списка
        playerDeaths.add(0, deathData)
        
        // Ограничиваем количество сохраненных смертей
        if (playerDeaths.size > config.maxDeathsStored) {
            playerDeaths.removeAt(playerDeaths.size - 1)
        }
        
        // Воспроизводим звук смерти
        if (config.deathSoundEnabled) {
            SoundUtils.playSound(player, config.deathSound, 1.0f, 1.0f, this)
        }
        
        // Выполняем команды, настроенные для смерти
        if (config.executeCommandsOnDeath) {
            for (cmd in config.deathCommands) {
                val processedCmd = cmd
                    .replace("%player%", player.name)
                    .replace("%world%", world)
                    .replace("%x%", deathLocation.blockX.toString())
                    .replace("%y%", deathLocation.blockY.toString())
                    .replace("%z%", deathLocation.blockZ.toString())
                
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), processedCmd)
            }
        }
        
        // Отправляем сообщение о смерти
        Bukkit.getScheduler().runTaskLater(this, Runnable {
            sendDeathMessages(player)
        }, 20L)
    }

    fun sendDeathMessages(player: Player) {
        val playerDeaths = deathLocations[player.uniqueId] ?: return
        
        // Отправляем заголовок
        player.sendMessage(FormatUtils.formatMessage(config.deathMessageHeader))
        
        // Ограничиваем количество отображаемых смертей
        val displayCount = Math.min(playerDeaths.size, config.maxDeathsShown)
        
        for (i in 0 until displayCount) {
            val death = playerDeaths[i]
            val deathLocation = death.location
            val world = deathLocation.world.name
            val x = deathLocation.blockX
            val y = deathLocation.blockY
            val z = deathLocation.blockZ
            
            // Создаем сообщение о месте смерти с плейсхолдерами
            val replacements = mapOf(
                "%world%" to world,
                "%x%" to x.toString(),
                "%y%" to y.toString(),
                "%z%" to z.toString(),
                "%index%" to (i + 1).toString()
            )
            
            // Создаем компонент сообщения с форматированием
            val message = FormatUtils.formatWithPlaceholders(config.deathMessage, replacements)
            
            // Создаем кнопки для телепортации и восстановления предметов
            val teleportButton = Component.text()
                .content(config.teleportButtonText)
                .color(NamedTextColor.GREEN)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/deathpoints tp ${player.uniqueId} $i"))
                .build()
            
            val itemsButton = Component.text()
                .content(config.itemsButtonText)
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
                .clickEvent(ClickEvent.runCommand("/deathpoints items ${player.uniqueId} $i"))
                .build()
            
            // Объединяем и отправляем сообщение игроку
            player.sendMessage(message
                .append(Component.space())
                .append(teleportButton)
                .append(Component.space())
                .append(itemsButton)
            )
        }
        
        // Отправляем нижний колонтитул
        player.sendMessage(FormatUtils.formatMessage(config.deathMessageFooter))
    }

    fun getEconomy(): Economy? = economy
    
    fun getDeathData(uuid: UUID): List<DeathData>? = deathLocations[uuid]
    
    /**
     * Возвращает объект конфигурации плагина
     */
    fun getDeathConfig(): DeathConfig {
        return config
    }
    
    /**
     * Перезагружает конфигурацию плагина
     * 
     * @return true если перезагрузка прошла успешно, false в случае ошибки
     */
    fun reloadPluginConfig(): Boolean {
        return try {
            // Перезагружаем конфигурационный файл
            reloadConfig()
            
            // Загружаем настройки
            config.load(this)
            
            // Логируем успешную перезагрузку
            Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN.toString() + "Конфигурация плагина ZRestoreBack успешно перезагружена!")
            true
        } catch (e: Exception) {
            // Логируем ошибку
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED.toString() + "Ошибка при перезагрузке конфигурации: " + e.message)
            false
        }
    }
    
    /**
     * Инициализирует файл данных
     */
    private fun setupDataFile() {
        deathDataFile = File(dataFolder, "death_data.yml")
        
        // Создаем папку плагина, если она не существует
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        
        // Создаем файл данных, если он не существует
        if (!deathDataFile.exists()) {
            try {
                deathDataFile.createNewFile()
            } catch (e: Exception) {
                logger.severe("Не удалось создать файл данных: ${e.message}")
            }
        }
        
        // Загружаем конфигурацию из файла
        deathData = YamlConfiguration.loadConfiguration(deathDataFile)
    }
    
    /**
     * Настраивает автоматическое сохранение данных
     */
    private fun setupAutoSave() {
        val saveInterval = config.autoSaveInterval * 20L * 60 // Переводим минуты в тики
        if (saveInterval > 0) {
            Bukkit.getScheduler().runTaskTimer(this, Runnable {
                saveDeathData()
                if (config.showSaveMessages) {
                    Bukkit.getConsoleSender().sendMessage("${ChatColor.YELLOW}[ZRestoreBack] Данные о точках смерти сохранены.")
                }
            }, saveInterval, saveInterval)
        }
    }
    
    /**
     * Загружает данные о точках смерти из файла
     */
    private fun loadDeathData() {
        try {
            val playersSection = deathData.getConfigurationSection("players") ?: return
            
            for (playerUUID in playersSection.getKeys(false)) {
                val uuid = UUID.fromString(playerUUID)
                val playerDeaths = mutableListOf<DeathData>()
                val deathsSection = playersSection.getConfigurationSection(playerUUID) ?: continue
                
                for (deathIndex in deathsSection.getKeys(false)) {
                    val deathSection = deathsSection.getConfigurationSection(deathIndex) ?: continue
                    
                    // Загружаем местоположение
                    val world = deathSection.getString("world") ?: continue
                    val x = deathSection.getDouble("x")
                    val y = deathSection.getDouble("y")
                    val z = deathSection.getDouble("z")
                    val yaw = deathSection.getDouble("yaw").toFloat()
                    val pitch = deathSection.getDouble("pitch").toFloat()
                    
                    val bukkitWorld = Bukkit.getWorld(world)
                    if (bukkitWorld == null) {
                        logger.warning("Мир $world не найден при загрузке точки смерти для игрока $playerUUID")
                        continue
                    }
                    
                    val location = Location(bukkitWorld, x, y, z, yaw, pitch)
                    
                    // Загружаем предметы
                    val items = mutableListOf<ItemStack>()
                    val itemsSection = deathSection.getConfigurationSection("items")
                    
                    if (itemsSection != null) {
                        for (itemIndex in itemsSection.getKeys(false)) {
                            val item = itemsSection.getItemStack(itemIndex)
                            if (item != null) {
                                items.add(item)
                            }
                        }
                    }
                    
                    // Загружаем время
                    val timestamp = deathSection.getLong("timestamp")
                    
                    // Создаем и добавляем данные о смерти
                    val deathData = DeathData(location, items, timestamp)
                    playerDeaths.add(deathData)
                }
                
                // Сортируем точки смерти по времени (сначала новые)
                playerDeaths.sortByDescending { it.timestamp }
                
                // Добавляем загруженные данные в карту
                if (playerDeaths.isNotEmpty()) {
                    deathLocations[uuid] = playerDeaths
                }
            }
            
            Bukkit.getConsoleSender().sendMessage("${ChatColor.GREEN}[ZRestoreBack] Данные о точках смерти успешно загружены.")
        } catch (e: Exception) {
            logger.severe("Ошибка при загрузке данных о точках смерти: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Сохраняет данные о точках смерти в файл
     */
    private fun saveDeathData() {
        try {
            // Очищаем предыдущие данные
            deathData.set("players", null)
            
            // Создаем секцию для игроков
            val playersSection = deathData.createSection("players")
            
            // Сохраняем данные для каждого игрока
            for ((uuid, deaths) in deathLocations) {
                val playerSection = playersSection.createSection(uuid.toString())
                
                // Сохраняем каждую точку смерти
                for ((index, deathData) in deaths.withIndex()) {
                    val deathSection = playerSection.createSection(index.toString())
                    val location = deathData.location
                    
                    // Сохраняем местоположение
                    deathSection.set("world", location.world.name)
                    deathSection.set("x", location.x)
                    deathSection.set("y", location.y)
                    deathSection.set("z", location.z)
                    deathSection.set("yaw", location.yaw)
                    deathSection.set("pitch", location.pitch)
                    
                    // Сохраняем время
                    deathSection.set("timestamp", deathData.timestamp)
                    
                    // Сохраняем предметы
                    val itemsSection = deathSection.createSection("items")
                    for ((itemIndex, item) in deathData.items.withIndex()) {
                        itemsSection.set(itemIndex.toString(), item)
                    }
                }
            }
            
            // Сохраняем в файл
            deathData.save(deathDataFile)
        } catch (e: Exception) {
            logger.severe("Ошибка при сохранении данных о точках смерти: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Сохраняет данные о смертях вручную
     * 
     * @return true если сохранение прошло успешно, false в случае ошибки
     */
    fun saveDeathDataManually(): Boolean {
        return try {
            saveDeathData()
            true
        } catch (e: Exception) {
            logger.severe("Ошибка при ручном сохранении данных: ${e.message}")
            false
        }
    }
}

data class DeathData(
    val location: Location,
    var items: List<ItemStack>,
    val timestamp: Long
) 