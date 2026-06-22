package com.todaypoor.auth.repository;

import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRefreshTokenRepository implements RefreshTokenRepository {

    private final ConcurrentHashMap<UUID, String> store = new ConcurrentHashMap<>();

    @Override
    public void save(UUID userId, String refreshToken, long expirationTimeMs) {
        store.put(userId, refreshToken);
    }

    @Override
    public String findByUserId(UUID userId) {
        return store.get(userId);
    }

    @Override
    public void deleteByUserId(UUID userId) {
        store.remove(userId);
    }
}
