package pl.com.bottega.tennisassist;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class TennisMatch {

    private Score<Integer> matchScore;
    private MatchState currentState = new BeforeFirstGem(this);
    private List<Score> scoresOfSets = new LinkedList<>();
    final Players players;
    final MatchSettings matchSettings;
    Player currentlyServingPlayer;
    TennisSetScoreCounter currentSetScoreCounter;

    public TennisMatch(Players players, MatchSettings matchSettings) {
        this.players = players;
        this.matchScore = new Score<>(players, 0);
        this.matchSettings = matchSettings;
    }

    public Score getMatchScore() {
        return matchScore;
    }

    public Optional<Player> getCurrentServingPlayer() {
        return Optional.ofNullable(currentlyServingPlayer);
    }

    public void registerFirstServingPlayer(Player player) {
        this.currentState.registerFirstServingPlayer(player);
    }

    public void startPlay() {
        currentState.startPlay();
    }

    public Optional<Score<GemPoint>> getCurrentGemScore() {
        return currentState.getCurrentGemScore();
    }

    public void registerPoint(Player winningPlayer) {
        currentState.registerPoint(winningPlayer);
    }

    public Optional<Score> getCurrentSetScore() {
        return Optional.ofNullable(currentSetScoreCounter).map(TennisSetScoreCounter::getScore);
    }

    public List<Score> getScoresOfSets() {
        return Collections.unmodifiableList(scoresOfSets);
    }

    public Optional<Player> getMatchWinner() {
        return currentState.getMatchWinner();
    }

    private void finishMatch(Player winner) {
        currentState = new MatchFinished(winner);
        currentSetScoreCounter = null;
    }

    private void startBreak() {
        changeServingPlayer();
        currentState = new BreakInPlay(this);
    }

    void startSuperTiebreak() {
        currentState = new SupertiebreakInPlay(this);
    }

    void startTiebreak() {
        this.currentState = new TiebreakInPlay(this, currentlyServingPlayer);
    }

    void startGem() {
        currentState = new GemInProgress(this);
    }

    void finishGem(Player winningPlayer) {
        currentSetScoreCounter.increase(winningPlayer);
        if (currentSetScoreCounter.isWonInLastGem(winningPlayer)) {
            finishSet(winningPlayer, currentSetScoreCounter.getScore());
        } else {
            startBreak();
        }
    }

    void finishSet(Player winningPlayer, Score<Integer> setScoreToSave) {
        matchScore = matchScore.withUpdatedScore(winningPlayer, matchScore.getScore(winningPlayer) + 1);
        scoresOfSets.add(setScoreToSave);
        currentSetScoreCounter = null;
        if (matchSettings.isMatchFinished(matchScore.getScore(winningPlayer))) {
            finishMatch(winningPlayer);
        } else {
            startBreak();
        }
    }

    private boolean isFinalSet() {
        return matchSettings.isFinalSet(matchScore.getScore(players.getPlayer1()), matchScore.getScore(players.getPlayer2()));
    }

    boolean needsSuperTiebreak() {
        Integer player1MatchScore = matchScore.getScore(players.getPlayer1());
        Integer player2MatchScore = matchScore.getScore(players.getPlayer2());
        return matchSettings.needsSuperTiebreak(player1MatchScore, player2MatchScore) ||
            currentSetScoreCounter.needsSuperTiebreak();
    }

    void ensureSetStarted() {
        if (currentSetScoreCounter == null) {
            startSet();
        }
    }

    private void startSet() {
        currentSetScoreCounter = TennisSetScoreCounter.of(players, matchSettings.getFinalSetTieResolutionType(), isFinalSet());
    }

    void changeServingPlayer() {
        currentlyServingPlayer = currentlyServingPlayer.equals(players.getPlayer1()) ? players.getPlayer2() : players.getPlayer1();
    }

}
