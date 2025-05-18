package org.zoobastiks.zrestoreback.utils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

/**
 * Утилитный класс для форматирования сообщений
 */
object FormatUtils {
    private val miniMessage = MiniMessage.miniMessage()
    private val legacySerializer = LegacyComponentSerializer.builder()
        .character('&')
        .hexColors()
        .useUnusualXRepeatedCharacterHexFormat()
        .build()
    
    /**
     * Преобразует строку с различными форматами (MiniMessage, &-коды, §-коды, \n) в компонент Adventure API
     * 
     * @param message Строка с форматированием
     * @return Компонент Adventure API
     */
    fun formatMessage(message: String): Component {
        // Обрабатываем переносы строк
        val withNewLines = message.replace("\\n", "\n")
        
        // Проверяем наличие &-кодов
        if (withNewLines.contains('&') || withNewLines.contains('§')) {
            return legacySerializer.deserialize(withNewLines)
        }
        
        // Если нет &-кодов, пробуем обработать через MiniMessage
        return try {
            miniMessage.deserialize(withNewLines)
        } catch (e: Exception) {
            // Если и это не получилось, просто возвращаем текст как есть
            Component.text(withNewLines)
        }
    }
    
    /**
     * Форматирует сообщение, заменяя в нем плейсхолдеры на значения
     * 
     * @param message Сообщение с плейсхолдерами
     * @param replacements Пары ключ-значение для замены
     * @return Отформатированное сообщение
     */
    fun formatWithPlaceholders(message: String, replacements: Map<String, String>): Component {
        var result = message
        
        // Заменяем все плейсхолдеры
        for ((key, value) in replacements) {
            result = result.replace(key, value)
        }
        
        return formatMessage(result)
    }
} 