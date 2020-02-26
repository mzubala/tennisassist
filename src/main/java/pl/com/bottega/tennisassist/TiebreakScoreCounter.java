package pl.com.bottega.tennisassist;

class TiebreakScoreCounter {

    private Score<Integer> score;
    private Integer winningScore;
    private String player1;
    private String player2;

    TiebreakScoreCounter(String player1, String player2, Integer winningScore) {
        this.player1 = player1;
        this.player2 = player2;
        score = new Score<>(player1, player2, 0);
        this.winningScore = winningScore;
    }

    void increment(String player) {
        if(isWon(player)) {
            throw new IllegalStateException("Tiebreak finished");
        }
        score = score.withUpdatedScore(player, score.getScore(player) + 1);
    }

    boolean isWon(String player) {
        return score.getScore(player) >= winningScore && score.getScore(player) - score.getOtherPlayerScore(player) >= 2;
    }

    public boolean shouldChangeServingPlayer() {
        return (score.getScore(player1) + score.getScore(player2)) % 2 == 1;
    }

    public Score<Integer> getScore() {
        return score;
    }
}
