package pl.com.bottega.tennisassist;

abstract class TennisSetScoreCounter {

    Score<Integer> score;
    Players players;

    TennisSetScoreCounter(Players players) {
        this.players = players;
        this.score = new Score<>(players, 0);
    }

    static TennisSetScoreCounter of(Players players, TieResolutionType finalSetTieResolutionType, boolean lastSet) {
        if(lastSet) {
            switch (finalSetTieResolutionType) {
                case ADVANTAGE:
                    return new AdvantageSetScoreCounter(players);
                case SUPER_TIEBREAK:
                    return new SuperTiebreakSetScoreCounter(players);
                case TIEBREAK:
                    return new TiebreakSetScoreCounter(players);
                default:
                    throw new IllegalStateException();
            }
        } else {
            return new TiebreakSetScoreCounter(players);
        }
    }

    Score<Integer> getScore() {
        return score;
    }

    void increase(Player player) {
        if (isWonInLastGem(player)) {
            throw new IllegalStateException("Set is already finished");
        }
        score = score.withUpdatedScore(player, score.getScore(player) + 1);
    }

    abstract boolean isWonInLastGem(Player player);

    Integer getScore(Player player) {
        return score.getScore(player);
    }

    abstract boolean needsTiebreak();

    abstract boolean needsSuperTiebreak();

    private static class AdvantageSetScoreCounter extends TennisSetScoreCounter {
        AdvantageSetScoreCounter(Players players) {
            super(players);
        }

        @Override
        boolean isWonInLastGem(Player player) {
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

    private static class TiebreakSetScoreCounter extends TennisSetScoreCounter {

        TiebreakSetScoreCounter(Players players) {
            super(players);
        }

        @Override
        boolean isWonInLastGem(Player player) {
            Integer playerScore = score.getScore(player);
            Integer otherPlayerScore = score.getOtherPlayerScore(player);
            return playerScore == 6 && playerScore - otherPlayerScore >= 2 || playerScore == 7;
        }

        @Override
        boolean needsTiebreak() {
            return score.getScore(players.getPlayer1()) == 6 && score.getScore(players.getPlayer2()) == 6;
        }

        @Override
        boolean needsSuperTiebreak() {
            return false;
        }
    }

    private static class SuperTiebreakSetScoreCounter extends TennisSetScoreCounter {

        SuperTiebreakSetScoreCounter(Players players) {
            super(players);
        }

        @Override
        boolean isWonInLastGem(Player player) {
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
            return score.getScore(players.getPlayer1()) == 6 && score.getScore(players.getPlayer2()) == 6;
        }
    }
}