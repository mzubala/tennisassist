package pl.com.bottega.tennisassist;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Score<T> {

    private final Map<String, T> scoreMap;

    Score(String player1, String player2, T zero) {
        this(player1, zero, player2, zero);
    }

    Score(String player1, T player1Score, String player2, T player2Score) {
        scoreMap = new HashMap<>();
        scoreMap.put(player1, player1Score);
        scoreMap.put(player2, player2Score);
    }

    private Score(Map<String, T> scoreMap) {
        this.scoreMap = scoreMap;
    }

    public T getScore(String player) {
        ensurePlayerExists(player);
        return scoreMap.get(player);
    }

    Score<T> withUpdatedScore(String player, T score) {
        ensurePlayerExists(player);
        Map<String, T> newScoreMap = new HashMap<>(scoreMap);
        newScoreMap.put(player, score);
        return new Score<>(newScoreMap);
    }

    T getOtherPlayerScore(String player) {
        ensurePlayerExists(player);
        Set<String> players = new HashSet<>(scoreMap.keySet());
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

    private void ensurePlayerExists(String player) {
        if (!scoreMap.containsKey(player)) {
            throw new IllegalArgumentException("No such player");
        }
    }
}
