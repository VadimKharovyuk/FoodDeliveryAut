package com.example.fooddeliveryaut.service;

import com.example.fooddeliveryaut.dto.*;

import java.util.List;

/**
 * 🌍 Сервис для работы с геолокацией пользователей
 *
 * Основные функции:
 * - Управление геолокацией пользователей
 * - Поиск ближайших магазинов
 * - Расчет расстояний и времени доставки
 * - Статистика по геолокации
 */
public interface UserLocationService {

    /**
     * 📍 Обновление координат пользователя
     *
     * @param userId ID пользователя
     * @param locationDto данные для обновления геолокации
     * @return обновленная информация о пользователе
     * @throws RuntimeException если пользователь не найден
     */
    UserResponseDto updateUserLocation(Long userId, UpdateUserLocationDto locationDto);

    /**
     * 🏠 Обновление адреса пользователя с автоматическим геокодированием
     *
     * @param userId ID пользователя
     * @param addressDto данные адреса
     * @return обновленная информация о пользователе
     * @throws RuntimeException если пользователь не найден или геокодирование не удалось
     */
    UserResponseDto updateUserAddress(Long userId, UpdateUserAddressDto addressDto);

    /**
     * 📍 Получение текущей геолокации пользователя
     *
     * @param userId ID пользователя
     * @return информация о геолокации пользователя
     * @throws RuntimeException если пользователь не найден
     */
    UserLocationDto getUserLocation(Long userId);

    /**
     * 🔍 Поиск ближайших магазинов к пользователю
     *
     * @param userId ID пользователя
     * @param searchDto параметры поиска
     * @return список ближайших магазинов с расстояниями
     * @throws RuntimeException если пользователь не найден или у него нет геолокации
     */
    List<NearbyStoreDto> findNearbyStores(Long userId, FindNearbyStoresDto searchDto);

    /**
     * 📏 Расчет расстояния между пользователем и конкретным магазином
     *
     * @param userId ID пользователя
     * @param storeId ID магазина
     * @return информация о расстоянии и времени доставки
     * @throws RuntimeException если пользователь/магазин не найден или нет геолокации
     */
    DistanceCalculationDto calculateDistanceToStore(Long userId, Long storeId);

    /**
     * 📊 Получение статистики по геолокации пользователей (для админов)
     *
     * @return статистика использования геолокации
     */
    UserLocationStatsDto getUserLocationStats();

    /**
     * 🧹 Очистка геолокации пользователя
     *
     * @param userId ID пользователя
     * @return обновленная информация о пользователе
     * @throws RuntimeException если пользователь не найден
     */
    UserResponseDto clearUserLocation(Long userId);
}