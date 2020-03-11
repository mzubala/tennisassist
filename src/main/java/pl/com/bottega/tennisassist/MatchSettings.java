package pl.com.bottega.tennisassist;

public class MatchSettings {
    private final MatchFormat matchFormat;
    private final GemScoringType gemScoringType;
    private final TieResolutionType finalSetTieResolutionType;

    public MatchSettings(MatchFormat matchFormat, GemScoringType gemScoringType, TieResolutionType finalSetTieResolutionType) {
        this.matchFormat = matchFormat;
        this.gemScoringType = gemScoringType;
        this.finalSetTieResolutionType = finalSetTieResolutionType;
    }

    public MatchFormat getMatchFormat() {
        return matchFormat;
    }

    public GemScoringType getGemScoringType() {
        return gemScoringType;
    }

    public TieResolutionType getFinalSetTieResolutionType() {
        return finalSetTieResolutionType;
    }

    public boolean needsSuperTiebreak(Integer player1MatchScore, Integer player2MatchScore) {
        return matchFormat.needsSuperTiebreak(player1MatchScore, player2MatchScore);
    }

    public boolean isFinalSet(Integer score, Integer score1) {
        return matchFormat.isFinalSet(score, score1);
    }

    public boolean isMatchFinished(Integer score) {
        return matchFormat.isMatchFinished(score);
    }
}
