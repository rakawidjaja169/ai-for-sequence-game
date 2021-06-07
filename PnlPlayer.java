import javax.swing.*;
import javax.swing.border.LineBorder;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PnlPlayer extends JPanel {
    public static final Color COLOR_1 = Color.BLUE;
    public static final Color COLOR_2 = Color.ORANGE;

    private FrmMain frmMain;
    private Player player;
    private JLabel lblTeam;
    private List<LblCard> lblCards;
    private JLabel lblFiller;
    public PnlPlayer(FrmMain frmMain, Player player){
        //Set the panel's properties
        this.frmMain = frmMain;
        setLayout(new GridLayout(GameModel.MAX_CARD_PLAYER+1, 1));
        setBorder(FrmMain.BORDER_PANEL);

        //Add a label showing which team the player belongs to
        lblTeam = new JLabel();
        lblTeam.setHorizontalAlignment(SwingConstants.CENTER);
        lblTeam.setFont(new Font("Times New Roman", Font.BOLD, 30));
        add(lblTeam);

        //Set the player
        setPlayer(player);

        //Calculate the height of each label for the cards
        int lblHeight = frmMain.getHeight()/GameModel.MAX_CARD_PLAYER+2;

        //Initialize labels for displaying cards.
        List<Card> tempList = player.getCards();
        lblCards = new ArrayList<LblCard>(tempList.size());
        for(int i=0; i<tempList.size(); i++){
            lblCards.add(new LblCard(frmMain, lblHeight, true,
                    tempList.get(i), true));
            add(lblCards.get(lblCards.size()-1));
        }
        tempList = null;

        //Add labels as placeholders for the remainder of the panel
        lblFiller = new JLabel();
        for(int i=0; i<GameModel.MAX_CARD_PLAYER - lblCards.size(); i++){
            add(lblFiller);
        }

        setVisible(true);
    }



    /**
     * update the information displayed on the panel to match the information of the player's represented in this panel.
     * This method then calls repaint and revalidate to refresh its interface.
     */
    public void update(){
        //remove all components from this panel
        this.removeAll();

        //add the team's information
        this.add(lblTeam);

        //update the number of card labels to match with the total cards in hand
        List<Card> tempList = player.getCards();
        while(tempList.size() < lblCards.size()){
            lblCards.get(lblCards.size()-1).reset();
            lblCards.remove(lblCards.size()-1);
        }
        if(tempList.size() > lblCards.size()){
            for(int i=lblCards.size(); i<tempList.size(); i++){
                lblCards.add(new LblCard(frmMain, getHeight(), true, tempList.get(i), true));
            }
        }

        //add all cards held by the player
        for(int i=0; i<lblCards.size(); i++){
            System.out.println(lblCards.size() + " - " + tempList.size());
            lblCards.get(i).setSelected(false);
            lblCards.get(i).setCard(tempList.get(i));
            this.add(lblCards.get(i));
        }

        //add fillers for the remaining area of the panel
        for(int i=lblCards.size(); i<GameModel.MAX_CARD_PLAYER; i++){
            this.add(lblFiller);
        }
        repaint();
        revalidate();
    }

    public void unselectLabel(){
        for(LblCard eachLbl : lblCards){
            if(eachLbl.isSelected()) {
                eachLbl.select();
            }
        }
    }

    /**
     * disable clickable property of all labels in this panel
     */
    public void disableCards(){
        for(LblCard eachLbl : lblCards){
            eachLbl.setClickable(false);
        }
    }

    /**
     * enable clickable property of all labels in this panel
     */
    public void enableCards(){
        for(LblCard eachLbl : lblCards){
            eachLbl.setClickable(true);
        }
    }

    /**
     * set the player whose information is to be displayed in this panel.
     * @param player the player whose information is to be displayed in this panel.
     */
    private void setPlayer(Player player){
        this.player = player;

        //if player from team 1
        if(player.getTeam() == Player.TEAM_1){
            lblTeam.setForeground(COLOR_1);
            //lblTeam.setText("<html>Team 1<br/>Player " + player.getId() + "</html>");
        }
        //if player from team 2
        else{
            lblTeam.setForeground(COLOR_2);
            //lblTeam.setText("<html>Team 2<br/>Player " + player.getId() + "</html>");
        }
        lblTeam.setText("Player " + player.getId());
    }

    /**
     * change the player whose information to be displayed in this panel, then refresh the display.
     * @param player the player whose information to be displayed in this panel.
     */
    public void changePlayer(Player player){
        setPlayer(player);
        update();
    }

}
