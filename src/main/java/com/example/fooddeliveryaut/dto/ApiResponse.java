package com.example.fooddeliveryaut.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Не включать null поля в JSON
public class ApiResponse<T> {

    /**
     * ✅ Успешность операции
     */
    private Boolean success;

    /**
     * 📝 Сообщение для пользователя
     */
    private String message;

    /**
     * 📊 Данные ответа
     */
    private T data;

    /**
     * ❌ Код ошибки (только при success = false)
     */
    private String errorCode;

    /**
     * 🐛 Детали ошибки для разработчиков (только при success = false)
     */
    private String errorDetails;

    /**
     * 🕐 Время ответа
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * 🔗 Путь API endpoint'а (опционально)
     */
    private String path;

    /**
     * 📊 Метаданные (количество элементов, пагинация и т.д.)
     */
    private ApiMetadata metadata;

    // === СТАТИЧЕСКИЕ МЕТОДЫ ДЛЯ СОЗДАНИЯ ОТВЕТОВ ===

    /**
     * ✅ Создание успешного ответа с данными и сообщением
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * ✅ Создание успешного ответа только с данными
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Операция выполнена успешно");
    }

    /**
     * ✅ Создание успешного ответа только с сообщением (без данных)
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * ✅ Создание успешного ответа с метаданными (для списков)
     */
    public static <T> ApiResponse<T> success(T data, String message, ApiMetadata metadata) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .metadata(metadata)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * ❌ Создание ответа с ошибкой
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * ❌ Создание ответа с ошибкой и кодом ошибки
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * ❌ Создание подробного ответа с ошибкой
     */
    public static <T> ApiResponse<T> error(String message, String errorCode, String errorDetails) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .errorDetails(errorDetails)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * ❌ Создание ответа с ошибкой из исключения
     */
    public static <T> ApiResponse<T> error(String message, Exception exception) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(exception.getClass().getSimpleName())
                .errorDetails(exception.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    // === UTILITY МЕТОДЫ ===

    /**
     * 🔄 Установка пути API endpoint'а
     */
    public ApiResponse<T> withPath(String path) {
        this.path = path;
        return this;
    }

    /**
     * 📊 Установка метаданных
     */
    public ApiResponse<T> withMetadata(ApiMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * 🏷️ Установка дополнительного сообщения
     */
    public ApiResponse<T> withMessage(String additionalMessage) {
        if (this.message != null) {
            this.message = this.message + ". " + additionalMessage;
        } else {
            this.message = additionalMessage;
        }
        return this;
    }

    /**
     * ✅ Проверка успешности ответа
     */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(success);
    }

    /**
     * ❌ Проверка наличия ошибки
     */
    public boolean hasError() {
        return Boolean.FALSE.equals(success);
    }

    /**
     * 📊 Проверка наличия данных
     */
    public boolean hasData() {
        return data != null;
    }

    /**
     * 📊 Проверка наличия метаданных
     */
    public boolean hasMetadata() {
        return metadata != null;
    }
}