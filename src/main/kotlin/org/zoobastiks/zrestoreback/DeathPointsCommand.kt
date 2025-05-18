package org.zoobastiks.zrestoreback

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.zoobastiks.zrestoreback.utils.FormatUtils
import org.zoobastiks.zrestoreback.utils.SoundUtils
import java.util.*
import org.bukkit.ChatColor
import net.kyori.adventure.text.event.ClickEvent

class DeathPointsCommand(private val plugin: ZRestoreBack) : CommandExecutor, TabCompleter {
    private val mm = MiniMessage.miniMessage()
    
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player && (args.isEmpty() || (args[0] != "reload" && args[0] != "save" && args[0] != "check"))) {
            sender.sendMessage("${ChatColor.RED}Эта команда может быть использована только игроком или консолью с аргументом reload/save/check.")
            return true
        }
        
        if (args.isEmpty()) {
            showHelp(sender)
            return true
        }
        
        when (args[0].lowercase()) {
            "tp", "teleport" -> {
                if (sender !is Player) {
                    sender.sendMessage("${ChatColor.RED}Эта команда может быть использована только игроком.")
                    return true
                }
                
                if (args.size == 1) {
                    plugin.sendDeathMessages(sender)
                    return true
                } else if (args.size == 2) {
                    try {
                        val index = args[1].toInt() - 1
                        if (index < 0) {
                            sender.sendMessage(FormatUtils.formatMessage("&cИндекс должен быть положительным числом."))
                            return true
                        }
                        
                        handleTeleport(sender, arrayOf("tp", sender.uniqueId.toString(), index.toString()))
                    } catch (e: NumberFormatException) {
                        sender.sendMessage(FormatUtils.formatMessage("&cНеверный формат индекса. Используйте число."))
                    }
                    return true
                }
                
                handleTeleport(sender, args)
                return true
            }
            
            "items", "restore" -> {
                if (sender !is Player) {
                    sender.sendMessage("${ChatColor.RED}Эта команда может быть использована только игроком.")
                    return true
                }
                
                if (args.size == 1) {
                    plugin.sendDeathMessages(sender)
                    return true
                } else if (args.size == 2) {
                    try {
                        val index = args[1].toInt() - 1
                        if (index < 0) {
                            sender.sendMessage(FormatUtils.formatMessage("&cИндекс должен быть положительным числом."))
                            return true
                        }
                        
                        handleItemsRestore(sender, arrayOf("items", sender.uniqueId.toString(), index.toString()))
                    } catch (e: NumberFormatException) {
                        sender.sendMessage(FormatUtils.formatMessage("&cНеверный формат индекса. Используйте число."))
                    }
                    return true
                }
                
                handleItemsRestore(sender, args)
                return true
            }
            
            "list" -> {
                if (sender !is Player) {
                    sender.sendMessage("${ChatColor.RED}Эта команда может быть использована только игроком.")
                    return true
                }
                plugin.sendDeathMessages(sender)
                return true
            }
            
            "help" -> {
                showHelp(sender)
                return true
            }
            
            "reload" -> {
                if (!sender.hasPermission("zrestoreback.admin") && !sender.hasPermission("zrestoreback.reload")) {
                    sender.sendMessage("${ChatColor.RED}У вас нет прав для выполнения этой команды.")
                    return true
                }
                
                if (plugin.reloadPluginConfig()) {
                    sender.sendMessage("${ChatColor.GREEN}Конфигурация успешно перезагружена!")
                } else {
                    sender.sendMessage("${ChatColor.RED}Произошла ошибка при перезагрузке конфигурации.")
                }
                return true
            }
            
            "save" -> {
                if (!sender.hasPermission("zrestoreback.admin") && !sender.hasPermission("zrestoreback.save")) {
                    sender.sendMessage("${ChatColor.RED}У вас нет прав для выполнения этой команды.")
                    return true
                }
                
                if (plugin.saveDeathDataManually()) {
                    sender.sendMessage("${ChatColor.GREEN}Данные о точках смерти успешно сохранены!")
                } else {
                    sender.sendMessage("${ChatColor.RED}Произошла ошибка при сохранении данных.")
                }
                return true
            }
            
            "check" -> {
                if (!sender.hasPermission("zrestoreback.admin") && !sender.hasPermission("zrestoreback.check")) {
                    sender.sendMessage("${ChatColor.RED}У вас нет прав для выполнения этой команды.")
                    return true
                }
                
                if (args.size < 2) {
                    sender.sendMessage("${ChatColor.RED}Пожалуйста, укажите имя игрока: /dp check <имя_игрока>")
                    return true
                }
                
                val targetName = args[1]
                val targetPlayer = Bukkit.getOfflinePlayer(targetName)
                
                if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
                    sender.sendMessage("${ChatColor.RED}Игрок с именем ${ChatColor.YELLOW}$targetName${ChatColor.RED} не найден или никогда не играл на сервере.")
                    return true
                }
                
                val deathLocations = plugin.getDeathData(targetPlayer.uniqueId)
                
                if (deathLocations == null || deathLocations.isEmpty()) {
                    sender.sendMessage("${ChatColor.RED}У игрока ${ChatColor.YELLOW}$targetName${ChatColor.RED} нет сохраненных точек смерти.")
                    return true
                }
                
                sender.sendMessage("${ChatColor.GOLD}${ChatColor.BOLD}Точки смерти игрока ${ChatColor.YELLOW}$targetName${ChatColor.GOLD}:")
                
                val config = plugin.getDeathConfig()
                val maxDeathsShown = config.maxDeathsShown
                val limitedLocations = if (deathLocations.size > maxDeathsShown) {
                    deathLocations.subList(0, maxDeathsShown)
                } else {
                    deathLocations
                }
                
                limitedLocations.forEachIndexed { index, deathData ->
                    val location = deathData.location
                    val timestamp = Date(deathData.timestamp).toString()
                    val itemsCount = deathData.items.size
                    
                    val message = "${ChatColor.AQUA}#${index + 1}${ChatColor.WHITE}: " +
                            "${ChatColor.YELLOW}${location.world.name}${ChatColor.WHITE} " +
                            "(${ChatColor.GREEN}${location.blockX}${ChatColor.WHITE}, " +
                            "${ChatColor.GREEN}${location.blockY}${ChatColor.WHITE}, " +
                            "${ChatColor.GREEN}${location.blockZ}${ChatColor.WHITE}) " +
                            "${ChatColor.GRAY}• $timestamp • ${ChatColor.YELLOW}$itemsCount предметов"
                    
                    sender.sendMessage(message)
                    
                    if (sender is Player) {
                        val teleportMessage = "${ChatColor.GREEN}[ТП]"
                        val viewItemsMessage = "${ChatColor.LIGHT_PURPLE}[Вещи]"
                        
                        val teleportCommand = "/dp tp ${targetPlayer.uniqueId} $index"
                        val itemsCommand = "/dp items ${targetPlayer.uniqueId} $index"
                        
                        val component = Component.text("   ")
                            .append(Component.text(teleportMessage)
                                .clickEvent(ClickEvent.runCommand(teleportCommand))
                                .hoverEvent(Component.text("Телепортироваться к месту смерти").asHoverEvent()))
                            .append(Component.text(" "))
                            .append(Component.text(viewItemsMessage)
                                .clickEvent(ClickEvent.runCommand(itemsCommand))
                                .hoverEvent(Component.text("Восстановить предметы").asHoverEvent()))
                        
                        sender.sendMessage(component)
                    }
                }
                
                if (deathLocations.size > maxDeathsShown) {
                    sender.sendMessage("${ChatColor.GRAY}Показаны ${maxDeathsShown} из ${deathLocations.size} точек смерти.")
                }
                
                return true
            }
            
            else -> {
                try {
                    val index = args[0].toInt() - 1
                    if (index >= 0) {
                        if (sender is Player) {
                            handleTeleport(sender, arrayOf("tp", sender.uniqueId.toString(), index.toString()))
                            return true
                        } else {
                            sender.sendMessage("${ChatColor.RED}Эта команда может быть использована только игроком.")
                            return true
                        }
                    }
                } catch (e: NumberFormatException) {
                    sender.sendMessage(FormatUtils.formatMessage("&cНеизвестная команда. Используйте: /deathpoints [list|tp|items|help]"))
                }
            }
        }
        
        return true
    }
    
    private fun showHelp(sender: CommandSender) {
        val messages = listOf(
            "${ChatColor.GOLD}${ChatColor.BOLD}ZRestoreBack - Команды:${ChatColor.RESET}",
            "${ChatColor.GREEN}/dp list ${ChatColor.WHITE}- Показать список точек смерти",
            "${ChatColor.GREEN}/dp tp <номер> ${ChatColor.WHITE}- Телепортироваться на точку смерти",
            "${ChatColor.GREEN}/dp <номер> ${ChatColor.WHITE}- Телепортироваться на точку смерти (сокращенная версия)"
        )
        
        val adminMessages = listOf(
            "${ChatColor.RED}${ChatColor.BOLD}Административные команды:${ChatColor.RESET}",
            "${ChatColor.RED}/dp reload ${ChatColor.WHITE}- Перезагрузить конфигурацию плагина",
            "${ChatColor.RED}/dp save ${ChatColor.WHITE}- Сохранить данные о точках смерти",
            "${ChatColor.RED}/dp check <игрок> ${ChatColor.WHITE}- Показать точки смерти указанного игрока"
        )
        
        messages.forEach { sender.sendMessage(it) }
        
        if (sender.hasPermission("zrestoreback.admin") || sender.hasPermission("zrestoreback.reload") 
            || sender.hasPermission("zrestoreback.save") || sender.hasPermission("zrestoreback.check")) {
            adminMessages.forEach { sender.sendMessage(it) }
        }
    }
    
    private fun handleTeleport(player: Player, args: Array<out String>) {
        val config = plugin.getDeathConfig()
        val economy = plugin.getEconomy() ?: run {
            player.sendMessage(FormatUtils.formatMessage("&cЭкономика не настроена. Обратитесь к администратору."))
            return
        }
        
        try {
            val uuid: UUID
            val index: Int
            
            if (args.size >= 3) {
                try {
                    uuid = UUID.fromString(args[1])
                    index = args[2].toInt()
                } catch (e: IllegalArgumentException) {
                    player.sendMessage(FormatUtils.formatMessage(config.invalidDeathPointMessage))
                    return
                }
            } else if (args.size == 2) {
                uuid = player.uniqueId
                try {
                    index = args[1].toInt()
                } catch (e: NumberFormatException) {
                    player.sendMessage(FormatUtils.formatMessage("&cНеверный формат номера точки смерти."))
                    return
                }
            } else {
                player.sendMessage(FormatUtils.formatMessage("&cУкажите номер точки смерти для телепортации."))
                return
            }
            
            // Проверяем права доступа к чужим точкам смерти
            if (player.uniqueId != uuid && !player.hasPermission("zrestoreback.admin")) {
                player.sendMessage(FormatUtils.formatMessage(config.invalidDeathPointMessage))
                return
            }
            
            val deathData = plugin.getDeathData(uuid) ?: run {
                player.sendMessage(FormatUtils.formatMessage(config.noDeathPointsMessage))
                return
            }
            
            if (index < 0 || index >= deathData.size) {
                player.sendMessage(FormatUtils.formatMessage(config.invalidDeathPointMessage))
                return
            }
            
            // Если это чужая точка смерти и игрок - администратор, не берем плату
            val isAdmin = player.hasPermission("zrestoreback.admin") && player.uniqueId != uuid
            
            // Проверяем, достаточно ли у игрока денег (если он не администратор)
            if (!isAdmin && !economy.has(player, config.teleportCost)) {
                val replacements = mapOf("%cost%" to config.teleportCost.toString())
                player.sendMessage(FormatUtils.formatWithPlaceholders(config.insufficientFundsTeleportMessage, replacements))
                return
            }
            
            // Снимаем деньги, если это не администратор
            if (!isAdmin) {
                economy.withdrawPlayer(player, config.teleportCost)
            }
            
            // Телепортируем игрока
            val deathLocation = deathData[index].location
            player.teleport(deathLocation)
            
            // Отправляем сообщение об успешной телепортации
            if (!isAdmin) {
                val replacements = mapOf("%cost%" to config.teleportCost.toString())
                player.sendMessage(FormatUtils.formatWithPlaceholders(config.teleportSuccessMessage, replacements))
            } else {
                player.sendMessage(FormatUtils.formatMessage("&aВы были телепортированы на место смерти игрока (режим администратора)."))
            }
            
            // Воспроизводим звук телепортации
            if (config.teleportSoundEnabled) {
                SoundUtils.playSound(player, config.teleportSound, 1.0f, 1.0f, plugin)
            }
        } catch (e: Exception) {
            player.sendMessage(FormatUtils.formatMessage("&cПроизошла ошибка при телепортации: ${e.message}"))
            plugin.logger.severe("Ошибка при телепортации: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun handleItemsRestore(player: Player, args: Array<out String>) {
        val config = plugin.getDeathConfig()
        val economy = plugin.getEconomy() ?: run {
            player.sendMessage(FormatUtils.formatMessage("&cЭкономика не настроена. Обратитесь к администратору."))
            return
        }
        
        try {
            val uuid: UUID
            val index: Int
            
            if (args.size >= 3) {
                try {
                    uuid = UUID.fromString(args[1])
                    index = args[2].toInt()
                } catch (e: IllegalArgumentException) {
                    player.sendMessage(FormatUtils.formatMessage(config.invalidDeathPointMessage))
                    return
                }
            } else if (args.size == 2) {
                uuid = player.uniqueId
                try {
                    index = args[1].toInt()
                } catch (e: NumberFormatException) {
                    player.sendMessage(FormatUtils.formatMessage("&cНеверный формат номера точки смерти."))
                    return
                }
            } else {
                player.sendMessage(FormatUtils.formatMessage("&cУкажите номер точки смерти для восстановления предметов."))
                return
            }
            
            // Проверяем права доступа к чужим точкам смерти
            if (player.uniqueId != uuid && !player.hasPermission("zrestoreback.admin")) {
                player.sendMessage(FormatUtils.formatMessage(config.invalidDeathPointMessage))
                return
            }
            
            val deathData = plugin.getDeathData(uuid) ?: run {
                player.sendMessage(FormatUtils.formatMessage(config.noDeathPointsMessage))
                return
            }
            
            if (index < 0 || index >= deathData.size) {
                player.sendMessage(FormatUtils.formatMessage(config.invalidDeathPointMessage))
                return
            }
            
            // Получаем предметы из точки смерти
            val items = deathData[index].items
            
            // Проверяем, есть ли предметы для восстановления
            if (items.isEmpty()) {
                player.sendMessage(FormatUtils.formatMessage("&cВ этой точке смерти нет предметов для восстановления."))
                return
            }
            
            // Если это чужая точка смерти и игрок - администратор, не берем плату
            val isAdmin = player.hasPermission("zrestoreback.admin") && player.uniqueId != uuid
            
            // Проверяем, достаточно ли у игрока денег (если он не администратор)
            if (!isAdmin && !economy.has(player, config.itemsReturnCost)) {
                val replacements = mapOf("%cost%" to config.itemsReturnCost.toString())
                player.sendMessage(FormatUtils.formatWithPlaceholders(config.insufficientFundsItemsMessage, replacements))
                return
            }
            
            // Снимаем деньги, если это не администратор
            if (!isAdmin) {
                economy.withdrawPlayer(player, config.itemsReturnCost)
            }
            
            // Возвращаем предметы
            val remainingItems = giveItemsToPlayer(player, items)
            
            // Если не все предметы поместились в инвентарь, сбрасываем оставшиеся на землю
            if (remainingItems.isNotEmpty()) {
                for (item in remainingItems) {
                    player.world.dropItemNaturally(player.location, item)
                }
                player.sendMessage(FormatUtils.formatMessage("&eНе все предметы поместились в инвентарь. Оставшиеся были сброшены на землю."))
            }
            
            // Отправляем сообщение об успешном восстановлении предметов
            if (!isAdmin) {
                val replacements = mapOf("%cost%" to config.itemsReturnCost.toString())
                player.sendMessage(FormatUtils.formatWithPlaceholders(config.itemsReturnSuccessMessage, replacements))
            } else {
                player.sendMessage(FormatUtils.formatMessage("&aПредметы были восстановлены (режим администратора)."))
            }
            
            // Воспроизводим звук восстановления предметов
            if (config.itemsReturnSoundEnabled) {
                SoundUtils.playSound(player, config.itemsReturnSound, 1.0f, 1.0f, plugin)
            }
            
            // Очищаем список предметов в точке смерти, чтобы их нельзя было восстановить повторно
            deathData[index].items = emptyList()
        } catch (e: Exception) {
            player.sendMessage(FormatUtils.formatMessage("&cПроизошла ошибка при восстановлении предметов: ${e.message}"))
            plugin.logger.severe("Ошибка при восстановлении предметов: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun giveItemsToPlayer(player: Player, items: List<ItemStack>): List<ItemStack> {
        val remainingItems = mutableListOf<ItemStack>()
        
        for (item in items) {
            val notFittedItems = player.inventory.addItem(item)
            remainingItems.addAll(notFittedItems.values)
        }
        
        return remainingItems
    }
    
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): List<String>? {
        val completions = mutableListOf<String>()
        
        if (args.size == 1) {
            completions.add("list")
            completions.add("tp")
            completions.add("items")
            completions.add("help")
            
            if (sender.hasPermission("zrestoreback.admin") || sender.hasPermission("zrestoreback.reload")) {
                completions.add("reload")
            }
            
            if (sender.hasPermission("zrestoreback.admin") || sender.hasPermission("zrestoreback.save")) {
                completions.add("save")
            }
            
            if (sender.hasPermission("zrestoreback.admin") || sender.hasPermission("zrestoreback.check")) {
                completions.add("check")
            }
            
            if (sender is Player) {
                val deathPoints = plugin.getDeathData(sender.uniqueId)
                val deathCount = deathPoints?.size ?: 0
                
                for (i in 1..deathCount) {
                    completions.add(i.toString())
                }
            }
            
            return completions.filter { it.startsWith(args[0].lowercase()) }
        }
        
        if (args.size == 2) {
            when (args[0].lowercase()) {
                "tp", "teleport", "items", "restore" -> {
                    if (sender is Player) {
                        val indices = mutableListOf<String>()
                        val deathPoints = plugin.getDeathData(sender.uniqueId)
                        val deathCount = deathPoints?.size ?: 0
                        for (i in 1..deathCount) {
                            indices.add(i.toString())
                        }
                        return indices.filter { it.startsWith(args[1]) }
                    }
                }
            }
        } else if (args.size == 2 && args[0].equals("check", ignoreCase = true)) {
            if (sender.hasPermission("zrestoreback.admin") || sender.hasPermission("zrestoreback.check")) {
                val onlinePlayers = Bukkit.getOnlinePlayers()
                for (player in onlinePlayers) {
                    completions.add(player.name)
                }
            }
        }
        
        return completions
    }
} 