package com.zlscorp.ultragrav.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "feedbackScaleParams")
public class FeedbackScaleParams extends AbstractParamsObject {
    private static final long serialVersionUID = 1L;
    
    @DatabaseField
    private Integer freqMinus10;

    @DatabaseField
    private Integer freqMinus5;

    @DatabaseField
    private Integer freq0;

    @DatabaseField
    private Integer freqPlus5;

    @DatabaseField
    private Integer freqPlus10;

    @DatabaseField
    private Double fivePercentMinus10;

    @DatabaseField
    private Double fivePercentMinus5;

    @DatabaseField
    private Double fivePercent0;

    @DatabaseField
    private Double fivePercentPlus5;

    @DatabaseField
    private Double fivePercentPlus10;

    @DatabaseField
    private Double nintyEightPercentMinus10;

    @DatabaseField
    private Double nintyEightPercentMinus5;

    @DatabaseField
    private Double nintyEightPercent0;

    @DatabaseField
    private Double nintyEightPercentPlus5;

    @DatabaseField
    private Double nintyEightPercentPlus10;

    @DatabaseField
    private Double ccnmFactor;
    
    public void setFrequencyArray(int column, Integer value) {
        switch (column) {
            case 0:
                freqMinus10 = value;
                break;
            case 1:
                freqMinus5 = value;
                break;
            case 2:
                freq0 = value;
                break;
            case 3:
                freqPlus5 = value;
                break;
            case 4:
                freqPlus10 = value;
                break;

            default:
                break;
        }
    }
    
    public Integer getFrequencyArray (int column) {
        switch (column) {
            case 0:  return freqMinus10;
            case 1:  return freqMinus5;
            case 2:  return freq0;
            case 3:  return freqPlus5;
            case 4:  return freqPlus10;
            default: return null;
        }
    }

    public Double getDialCounterArray (int row, int column) {
        if (row == 1) {
            switch (column) {
                case 0:  return fivePercentMinus10;
                case 1:  return fivePercentMinus5;
                case 2:  return fivePercent0;
                case 3:  return fivePercentPlus5;
                case 4:  return fivePercentPlus10;
                default: return null;
            }
        } else if (row == 2) {
            switch (column) {
                case 0:  return nintyEightPercentMinus10;
                case 1:  return nintyEightPercentMinus5;
                case 2:  return nintyEightPercent0;
                case 3:  return nintyEightPercentPlus5;
                case 4:  return nintyEightPercentPlus10;
                default: return null;
            }
        }
        return null;
    }
    
    public void setDialCounterArray(int row, int column, Double value) {
        if (row == 1) {
            switch (column) {
                case 0:
                    fivePercentMinus10 = value;
                    break;
                case 1:
                    fivePercentMinus5 = value;
                    break;
                case 2:
                    fivePercent0 = value;
                    break;
                case 3:
                    fivePercentPlus5 = value;
                    break;
                case 4:
                    fivePercentPlus10 = value;
                    break;

                default:
                    break;
            }
        } else if (row == 2) {
            switch (column) {
                case 0:
                    nintyEightPercentMinus10 = value;
                    break;
                case 1:
                    nintyEightPercentMinus5 = value;
                    break;
                case 2:
                    nintyEightPercent0 = value;
                    break;
                case 3:
                    nintyEightPercentPlus5 = value;
                    break;
                case 4:
                    nintyEightPercentPlus10 = value;
                    break;

                default:
                    break;
            }
        }
    }

    public Integer getFreqMinus10() {
        return freqMinus10;
    }

    public void setFreqMinus10(Integer freqMinus10) {
        this.freqMinus10 = freqMinus10;
    }

    public Integer getFreqMinus5() {
        return freqMinus5;
    }

    public void setFreqMinus5(Integer freqMinus5) {
        this.freqMinus5 = freqMinus5;
    }

    public Integer getFreq0() {
        return freq0;
    }

    public void setFreq0(Integer freq0) {
        this.freq0 = freq0;
    }

    public Integer getFreqPlus5() {
        return freqPlus5;
    }

    public void setFreqPlus5(Integer freqPlus5) {
        this.freqPlus5 = freqPlus5;
    }

    public Integer getFreqPlus10() {
        return freqPlus10;
    }

    public void setFreqPlus10(Integer freqPlus10) {
        this.freqPlus10 = freqPlus10;
    }

    public Double getFivePercentMinus10() {
        return fivePercentMinus10;
    }

    public void setFivePercentMinus10(Double fivePercentMinus10) {
        this.fivePercentMinus10 = fivePercentMinus10;
    }

    public Double getFivePercentMinus5() {
        return fivePercentMinus5;
    }

    public void setFivePercentMinus5(Double fivePercentMinus5) {
        this.fivePercentMinus5 = fivePercentMinus5;
    }

    public Double getFivePercent0() {
        return fivePercent0;
    }

    public void setFivePercent0(Double fivePercent0) {
        this.fivePercent0 = fivePercent0;
    }

    public Double getFivePercentPlus5() {
        return fivePercentPlus5;
    }

    public void setFivePercentPlus5(Double fivePercentPlus5) {
        this.fivePercentPlus5 = fivePercentPlus5;
    }

    public Double getFivePercentPlus10() {
        return fivePercentPlus10;
    }

    public void setFivePercentPlus10(Double fivePercentPlus10) {
        this.fivePercentPlus10 = fivePercentPlus10;
    }

    public Double getNintyEightPercentMinus10() {
        return nintyEightPercentMinus10;
    }

    public void setNintyEightPercentMinus10(Double nintyEightPercentMinus10) {
        this.nintyEightPercentMinus10 = nintyEightPercentMinus10;
    }

    public Double getNintyEightPercentMinus5() {
        return nintyEightPercentMinus5;
    }

    public void setNintyEightPercentMinus5(Double nintyEightPercentMinus5) {
        this.nintyEightPercentMinus5 = nintyEightPercentMinus5;
    }

    public Double getNintyEightPercent0() {
        return nintyEightPercent0;
    }

    public void setNintyEightPercent0(Double nintyEightPercent0) {
        this.nintyEightPercent0 = nintyEightPercent0;
    }

    public Double getNintyEightPercentPlus5() {
        return nintyEightPercentPlus5;
    }

    public void setNintyEightPercentPlus5(Double nintyEightPercentPlus5) {
        this.nintyEightPercentPlus5 = nintyEightPercentPlus5;
    }

    public Double getNintyEightPercentPlus10() {
        return nintyEightPercentPlus10;
    }

    public void setNintyEightPercentPlus10(Double nintyEightPercentPlus10) {
        this.nintyEightPercentPlus10 = nintyEightPercentPlus10;
    }

    public Double getCcnmFactor() {
        return ccnmFactor;
    }

    public void setCcnmFactor(Double ccnmFactor) {
        this.ccnmFactor = ccnmFactor;
    }

    @Override
    public String toString() {
        return "FeedbackScaleParams [freqMinus10=" + freqMinus10 + ", freqMinus5=" + freqMinus5 + ", freq0=" + freq0
                + ", freqPlus5=" + freqPlus5 + ", freqPlus10=" + freqPlus10 + ", fivePercentMinus10="
                + fivePercentMinus10 + ", fivePercentMinus5=" + fivePercentMinus5 + ", fivePercent0=" + fivePercent0
                + ", fivePercentPlus5=" + fivePercentPlus5 + ", fivePercentPlus10=" + fivePercentPlus10
                + ", nintyEightPercentMinus10=" + nintyEightPercentMinus10 + ", nintyEightPercentMinus5="
                + nintyEightPercentMinus5 + ", nintyEightPercent0=" + nintyEightPercent0 + ", nintyEightPercentPlus5="
                + nintyEightPercentPlus5 + ", nintyEightPercentPlus10=" + nintyEightPercentPlus10 + ", ccnmFactor="
                + ccnmFactor + "]";
    }
}
