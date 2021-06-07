public class Card {
    public static enum Code{
        X, S2, S3, S4, S5, S6, S7, S8, S9, S10, SQ, SK, SA,
        H2, H3, H4, H5, H6, H7, H8, H9, H10, HQ, HK, HA,
        C2, C3, C4, C5, C6, C7, C8, C9, C10, CQ, CK, CA,
        D2, D3, D4, D5, D6, D7, D8, D9, D10, DQ, DK, DA,
        SJ, HJ, CJ, DJ
    }

    /**a String representing the suit and the rank of this card, given in a specific format (see class' constants).*/
    private Code code;

    /**
     * Create a card with the specified code representing the suit and the rank of the created Card. Use the
     * {@code enum Code} for the list of codes for the card.
     * @param code the code containing the suit and the rank of this card in a specific format (use enum).
     */
    public Card(Code code){
        this.code = code;
    }

    /**
     * Create a card with the specified name representing the suit and the rank of the created Card. Use the
     * {@code enum Code} for the list of names for the card.
     * @param name the name containing the suit and the rank of this card in a specific format (use enum).
     */
    public Card(String name) {
        this.code = Code.valueOf(name);
    }

    /**
     * Create a card with the specified id. Use the ordinal values from {@code enum Code} for the list of id.
     * @param id the id of the card.
     */
    public Card(int id){
        this.code = Code.values()[id];
    }

    /**
     * determine whether this card is a one-eyed jack, which is Jack of Spade or Jack of Heart
     * @return true if this card is either Jack of Spade or Jack of Heart, false otherwise
     */
    public boolean isOneEyedJack(){
        return code.equals(Code.SJ) || code.equals(Code.HJ);
    }

    /**
     * determine whether this card is a two-eyed jack, which is Jack of Club or Jack of Diamond
     * @return true if this card is either Jack of Club or Jack of Diamond, false otherwise
     */
    public boolean isTwoEyedJack(){
        return code.equals(Code.CJ) || code.equals(Code.DJ);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null)
            return false;
        else if(!(obj instanceof Card))
            return false;
        else{
            Card otherCard = (Card)obj;
            return getName().equals(otherCard.getName());
        }
    }

    public boolean equals(int id){
        return getId() == id;
    }


    /**
     * get the code of type String for this card that represents the suit and the rank of this card.
     * The first first letter shows the suit (S, H, C, D), while the rest shows the rank.
     * @return a String whose first letter shows the suit (S, H, C, D) and the rest shows the rank.
     */
    public Code getCode() { return code; }

    public String getName(){ return code.name(); }

    public int getId(){
        return code.ordinal();
    }
}
