import java.util.ArrayList;
import java.util.List;

public class Player {
    static public final int TEAM_1 = 1;
    static public final int TEAM_2 = 10;
    private int team;
    private int id;

    List<Card> cards;

    /**
     * create a player with the given team and an empty hand.
     * @param team the team this player is assigned to (see constants).
     */
    public Player(int id, int team){
        this.id = id;
        this.team = team;
        cards = new ArrayList<>();
    }

    /**
     * determine whether this player's hand contains two of the given card
     * @param card the card to be checked whether this player has two of them
     * @return true if this player's hand contains two of the given card, false otherwise (has only one or has none)
     */
    public boolean isTwinCard(Card card){
        //has two of the given card if the first and last occurrence of the given card are different
        return(cards.indexOf(card) != -1 && cards.indexOf(card) != cards.lastIndexOf(card));
    }

    /**
     * determine whether this player has pairs of identical cards (same rank and same suit)
     * @return true if this player has at least a pair of identical cards, false otherwise
     */
    public boolean hasTwinCards(){
        for(int i=0; i<cards.size(); i++){
            if(cards.get(i).isTwoEyedJack() || cards.get(i).isOneEyedJack())
                continue;
            if(cards.indexOf( cards.get(i)) != cards.lastIndexOf(cards.get(i)))
                return true;
        }
        return false;
    }

    /**
     * determine whether this player has any card (other than one-eyed jack) that has no available cells on board
     * @param board the board of the current game
     * @return true if this player has at least a card that has no available cells on board, false otherwise
     */
    public boolean hasDeadCard(SequenceBoard board){
        for(int i=0; i<cards.size(); i++){
            if(board.getAvailableCellsIdx(cards.get(i)).size() == 0)
                return true;
        }
        return false;
    }

    /**
     * get all cards whose identical pair is also on this player's hand (except Jack).
     * @return a list of cards whose identical pair is also on this player's hand if there is any, null otherwise.
     */
    public List<Card> getTwinCards(){
        List<Card> tempList = new ArrayList<>(cards);
        List<Card> returnedList = new ArrayList<>();
        int idx = 0;
        while(idx < tempList.size()){
            if(tempList.get(idx).isOneEyedJack() || tempList.get(idx).isTwoEyedJack()) {
                tempList.remove(idx);
            }
            else if(idx == tempList.lastIndexOf(tempList.get(idx))) {
                tempList.remove(idx);
            }
            else{
                returnedList.add(tempList.get(idx));
                tempList.remove(tempList.lastIndexOf(tempList.get(idx)));
                tempList.remove(idx);
            }
        }
        tempList.clear();
        tempList = null;
        if(returnedList.size() == 0) {
            return null;
        }
        else{
            return returnedList;
        }
    }

    /**
     * get a list of cards on this player's hand that have no available cells left on the board
     * @param board the board of the current game
     * @return a list of cards on this player's hand that have no available cells left on the board if there is any,
     * null otherwise
     */
    public List<Card> getDeadCards(SequenceBoard board){
        List<Card> returnedList = new ArrayList<Card>();
        for(int i=0; i<cards.size(); i++){
            if(cards.get(i).isOneEyedJack() || cards.get(i).isTwoEyedJack())
                continue;
            if(board.getAvailableCellsIdx(cards.get(i)).size() == 0)
                returnedList.add(cards.get(i));
        }
        if(returnedList.size() == 0)
            return null;
        else
            return returnedList;
    }

    /**
     * try to remove the given card from the player's hand.
     * @param card the card to be removed from this player's hand.
     * @return true if the removal is successful, false otherwise.
     */
    public boolean removeCard(Card card){
        int idx = cards.indexOf(card);
        if(idx >= 0) {
            cards.remove(card);
            return true;
        }
        return false;
    }

    /**
     * try to remove the card from the player's hand at the specified index.
     * @param idx the index of the card to be removed from the player's hand.
     * @return true if the removal is successful (idx is not out of bounds), false otherwise.
     */
    public boolean removeCard(int idx){
        if(idx < 0 && idx >= cards.size())
            return false;
        else{
            cards.remove(idx);
            return true;
        }

    }

    /**
     * add the given card to this player's hand.
     * @param card the new card given to this player.
     */
    public void addCard(Card card){ cards.add(card); }

    /**
     * get all the cards from this player.
     * @return a reference to the list of cards inside this object.
     */
    public List<Card> getCards(){ return cards; }

    /**
     * get the card with the given name from this player.
     * @param name the name of the card (see enumeration in {@code Card} class).
     * @return the card with the given name in this player's hand if exist, null otherwise.
     */
    public Card getCard(String name){
        for(Card eachCard : cards){
            if(eachCard.getName().equals(name))
                return eachCard;
        }
        return null;
    }

    /**
     * get the team this player belongs to.
     * @return the team number this player belongs to.
     */
    public int getTeam() { return team; }

    /**
     * get the id for this player.
     * @return the id for this player.
     */
    public int getId() {return id; }
}
