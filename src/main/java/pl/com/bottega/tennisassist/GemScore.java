package pl.com.bottega.tennisassist;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static pl.com.bottega.tennisassist.GemPoint.*;

public class GemScore {

    private final Map<String, GemPoint> pointMap = new HashMap<>();

    GemScore(String player1, String player2) {
        this(player1, ZERO, player2, ZERO);
    }

    GemScore(String player1, GemPoint player1Score, String player2, GemPoint player2Score) {
        pointMap.put(player1, player1Score);
        pointMap.put(player2, player2Score);
    }

    public GemPoint scoreOf(String player) {
        return pointMap.get(player);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GemScore gemScore = (GemScore) o;
        return pointMap.equals(gemScore.pointMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointMap);
    }

    public void setScore(String player, GemPoint value) {
        pointMap.put(player, value);
    }
}

enum GemPoint {
    ZERO,
    FIFTEEN,
    THIRTY,
    FOURTY,
    ADVANTAGE
}
