package pl.com.bottega.tennisassist;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static pl.com.bottega.tennisassist.TieResolutionType.SUPER_TIEBREAK;

public class TennisMatch {

    private final String player1;
    private final String player2;
    private final MatchFormat matchFormat;
    private final GemScoringType gemScoringType;
    private final TieResolutionType finalSetTieResolutionType;
    private Score<Integer> matchScore;
    private MatchState currentState = new BeforeFirstGem();
    private String currentlyServingPlayer;
    private TennisSetScoreCounter currentSetScoreCounter;
    private List<Score> scoresOfSets = new LinkedList<>();

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
        currentState = new BreakInPlay();
    }

    private void startSuperTiebreak() {
        currentState = new SupertiebreakInPlay();
    }

    private void startTiebreak() {
        this.currentState = new TiebreakInPlay(currentlyServingPlayer);
    }

    private void startGem() {
        currentState = new GemInProgress();
    }

    private void finishGem(String winningPlayer) {
        currentSetScoreCounter.increase(winningPlayer);
        if (currentSetScoreCounter.isWonInLastGem(winningPlayer)) {
            finishSet(winningPlayer, currentSetScoreCounter.getScore());
        } else {
            startBreak();
        }
    }

    private void finishSet(String winningPlayer, Score<Integer> setScoreToSave) {
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

    private boolean needsSuperTiebreak() {
        Integer player1MatchScore = matchScore.getScore(player1);
        Integer player2MatchScore = matchScore.getScore(player2);
        return matchFormat.needsSuperTiebreak(player1MatchScore, player2MatchScore) ||
            currentSetScoreCounter.needsSuperTiebreak();
    }

    private void ensureSetStarted() {
        if (currentSetScoreCounter == null) {
            startSet();
        }
    }

    private void startSet() {
        currentSetScoreCounter = TennisSetScoreCounter.of(player1, player2, finalSetTieResolutionType, isFinalSet());
    }

    private interface MatchState {
        void registerFirstServingPlayer(String player);

        void startPlay();

        void registerPoint(String winningPlayer);

        default Optional<Score<GemPoint>> getCurrentGemScore() {
            return Optional.empty();
        }

        default Optional<String> getMatchWinner() {
            return Optional.empty();
        }
    }

    private class BeforeFirstGem implements MatchState {

        @Override
        public void registerFirstServingPlayer(String player) {
            if (currentlyServingPlayer != null) {
                throw new IllegalStateException("The server has already been chosen");
            }
            currentlyServingPlayer = player;
        }

        @Override
        public void startPlay() {
            if (currentlyServingPlayer == null) {
                throw new IllegalStateException("The server has not yet been chosen");
            }
            ensureSetStarted();
            startGem();
        }

        @Override
        public void registerPoint(String winningPlayer) {
            throw new IllegalStateException("No play is currently in progress");
        }

    }

    private class GemInProgress implements MatchState {

        private GemScoreCounter gemScoreCounter = GemScoreCounter.of(player1, player2, gemScoringType);

        @Override
        public void registerFirstServingPlayer(String player) {
            throw new IllegalStateException("A gem is currently in progress");
        }

        @Override
        public void startPlay() {
            throw new IllegalStateException("A gem is currently in progress");
        }

        @Override
        public void registerPoint(String winningPlayer) {
            gemScoreCounter.increment(winningPlayer);
            if (gemScoreCounter.hasWon(winningPlayer)) {
                finishGem(winningPlayer);
            }
        }

        @Override
        public Optional<Score<GemPoint>> getCurrentGemScore() {
            return Optional.of(gemScoreCounter.getScore());
        }
    }

    private class BreakInPlay implements MatchState {

        @Override
        public void registerFirstServingPlayer(String player) {
            throw new IllegalStateException("The server has already been chosen");
        }

        @Override
        public void startPlay() {
            ensureSetStarted();
            if (needsSuperTiebreak()) {
                startSuperTiebreak();
            } else if (currentSetScoreCounter.needsTiebreak()) {
                startTiebreak();
            } else {
                startGem();
            }
        }

        @Override
        public void registerPoint(String winningPlayer) {
            throw new IllegalStateException("No play is currently in progress");
        }
    }

    private class TiebreakInPlay implements MatchState {

        private String firstServingPlayerInTiebreak;
        private TiebreakScoreCounter tiebreakScoreCounter = new TiebreakScoreCounter(player1, player2, 7);

        public TiebreakInPlay(String currentlyServingPlayer) {
            firstServingPlayerInTiebreak = currentlyServingPlayer;
        }

        @Override
        public void registerFirstServingPlayer(String player) {
            throw new IllegalStateException("A server has already been chosen");
        }

        @Override
        public void startPlay() {
            throw new IllegalStateException("A tiebreak is currently in progress");
        }

        @Override
        public void registerPoint(String winningPlayer) {
            tiebreakScoreCounter.increment(winningPlayer);
            if(tiebreakScoreCounter.isWon(winningPlayer)) {
                finishGem(winningPlayer);
                currentlyServingPlayer = firstServingPlayerInTiebreak.equals(player1) ? player2 : player1;
            } else if(tiebreakScoreCounter.shouldChangeServingPlayer()) {
                changeServingPlayer();
            }
        }
    }

    private void changeServingPlayer() {
        currentlyServingPlayer = currentlyServingPlayer.equals(player1) ? player2 : player1;
    }

    private class SupertiebreakInPlay implements MatchState {

        private TiebreakScoreCounter currentSuperTiebreakScore = new TiebreakScoreCounter(player1, player2, 10);

        @Override
        public void registerFirstServingPlayer(String player) {
            throw new IllegalStateException("The server has already been chosen");
        }

        @Override
        public void startPlay() {
            throw new IllegalStateException("A supertiebreak is currently in progress");
        }

        @Override
        public void registerPoint(String winningPlayer) {
            currentSuperTiebreakScore.increment(winningPlayer);
            if (currentSuperTiebreakScore.isWon(winningPlayer)) {
                if(finalSetTieResolutionType == SUPER_TIEBREAK) {
                    finishGem(winningPlayer);
                } else {
                    finishSet(winningPlayer, currentSuperTiebreakScore.getScore());
                }
            } else if (currentSuperTiebreakScore.shouldChangeServingPlayer()) {
                changeServingPlayer();
            }
        }
    }

    private static class MatchFinished implements MatchState {

        private final String winner;

        MatchFinished(String winner) {
            this.winner = winner;
        }

        @Override
        public void registerFirstServingPlayer(String player) {
            throwException();
        }

        @Override
        public void startPlay() {
            throwException();
        }

        @Override
        public void registerPoint(String winningPlayer) {
            throwException();
        }

        @Override
        public Optional<String> getMatchWinner() {
            return Optional.of(winner);
        }

        private void throwException() {
            throw new IllegalStateException("A match is finished");
        }
    }

}
