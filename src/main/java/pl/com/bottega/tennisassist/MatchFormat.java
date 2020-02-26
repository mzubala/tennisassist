package pl.com.bottega.tennisassist;

public enum MatchFormat {

    BEST_OF_THREE {
        @Override
        boolean isMatchFinished(Integer winningPlayerScore) {
            return winningPlayerScore == 2;
        }

        @Override
        boolean needsSuperTiebreak(Integer player1SetScore, Integer player2SetScore) {
            return false;
        }

        @Override
        boolean isFinalSet(Integer player1SetScore, Integer player2SetScore) {
            return player1SetScore == 1 && player2SetScore == 1;
        }
    },

    BEST_OF_FIVE {
        @Override
        boolean isMatchFinished(Integer winningPlayerScore) {
            return winningPlayerScore == 3;
        }

        @Override
        boolean needsSuperTiebreak(Integer player1SetScore, Integer player2SetScore) {
            return false;
        }

        @Override
        boolean isFinalSet(Integer player1SetScore, Integer player2SetScore) {
            return player1SetScore == 2 && player2SetScore == 2;
        }
    },

    BEST_OF_TWO_WITH_SUPER_TIEBREAK {
        @Override
        boolean isMatchFinished(Integer winningPlayerScore) {
            return winningPlayerScore == 2;
        }

        @Override
        boolean needsSuperTiebreak(Integer player1SetScore, Integer player2SetScore) {
            return  player1SetScore == 1 && player2SetScore == 1;
        }

        @Override
        boolean isFinalSet(Integer player1SetScore, Integer player2SetScore) {
            return false;
        }
    };

    abstract boolean isMatchFinished(Integer winningPlayerScore);

    abstract boolean needsSuperTiebreak(Integer player1SetScore, Integer player2SetScore);

    abstract boolean isFinalSet(Integer player1SetScore, Integer player2SetScore);

}
