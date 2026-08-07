package BlackjackTrainer;

public class Player {
    private int score;

    public Player() {
        this.score = 0;
    }
    public int updateScore(int score, String drawnCard){
        int cardValue = Integer.parseInt(drawnCard.substring(0,2));
        if(cardValue==11||cardValue==12||cardValue==13){
            cardValue=10;
        }
        return score + cardValue;
    }

    public boolean playerBust(int score,String drawnCard){
        boolean bust =false;
        if (score+Integer.parseInt(drawnCard.substring(0,2))>21){
            bust = true;
        }
        return bust;
    }
}
