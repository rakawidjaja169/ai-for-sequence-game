import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

public class PnlInfo extends JPanel {
    private final Font FONT_INFO = new Font("Times New Roman", Font.BOLD, 30);
    public final LineBorder WINNER1_BORDER = new LineBorder(PnlPlayer.COLOR_1, 4);
    public final LineBorder WINNER2_BORDER = new LineBorder(PnlPlayer.COLOR_2, 4);
    private LblCard lblDeck;
    private LblCard lblUsedDeck;
    private GameModel gameModel;
    private List<Card> deck;
    private List<Card> usedDeck;
    private JPanel pnlDeck;

    private JLabel lblFiller;
    private JLabel lblTeam1;
    private JLabel lblTeam2;

    private boolean isDeckExist = true;
    private boolean isUsedDeckExist = false;

    public PnlInfo(FrmMain frmMain, GameModel mainModel){
        //set the properties of this panel
        initializeProperties(mainModel);
        setLayout(new GridLayout(3,1));

        //create label filler (when there is no deck to be displayed)
        lblFiller = new JLabel();

        //create the label for team info
        lblTeam1 = new JLabel();
        lblTeam1.setHorizontalAlignment(SwingConstants.CENTER);
        lblTeam1.setForeground(PnlPlayer.COLOR_1);
        lblTeam1.setFont(FONT_INFO);
        lblTeam2 = new JLabel();
        lblTeam2.setHorizontalAlignment(SwingConstants.CENTER);
        lblTeam2.setForeground(PnlPlayer.COLOR_2);
        lblTeam2.setFont(FONT_INFO);

        //create the panel for the decks
        pnlDeck = new JPanel();
        pnlDeck.setLayout(new GridLayout(1,2));

        //create labels for each deck
        lblDeck = new LblCard(frmMain, frmMain.getHeight()/3,
                false, deck.get(1), 1, false);
        lblUsedDeck = new LblCard(frmMain, frmMain.getHeight()/3,
                false, SequenceBoard.WILD_CARD, 1,true){
            @Override
            public void mouseClicked(MouseEvent e){
                if(isClickable() && isSelected()){
                    
                }
            }
        };

        //adding all components
        add(pnlDeck);
        pnlDeck.add(lblDeck);
        pnlDeck.add(lblFiller);
        add(lblTeam1);
        add(lblTeam2);

        setVisible(true);
        update();
    }

    public void update(){
        //update the decks and their info
        if(deck.size() > 0) {
            lblDeck.setCard(deck.get(0));
        }
        else{
            if(isDeckExist){
                pnlDeck.removeAll();
                pnlDeck.add(lblFiller);
                pnlDeck.add(lblUsedDeck);
                isDeckExist = false;
            }
        }

        if(usedDeck.size() > 0) {
            if(!isUsedDeckExist){
                pnlDeck.remove(lblFiller);
                lblUsedDeck.setSize(lblDeck.getWidth(), lblDeck.getHeight());
                pnlDeck.add(lblUsedDeck);
                isUsedDeckExist = true;
            }

            lblUsedDeck.setCard(usedDeck.get(usedDeck.size() - 1));
        }

        //update the info for both teams
        String infoTeam1 = "<html> Team 1 <br>" +
                "Lines: " + gameModel.getSequenceBoard().getNSequence(Player.TEAM_1) + "<br>" +
                "Score: " + gameModel.getScoreTeam1() + "<br>";
        String infoTeam2 = "<html> Team 2 <br>" +
                "Lines: " + gameModel.getSequenceBoard().getNSequence(Player.TEAM_2) + "<br>" +
                "Score: " + gameModel.getScoreTeam2() + "<br>";
        int winner = gameModel.getSequenceBoard().getWinner();
        if(winner == Player.TEAM_1){
            lblTeam1.setBorder(WINNER1_BORDER);
            infoTeam1 += "WINNER";
        }
        else if(winner == Player.TEAM_2){
            lblTeam2.setBorder(WINNER2_BORDER);
            infoTeam2 += "WINNER";
        }
        lblTeam1.setText(infoTeam1);
        lblTeam2.setText(infoTeam2);

        repaint();
        revalidate();
    }

    public void initializeProperties(GameModel mainModel){
        gameModel = mainModel;
        deck = gameModel.getDeck();
        usedDeck = gameModel.getUsedDeck();
        isDeckExist = true;
        isUsedDeckExist = false;
    }

    public void resetPanel(GameModel mainModel){
        initializeProperties(mainModel);
        pnlDeck.removeAll();
        pnlDeck.add(lblDeck);
        pnlDeck.add(lblUsedDeck);

        lblTeam1.setBorder(null);
        lblTeam2.setBorder(null);
        update();
    }
}
