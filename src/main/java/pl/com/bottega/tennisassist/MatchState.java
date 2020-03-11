package pl.com.bottega.tennisassist;

import java.util.Optional;

interface MatchState {
    void registerFirstServingPlayer(Player player);

    void startPlay();

    void registerPoint(Player winningPlayer);

    default Optional<Score<GemPoint>> getCurrentGemScore() {
        return Optional.empty();
    }

    default Optional<Player> getMatchWinner() {
        return Optional.empty();
    }
}
