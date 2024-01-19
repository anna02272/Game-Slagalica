package com.example.slagalica.login_registration;


public class User {
    private String username;
    private int tokens;
    private int stars;
    private  String date;
    private int playedGames;
    private int wonGames ;
    private int lostGames ;
    private int koZnaZna ;
    private int spojnice ;
    private int asocijacije;
    private int skocko;
    private int korakPoKorak ;
    private int mojBroj ;
    private int spojnicePoints ;
    private int korakPoKorakPoints ;
    private int mojBrojPoints ;

    public User() {
    }

    public User(String username) {
        this.username = username;
    }

    public User(String username, int tokens, int stars, String date,  int playedGames, int wonGames, int lostGames,
                int koZnaZna, int spojnice, int asocijacije, int skocko, int korakPoKorak, int mojBroj,
                int spojnicePoints, int korakPoKorakPoints, int mojBrojPoints) {
        this.username = username;
        this.tokens = tokens;
        this.stars = stars;
        this.date = date;
        this.playedGames = playedGames;
        this.wonGames = wonGames;
        this.lostGames = lostGames;
        this.koZnaZna = koZnaZna;
        this.spojnice = spojnice;
        this.asocijacije = asocijacije;
        this.skocko = skocko;
        this.korakPoKorak = korakPoKorak;
        this.mojBroj = mojBroj;
        this.spojnicePoints = spojnicePoints;
        this.korakPoKorakPoints = korakPoKorakPoints;
        this.mojBrojPoints = mojBrojPoints;

    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public int getTokens() {
        return tokens;
    }
    public void setTokens(int tokens) {
        this.tokens = tokens;
    }
    public int getStars() {
        return stars;
    }
    public void setStars(int stars) {
        this.stars = stars;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public int getPlayedGames() {
        return playedGames;
    }
    public void setPlayedGames(int playedGames) {
        this.playedGames = playedGames;
    }
    public int getWonGames() {
        return wonGames;
    }
    public void setWonGames(int wonGames) {
        this.wonGames = wonGames;
    }

    public int getLostGames() {
        return lostGames;
    }

    public void setLostGames(int lostGames) {
        this.lostGames = lostGames;
    }
    public int getKoZnaZna() {
        return koZnaZna;
    }

    public void setKoZnaZna(int koZnaZna) {
        this.koZnaZna = koZnaZna;
    }

    public int getSpojnice() {
        return spojnice;
    }

    public void setSpojnice(int spojnice) {
        this.spojnice = spojnice;
    }

    public int getAsocijacije() {
        return asocijacije;
    }

    public void setAsocijacije(int asocijacije) {
        this.asocijacije = asocijacije;
    }

    public int getSkocko() {
        return skocko;
    }

    public void setSkocko(int skocko) {
        this.skocko = skocko;
    }

    public int getKorakPoKorak() {
        return korakPoKorak;
    }

    public void setKorakPoKorak(int korakPoKorak) {
        this.korakPoKorak = korakPoKorak;
    }

    public int getMojBroj() {
        return mojBroj;
    }

    public void setMojBroj(int mojBroj) {
        this.mojBroj = mojBroj;
    }
    public int getSpojnicePoints() {
        return spojnicePoints;
    }

    public void setSpojnicePoints(int spojnicePoints) {
        this.spojnicePoints = spojnicePoints;
    }
    public int getKorakPoKorakPoints() {
        return korakPoKorakPoints;
    }

    public void setKorakPoKorakPoints(int korakPoKorakPoints) {
        this.korakPoKorakPoints = korakPoKorakPoints;
    }

    public int getMojBrojPoints() {
        return mojBrojPoints;
    }

    public void setMojBrojPoints(int mojBrojPoints) {
        this.mojBrojPoints = mojBrojPoints;
    }
}

