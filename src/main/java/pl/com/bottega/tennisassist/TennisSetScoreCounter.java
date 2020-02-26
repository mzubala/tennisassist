package pl.com.bottega.tennisassist;

abstract class TennisSetScoreCounter {

    Score<Integer> score;
    String player1;
    String player2;

    TennisSetScoreCounter(String player1, String player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.score = new Score<>(player1, player2, 0);
    }

    static TennisSetScoreCounter of(String player1, String player2, TieResolutionType finalSetTieResolutionType, boolean lastSet) {
        if(lastSet) {
            switch (finalSetTieResolutionType) {
                case ADVANTAGE:
                    return new AdvantageSetScoreCounter(player1, player2);
                case SUPER_TIEBREAK:
                    return new SuperTiebreakSetScoreCounter(player1, player2);
                case TIEBREAK:
                    return new TiebreakSetScoreCounter(player1, player2);
                default:
                    throw new IllegalStateException();
            }
        } else {
            return new TiebreakSetScoreCounter(player1, player2);
        }
    }

    Score<Integer> getScore() {
        return score;
    }

    void increase(String player) {
        if (isWonInLastGem(player)) {
            throw new IllegalStateException("Set is already finished");
        }
        score = score.withUpdatedScore(player, score.getScore(player) + 1);
    }

    abstract boolean isWonInLastGem(String player);

    Integer getScore(String player) {
        return score.getScore(player);
    }

    abstract boolean needsTiebreak();

    abstract boolean needsSuperTiebreak();
}

class AdvantageSetScoreCounter extends TennisSetScoreCounter {
    AdvantageSetScoreCounter(String player1, String player2) {
        super(player1, player2);
    }

    @Override
    boolean isWonInLastGem(String player) {
        Integer playerScore = score.getScore(player);
        Integer otherPlayerScore = score.getOtherPlayerScore(player);
        return playerScore >= 6 && playerScore - otherPlayerScore >= 2;
    }

    @Override
    boolean needsTiebreak() {
        return false;
    }

    @Override
    boolean needsSuperTiebreak() {
        return false;
    }
}

class TiebreakSetScoreCounter extends TennisSetScoreCounter {

    TiebreakSetScoreCounter(String player1, String player2) {
        super(player1, player2);
    }

    @Override
    boolean isWonInLastGem(String player) {
        Integer playerScore = score.getScore(player);
        Integer otherPlayerScore = score.getOtherPlayerScore(player);
        return playerScore == 6 && playerScore - otherPlayerScore >= 2 || playerScore == 7;
    }

    @Override
    boolean needsTiebreak() {
        return score.getScore(player1) == 6 && score.getScore(player2) == 6;
    }

    @Override
    boolean needsSuperTiebreak() {
        return false;
    }
}

class SuperTiebreakSetScoreCounter extends TennisSetScoreCounter {

    SuperTiebreakSetScoreCounter(String player1, String player2) {
        super(player1, player2);
    }

    @Override
    boolean isWonInLastGem(String player) {
        Integer playerScore = score.getScore(player);
        Integer otherPlayerScore = score.getOtherPlayerScore(player);
        return playerScore == 6 && playerScore - otherPlayerScore >= 2 || playerScore == 7;
    }

    @Override
    boolean needsTiebreak() {
        return false;
    }

    @Override
    boolean needsSuperTiebreak() {
        return score.getScore(player1) == 6 && score.getScore(player2) == 6;
    }
}