package pl.com.bottega.tennisassist;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Score<T> {

    private Map<String, T> scoreMap = new HashMap<>();

    Score(String player1, String player2, T zero) {
        this(player1, zero, player2, zero);
    }

    Score(String player1, T player1Score, String player2, T player2Score) {
        scoreMap.put(player1, player1Score);
        scoreMap.put(player2, player2Score);
    }

    public T getScore(String player) {
        ensurePlayerExists(player);
        return scoreMap.get(player);
    }

    public void setScore(String player, T score) {
        ensurePlayerExists(player);
        scoreMap.put(player, score);
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
