package org.zoobastiks.zrestoreback.utils

import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

/**
 * Утилитный класс для безопасной работы со звуками
 */
object SoundUtils {
    /**
     * Воспроизводит звук для игрока с обработкой ошибок
     *
     * @param player Игрок, для которого воспроизводится звук
     * @param soundName Название звука
     * @param volume Громкость звука (по умолчанию 1.0)
     * @param pitch Высота звука (по умолчанию 1.0)
     * @param plugin Экземпляр плагина для логгирования ошибок
     * @return true если звук успешно воспроизведен, false в случае ошибки
     */
    fun playSound(player: Player, soundName: String, volume: Float = 1.0f, pitch: Float = 1.0f, plugin: JavaPlugin): Boolean {
        return try {
            // Преобразуем название звука в правильный формат для Minecraft 1.21.4 (lowercase)
            val formattedSound = formatSoundName(soundName)
            player.playSound(player.location, formattedSound, volume, pitch)
            true
        } catch (e: Exception) {
            plugin.logger.warning("Ошибка при воспроизведении звука $soundName: ${e.message}")
            
            // Пробуем воспроизвести звук через enum для обратной совместимости
            try {
                val enumSound = Sound.valueOf(soundName)
                player.playSound(player.location, enumSound, volume, pitch)
                return true
            } catch (e: Exception) {
                plugin.logger.warning("Также не удалось воспроизвести звук через enum: ${e.message}")
                false
            }
        }
    }
    
    /**
     * Воспроизводит звук в указанной локации
     *
     * @param location Локация для воспроизведения звука
     * @param soundName Название звука
     * @param volume Громкость звука
     * @param pitch Высота звука
     * @param plugin Экземпляр плагина для логгирования ошибок
     * @return true если звук успешно воспроизведен, false в случае ошибки
     */
    fun playSound(location: Location, soundName: String, volume: Float = 1.0f, pitch: Float = 1.0f, plugin: JavaPlugin): Boolean {
        return try {
            // Преобразуем название звука в правильный формат для Minecraft 1.21.4 (lowercase)
            val formattedSound = formatSoundName(soundName)
            val world = location.world
            world.playSound(location, formattedSound, volume, pitch)
            true
        } catch (e: Exception) {
            plugin.logger.warning("Ошибка при воспроизведении звука $soundName: ${e.message}")
            
            // Пробуем воспроизвести звук через enum для обратной совместимости
            try {
                val enumSound = Sound.valueOf(soundName)
                location.world.playSound(location, enumSound, volume, pitch)
                return true
            } catch (e: Exception) {
                plugin.logger.warning("Также не удалось воспроизвести звук через enum: ${e.message}")
                false
            }
        }
    }
    
    /**
     * Форматирует название звука в формат, подходящий для Minecraft 1.21.4
     * 
     * @param soundName Название звука в формате ENTITY_PLAYER_DEATH
     * @return Отформатированное название звука entity.player.death
     */
    private fun formatSoundName(soundName: String): String {
        // Если звук уже в нижнем регистре и содержит '.', считаем что он уже в правильном формате
        if (soundName.lowercase() == soundName && soundName.contains('.')) {
            return soundName
        }
        
        // ENTITY_PLAYER_DEATH -> entity.player.death
        return when {
            soundName.contains("_") -> {
                soundName.lowercase().replace("_", ".")
            }
            soundName.contains(".") -> {
                // Уже может быть в формате entity.player.death
                soundName.lowercase()
            }
            else -> {
                // minecraft:название
                if (!soundName.contains(":")) {
                    "minecraft:$soundName".lowercase()
                } else {
                    soundName.lowercase()
                }
            }
        }
    }
} 