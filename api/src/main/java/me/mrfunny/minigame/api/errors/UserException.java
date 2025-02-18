package me.mrfunny.minigame.api.errors;

public class UserException extends RuntimeException {
    private final String translationFallback;

    public UserException(String message, String translationFallback) {
        super(message);
        this.translationFallback = translationFallback;
    }

    public String getTranslationFallback() {
        return translationFallback;
    }
}
