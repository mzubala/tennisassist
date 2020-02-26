package pl.com.bottega.tennisassist;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static pl.com.bottega.tennisassist.MatchFormat.BEST_OF_FIVE;
import static pl.com.bottega.tennisassist.MatchFormat.BEST_OF_THREE;
import static pl.com.bottega.tennisassist.TieResolutionType.ADVANTAGE;
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
    private Score<Integer> currentSetScore;
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
        if (currentSetScore == null) {
            currentSetScore = new Score<>(player1, player2, 0);
        }
        currentState.startPlay();
    }

    public Optional<Score<GemPoint>> getCurrentGemScore() {
        return currentState.getCurrentGemScore();
    }

    public void registerPoint(String winningPlayer) {
        currentState.registerPoint(winningPlayer);
    }

    public Optional<Score> getCurrentSetScore() {
        return Optional.ofNullable(currentSetScore);
    }

    public List<Score> getScoresOfSets() {
        return Collections.unmodifiableList(scoresOfSets);
    }

    public Optional<String> getMatchWinner() {
        return currentState.getMatchWinner();
    }

    private void finishMatch(String winner) {
        currentState = new MatchFinished(winner);
    }

    private void startBreak() {
        currentlyServingPlayer = currentlyServingPlayer.equals(player1) ? player2 : player1;
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

    private void increaseSetScore(String winningPlayer) {
        currentSetScore = currentSetScore.withUpdatedScore(winningPlayer, currentSetScore.getScore(winningPlayer) + 1);
        if (currentSetScore.getScore(winningPlayer) >= 6 && currentSetScore.getScore(winningPlayer) - currentSetScore.getOtherPlayerScore(winningPlayer) >= 2) {
            increaseMatchScore(winningPlayer);
        } else {
            startBreak();
        }

    }

    private void increaseMatchScore(String winningPlayer) {
        matchScore = matchScore.withUpdatedScore(winningPlayer, matchScore.getScore(winningPlayer) + 1);
        scoresOfSets.add(currentSetScore);
        currentSetScore = null;
        if ((matchFormat == BEST_OF_THREE && matchScore.getScore(winningPlayer) == 2) || (matchFormat == BEST_OF_FIVE && matchScore.getScore(winningPlayer) == 3)) {
            finishMatch(winningPlayer);
        } else {
            startBreak();
        }
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
            if(gemScoreCounter.hasWon(winningPlayer)) {
                increaseSetScore(winningPlayer);
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
            if (
                (matchFormat == MatchFormat.BEST_OF_TWO_WITH_SUPER_TIEBREAK && matchScore.getScore(player1) == 1 && matchScore.getScore(player2) == 1) ||
                    (finalSetTieResolutionType == SUPER_TIEBREAK && currentSetScore.getScore(player1) == 6 && currentSetScore.getScore(player2) == 6 &&
                        (
                            (matchScore.getScore(player1) == 1 && matchScore.getScore(player2) == 1 && matchFormat == BEST_OF_THREE) ||
                                (matchScore.getScore(player1) == 2 && matchScore.getScore(player2) == 2 && matchFormat == BEST_OF_FIVE)
                        )
                    )
            ) {
                startSuperTiebreak();
            } else if (currentSetScore.getScore(player1) == 6 && currentSetScore.getScore(player2) == 6 &&
                !(finalSetTieResolutionType == ADVANTAGE && (
                    (matchScore.getScore(player1) == 1 && matchScore.getScore(player2) == 1 && matchFormat == BEST_OF_THREE) ||
                        (matchScore.getScore(player1) == 2 && matchScore.getScore(player2) == 2 && matchFormat == BEST_OF_FIVE)
                ))
            ) {
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
        private Score<Integer> currentTiebreakScore = new Score<>(player1, player2, 0);

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
            String loosingPlayer = winningPlayer.equals(player1) ? player2 : player1;
            int winningPlayerScore = currentTiebreakScore.getScore(winningPlayer) + 1;
            currentTiebreakScore = currentTiebreakScore.withUpdatedScore(winningPlayer, winningPlayerScore);
            int loosingPlayerScore = currentTiebreakScore.getScore(loosingPlayer);
            if (winningPlayerScore >= 7 && winningPlayerScore - loosingPlayerScore >= 2) {
                currentSetScore = currentSetScore.withUpdatedScore(winningPlayer, currentSetScore.getScore(winningPlayer) + 1);
                if ((currentSetScore.getScore(winningPlayer) == 6 && currentSetScore.getScore(loosingPlayer) < 5) || currentSetScore.getScore(winningPlayer) == 7) {
                    increaseMatchScore(winningPlayer);
                }
                currentTiebreakScore = null;
                currentlyServingPlayer = firstServingPlayerInTiebreak.equals(player1) ? player2 : player1;
            } else if ((winningPlayerScore + loosingPlayerScore) % 2 == 1) {
                currentlyServingPlayer = currentlyServingPlayer.equals(player1) ? player2 : player1;
            }
        }
    }

    private class SupertiebreakInPlay implements MatchState {

        private Score<Integer> currentSuperTiebreakScore = new Score<>(player1, player2, 0);

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
            String loosingPlayer = winningPlayer.equals(player1) ? player2 : player1;
            int winningPlayerScore = currentSuperTiebreakScore.getScore(winningPlayer) + 1;
            currentSuperTiebreakScore = currentSuperTiebreakScore.withUpdatedScore(winningPlayer, winningPlayerScore);
            int loosingPlayerScore = currentSuperTiebreakScore.getScore(loosingPlayer);
            if (winningPlayerScore >= 10 && winningPlayerScore - loosingPlayerScore >= 2) {
                matchScore = matchScore.withUpdatedScore(winningPlayer, matchScore.getScore(winningPlayer) + 1);
                currentSuperTiebreakScore = null;
                if (finalSetTieResolutionType == SUPER_TIEBREAK) {
                    currentSetScore = currentSetScore.withUpdatedScore(winningPlayer, 7);
                    scoresOfSets.add(currentSetScore);
                }
                currentSetScore = null;
                finishMatch(winningPlayer);
            } else if ((winningPlayerScore + loosingPlayerScore) % 2 == 1) {
                currentlyServingPlayer = currentlyServingPlayer.equals(player1) ? player2 : player1;
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
