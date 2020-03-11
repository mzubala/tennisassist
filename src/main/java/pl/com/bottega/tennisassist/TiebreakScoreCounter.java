package pl.com.bottega.tennisassist;

class TiebreakScoreCounter {

    private Score<Integer> score;
    private Integer winningScore;
    private Players players;

    TiebreakScoreCounter(Players players, Integer winningScore) {
        this.players = players;
        score = new Score<>(players, 0);
        this.winningScore = winningScore;
    }

    void increment(Player player) {
        if(isWon(player)) {
            throw new IllegalStateException("Tiebreak finished");
        }
        score = score.withUpdatedScore(player, score.getScore(player) + 1);
    }

    boolean isWon(Player player) {
        return score.getScore(player) >= winningScore && score.getScore(player) - score.getOtherPlayerScore(player) >= 2;
    }

    public boolean shouldChangeServingPlayer() {
        return (score.getScore(players.getPlayer1()) + score.getScore(players.getPlayer2())) % 2 == 1;
    }

    public Score<Integer> getScore() {
        return score;
    }
}
