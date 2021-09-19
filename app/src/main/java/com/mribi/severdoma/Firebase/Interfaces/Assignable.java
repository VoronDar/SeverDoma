package com.mribi.severdoma.Firebase.Interfaces;

// Интерфейс для view, которые вызывают вход в аккаунт - здесь функции, которые срабатывают при приходе результата с сервера
public interface Assignable {
    void onSuccess();
    void onFailure();
    void onEmptyFills();
}
