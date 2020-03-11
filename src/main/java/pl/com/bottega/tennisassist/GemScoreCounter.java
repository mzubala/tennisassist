package pl.com.bottega.tennisassist;

import static pl.com.bottega.tennisassist.GemPoint.ADVANTAGE;
import static pl.com.bottega.tennisassist.GemPoint.FOURTY;
import static pl.com.bottega.tennisassist.GemPoint.GEM;
import static pl.com.bottega.tennisassist.GemPoint.ZERO;

abstract class GemScoreCounter {

    Score<GemPoint> score;

    GemScoreCounter(Players players) {
        score = new Score<>(players, ZERO);
    }

    static GemScoreCounter of(Players players, GemScoringType gemScoringType) {
        return gemScoringType == GemScoringType.WITH_ADVANTAGE ? new AdvantageGemScoreCounter(players) : new GoldenBallGemScoreCounter(players);
    }

    abstract void increment(Player player);

    boolean hasWon(Player player) {
        return score.getScore(player) == GEM;
    }

    Score<GemPoint> getScore() {
        return score;
    }

    void setIncremented(Player player, GemPoint currentPoints) {
        score = score.withUpdatedScore(player, GemPoint.values()[currentPoints.ordinal() + 1]);
    }
}

class AdvantageGemScoreCounter extends GemScoreCounter {

    private final Players players;

    AdvantageGemScoreCounter(Players players) {
        super(players);
        this.players = players;
    }

    @Override
    void increment(Player player) {
        GemPoint playerPoints = score.getScore(player);
        if (playerPoints == GEM) {
            throw new IllegalStateException("Cannot increment. The gem has finished.");
        }
        GemPoint otherPlayerPoints = score.getOtherPlayerScore(player);
        if (otherPlayerPoints == ADVANTAGE) {
            setDeuce();
        } else if (playerPoints == FOURTY && otherPlayerPoints != FOURTY) {
            score = score.withUpdatedScore(player, GEM);
        } else {
            setIncremented(player, playerPoints);
        }
    }

    private void setDeuce() {
        score = new Score<>(players.getPlayer1(), FOURTY, players.getPlayer2(), FOURTY);
    }
}

class GoldenBallGemScoreCounter extends GemScoreCounter {

    GoldenBallGemScoreCounter(Players players) {
        super(players);
    }

    @Override
    void increment(Player player) {
        GemPoint playerPoints = score.getScore(player);
        if (playerPoints == GEM) {
            throw new IllegalStateException("Cannot increment. The gem has finished.");
        }
        GemPoint otherPlayerPoints = score.getOtherPlayerScore(player);
        if (playerPoints == FOURTY) {
            score = score.withUpdatedScore(player, GEM);
        } else {
            setIncremented(player, playerPoints);
        }
    }
}