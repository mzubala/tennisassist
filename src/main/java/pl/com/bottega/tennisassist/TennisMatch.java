package pl.com.bottega.tennisassist;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class TennisMatch {

    private final MatchFormat matchFormat;
    private Score<Integer> matchScore;
    private MatchState currentState = new BeforeFirstGem(this);
    private List<Score> scoresOfSets = new LinkedList<>();
    final String player1;
    final String player2;
    final GemScoringType gemScoringType;
    final TieResolutionType finalSetTieResolutionType;
    String currentlyServingPlayer;
    TennisSetScoreCounter currentSetScoreCounter;

    public TennisMatch(String player1, String player2, MatchFormat matchFormat, GemScoringType gemScoringType, TieResolutionType finalSetTieResolutionType) {
        this.player1 = player1;
        this.player2 = player2;
        this.matchFormat = matchFormat;
        this.gemScoringType = gemScoringType;
        this.finalSetTieResolutionType = finalSetTieResolutionType;
        this.matchScore = new Score<>(player1, player2, 0);
    }

    public Score getMatchScore() {
        return matchScore;
    }

    public Optional<String> getCurrentServingPlayer() {
        return Optional.ofNullable(currentlyServingPlayer);
    }

    public void registerFirstServingPlayer(String player) {
        this.currentState.registerFirstServingPlayer(player);
    }

    public void startPlay() {
        currentState.startPlay();
    }

    public Optional<Score<GemPoint>> getCurrentGemScore() {
        return currentState.getCurrentGemScore();
    }

    public void registerPoint(String winningPlayer) {
        currentState.registerPoint(winningPlayer);
    }

    public Optional<Score> getCurrentSetScore() {
        return Optional.ofNullable(currentSetScoreCounter).map(TennisSetScoreCounter::getScore);
    }

    public List<Score> getScoresOfSets() {
        return Collections.unmodifiableList(scoresOfSets);
    }

    public Optional<String> getMatchWinner() {
        return currentState.getMatchWinner();
    }

    private void finishMatch(String winner) {
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

    void finishGem(String winningPlayer) {
        currentSetScoreCounter.increase(winningPlayer);
        if (currentSetScoreCounter.isWonInLastGem(winningPlayer)) {
            finishSet(winningPlayer, currentSetScoreCounter.getScore());
        } else {
            startBreak();
        }
    }

    void finishSet(String winningPlayer, Score<Integer> setScoreToSave) {
        matchScore = matchScore.withUpdatedScore(winningPlayer, matchScore.getScore(winningPlayer) + 1);
        scoresOfSets.add(setScoreToSave);
        currentSetScoreCounter = null;
        if (matchFormat.isMatchFinished(matchScore.getScore(winningPlayer))) {
            finishMatch(winningPlayer);
        } else {
            startBreak();
        }
    }

    private boolean isFinalSet() {
        return matchFormat.isFinalSet(matchScore.getScore(player1), matchScore.getScore(player2));
    }

    boolean needsSuperTiebreak() {
        Integer player1MatchScore = matchScore.getScore(player1);
        Integer player2MatchScore = matchScore.getScore(player2);
        return matchFormat.needsSuperTiebreak(player1MatchScore, player2MatchScore) ||
            currentSetScoreCounter.needsSuperTiebreak();
    }

    void ensureSetStarted() {
        if (currentSetScoreCounter == null) {
            startSet();
        }
    }

    private void startSet() {
        currentSetScoreCounter = TennisSetScoreCounter.of(player1, player2, finalSetTieResolutionType, isFinalSet());
    }

    void changeServingPlayer() {
        currentlyServingPlayer = currentlyServingPlayer.equals(player1) ? player2 : player1;
    }

}
