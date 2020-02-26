package pl.com.bottega.tennisassist;

import java.util.Optional;

interface MatchState {
    void registerFirstServingPlayer(String player);

    void startPlay();

    void registerPoint(String winningPlayer);

    default Optional<Score<GemPoint>> getCurrentGemScore() {
        return Optional.empty();
    }

    default Optional<String> getMatchWinner() {
        return Optional.empty();
    }
}
