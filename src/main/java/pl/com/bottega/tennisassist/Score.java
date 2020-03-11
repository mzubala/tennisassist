package pl.com.bottega.tennisassist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Score<T> {

    private final Map<Player, T> scoreMap;

    Score(Players players, T zero) {
        this(players.getPlayer1(), zero, players.getPlayer2(), zero);
    }

    Score(Player player1, T player1Score, Player player2, T player2Score) {
        scoreMap = new HashMap<>();
        scoreMap.put(player1, player1Score);
        scoreMap.put(player2, player2Score);
    }

    private Score(Map<Player, T> scoreMap) {
        this.scoreMap = scoreMap;
    }

    public T getScore(Player player) {
        ensurePlayerExists(player);
        return scoreMap.get(player);
    }

    Score<T> withUpdatedScore(Player player, T score) {
        ensurePlayerExists(player);
        Map<Player, T> newScoreMap = new HashMap<>(scoreMap);
        newScoreMap.put(player, score);
        return new Score<>(newScoreMap);
    }

    T getOtherPlayerScore(Player player) {
        ensurePlayerExists(player);
        Set<Player> players = new HashSet<>(scoreMap.keySet());
        players.remove(player);
        return getScore(players.iterator().next());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score = (Score) o;
        return scoreMap.equals(score.scoreMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scoreMap);
    }

    private void ensurePlayerExists(Player player) {
        if (!scoreMap.containsKey(player)) {
            throw new IllegalArgumentException("No such player");
        }
    }
}
