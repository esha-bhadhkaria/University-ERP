package edu.univ.erp.domain;

public class Grade {
    private int gradeid;
    private int enrollmentid;
    private String component;
    private double score;
    private double maxscore;
    private double weight;

    public Grade() {}

    public Grade(int enrollmentid, String component, double score, double maxscore, double weight) {
        this.enrollmentid = enrollmentid;
        this.component = component;
        this.score = score;
        this.maxscore = maxscore;
        this.weight = weight;
    }

    public void setGradeid(int gradeid) { this.gradeid = gradeid; }

    public int getEnrollmentid() { return enrollmentid; }
    public void setEnrollmentid(int enrollmentid) { this.enrollmentid = enrollmentid; }

    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public double getMaxscore() { return maxscore; }
    public void setMaxscore(double maxscore) { this.maxscore = maxscore; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getPercentage() {
        return maxscore > 0 ? (score / maxscore) * 100 : 0;
    }

    @Override
    public String toString() {
        return component + ": " + score + "/" + maxscore;
    }
}
