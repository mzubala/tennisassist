package pl.com.bottega.tennisassist;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Score {

    private Map<String, Integer> scoreMap = new HashMap<>();

    Score(String player1, String player2) {
        this(player1, 0, player2, 0);
    }

    Score(String player1, int player1Score, String player2, int player2Score) {
        scoreMap.put(player1, player1Score);
        scoreMap.put(player2, player2Score);
    }

    public int scoreOf(String player) {
        return scoreMap.getOrDefault(player, 0);
    }

    public void setScore(String player, int score) {
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
}
