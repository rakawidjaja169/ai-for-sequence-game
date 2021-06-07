import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FrmMain extends JFrame implements Runnable{
    public final List<Thread> threadQueue = new ArrayList<>();
    public boolean isInitializing;

    public static final Border BORDER_PANEL = new EtchedBorder();
    private GameModel mainModel;

    private PnlBoard pnlBoard;
    private PnlPlayer pnlPlayer;
    private PnlInfo pnlInfo;

    public FrmMain(){
        super();
        setSize(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        //create the model
        initializeGame();

        //initialize the panel displaying the board
        pnlBoard = new PnlBoard(this, mainModel.getSequenceBoard());
        add(pnlBoard);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.9;
        c.weighty = 0.8;
        c.gridwidth = 4;
        c.gridheight = GridBagConstraints.REMAINDER;
        c.gridx = c.gridy = 0;
        add(pnlBoard, c);

        //initialize the panel displaying the player
        pnlPlayer = new PnlPlayer(this, mainModel.getCurrentPlayer());
        /*System.out.println(pnlBoard.getWidth());
        pnlPlayer.setLocation(700, 0);*/
        add(pnlPlayer);
        c.anchor = GridBagConstraints.LINE_END;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.01;
        c.weighty = 0.8;
        c.gridwidth = 2;
        c.gridheight = GridBagConstraints.RELATIVE;
        c.gridx = 6; c.gridy = 0;
        add(pnlPlayer, c);

        //Button start game
        JButton btnPlay = new JButton("Start Game");
        btnPlay.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                initializeGame();
                resetDisplay();
                if(mainModel.isCurrentPlayerAi()){
                    (new Thread(FrmMain.this)).start();
                }
                /*if(mainModel.isCurrentPlayerAi()){
                    threadQueue.add(new Thread(FrmMain.this));
                }*/
            }
        });
        //add(btnPlay);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.weightx = 0.4;
        c.weighty = 0;
        c.gridy = 12;
        c.gridx = 5;
        add(btnPlay, c);

        //initialize the panel displaying the decks and the scores
        pnlInfo = new PnlInfo(this, mainModel);
        pnlInfo.setBorder(BORDER_PANEL);
        c.anchor = GridBagConstraints.CENTER;
        c.weightx=0.09; c.weighty=0.8;
        c.gridx = 5;c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = 1;
        c.gridheight = GridBagConstraints.RELATIVE;
        add(pnlInfo, c);

        setVisible(true);
        pnlPlayer.update();
        pnlInfo.update();

        if(mainModel.isCurrentPlayerAi()){
            (new Thread(FrmMain.this)).start();
        }
    }

    /**
     * play the selected card (in game model) at the given cell index. This method will trigger the model to update
     * its state (see {@code playCardAt} in {@code GameModel}), and then updates the display. Updating the display
     * includes unable all cells to be clicked, update the panel for current player, and also the panel for info.
     * @param x the x position where the selected card will be put on
     * @param y the y position where the selected card will be put on
     */
    public void playSelectedCardAt(int x, int y){
        if(mainModel.getSelectedCard() != null){
            //remove all highlights
            removeHighlights();

            //the model will make the current player to play the selected card at the given cell
            mainModel.playSelectedCardAt(x, y);

            //if there are more sequences to choose, update display to give users chance to pick which sequence
            if(mainModel.isConsecutiveSequences()){
                activateCandidateSequences(true);
            }
            //else, update players turn
            else{
                nextTurn();
            }
        }
    }

    public void playAi(AI player) {
        //pause to see all cards
        //pause();

        //if player still has cards
        if(!player.getCards().isEmpty()) {

            //determine whether the AI discards card or not
            int discardedCard = player.discardCard();
            if (discardedCard > 0) {
                JOptionPane.showMessageDialog(this,
                        discardedCard + " cards have been discarded.");
                pnlInfo.update();
                pnlPlayer.update();
            }

            //determine the card to be played by AI
            Card bestCard = player.evaluateHand();

            mainModel.setSelectedCard(bestCard);
            boolean isTwinCard = player.isTwinCardEnabled();
            if (isTwinCard) {
                List<Integer> listOfIdx = mainModel.getSequenceBoard().getAvailableCellsIdx(bestCard);
                mainModel.playSelectedTwinCard(listOfIdx);
            } else {
                int idx = player.getSelectedIdxOneDim();

                int x = SequenceBoard.getTwoDimCellIdxX(idx);
                int y = SequenceBoard.getTwoDimCellIdxY(idx);

                //play the selected card
                mainModel.playSelectedCardAt(x, y);
            }

            //resolve any consecutive sequences
            while (mainModel.isConsecutiveSequences()) {
                List<Sequence> currentConsecutiveSeqs = mainModel.getCurrentConsecutiveSequences();
                Sequence chosenSeq = player.chooseSequence(currentConsecutiveSeqs);
                mainModel.resolveConsecutiveSequences(chosenSeq, currentConsecutiveSeqs);

                //update panel board
                pnlBoard.repaint();
                pnlBoard.revalidate();
            }
        }
        //change the turn
        nextTurn();
        /*//update the model
        mainModel.nextTurn();

        //update panel player
        pnlPlayer.changePlayer(mainModel.getCurrentPlayer());

        //update panel info
        pnlInfo.update();

        //update panel board
        pnlBoard.repaint();
        pnlBoard.revalidate();*/
    }

    /**
     * update the model and all displays to change the turn to the next player
     * this is probably the place where you determine what actions the AI will do
     */
    public void nextTurn(){
        //update the model
        mainModel.nextTurn();

        //update panel player
        pnlPlayer.changePlayer(mainModel.getCurrentPlayer());

        //update panel info
        pnlInfo.update();

        //update panel board
        pnlBoard.repaint();
        pnlBoard.revalidate();

        //if current player is AI
        if(mainModel.isCurrentPlayerAi() && !isInitializing && !mainModel.isFinished()){
            (new Thread(FrmMain.this)).start();
            System.out.println(mainModel.isFinished());
        }
    }

    /**
     * updates the model to select the given card, then updates the display by setting all cells than can be clicked
     * (cells where the given card can be put on). This method is called in {@code LblCard} when the label is clicked.
     * @param card the card to be selected
     */
    public void selectCard(Card card){
        //remove all highlights that has been set
        removeHighlights();

        //set the selected card and get the available cells for that card
        List<Integer> availableCellsIdx = mainModel.setSelectedCard(card);

        //if null, then setSelectedCard() function has failed to operate
        if(availableCellsIdx != null){
            //a boolean variable to indicate whether normal situation is to be active
            boolean isNormal = true;

            //if this card is no one-eyed jack, and there is no cell to put this card, prompt user to discard it
            if(availableCellsIdx.isEmpty() && !card.isOneEyedJack()){
                //ask whether user wants to exchange this card with a new one from the deck
                int input = JOptionPane.showConfirmDialog(this,
                        "You can exchange this card with a new card from the deck without wasting your move. " +
                                "Do you want to do it?", "Discard Dead Card", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                //if user say yes
                if(input == JOptionPane.YES_OPTION) {
                    mainModel.discardSelectedCard();
                    pnlPlayer.unselectLabel();
                    pnlPlayer.update();
                }

                //normal condition should not be triggered
                isNormal = false;
            }

            //if current player has two of the selected card, and both can be placed on the board
            // (both cells are not filled)
            if(!card.isOneEyedJack() && !card.isTwoEyedJack() &&
                    mainModel.getCurrentPlayer().isTwinCard(card) && availableCellsIdx.size() > 1){

                //ask whether user wants to use both cards simultaneously
                int input = JOptionPane.showConfirmDialog(this,
                        "You have two of the same card, so you can place both. Do you want to use both simultaneously?",
                        "Use Two Cards", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

                //if user say yes
                if(input == JOptionPane.YES_OPTION) {
                    //get current selected card's name
                    String tempName = mainModel.getSelectedCard().getName();

                    mainModel.playSelectedTwinCard(availableCellsIdx);

                    //if there are more sequences to choose, update display to give users chance to pick which sequence
                    if(mainModel.isConsecutiveSequences()){
                        activateCandidateSequences(true);
                    }
                    else{
                        nextTurn();
                    }

                    //normal condition should not be triggered
                    isNormal = false;
                }
            }
            //normal situation
            if(isNormal){
                //update the board with the available cells
                for(int i=0; i<availableCellsIdx.size(); i++){
                    int x = mainModel.getSequenceBoard().getTwoDimCellIdxX(availableCellsIdx.get(i));
                    int y = mainModel.getSequenceBoard().getTwoDimCellIdxY(availableCellsIdx.get(i));
                    pnlBoard.setHighlightedCell(x, y, true);
                }
            }
        }
    }

    /**
     * activate all cells that are the first cell of each sequence candidate. Sequence candidates are a set of
     * sequences that was formed by playing a single card, so the player must choose which sequence to be formed.
     * If there are more than one set of candidate sequences, the game model will determined the order to which sets
     * of candidate sequences will be processed (activated, marked and chosen).
     * Activate here means all cells will be clickable to form a sequence.
     * This method is used after each player plays his/her card(s) on the board.
     * @param isActivated if true then the cells will be activated (clickable), otherwise it will be deactivated.
     */
    public void activateCandidateSequences( boolean isActivated){
        //disable all labels in player's panel
        pnlPlayer.disableCards();

        //get all sequences in one direction
        List<Sequence> seqs = mainModel.getCurrentConsecutiveSequences();
        Sequence tempSeq;
        for(int i=0; i<seqs.size(); i++){
            tempSeq = seqs.get(i);

            //enable / disable the beginning of each sequence to be clickable and selectable
            int x = SequenceBoard.getTwoDimCellIdxX(tempSeq.getCellIdxAt(0));
            int y = SequenceBoard.getTwoDimCellIdxY(tempSeq.getCellIdxAt(0));
            pnlBoard.setHighlightedCell(x, y, isActivated);
            pnlBoard.setCandidateSequence(x, y, isActivated);

            //if disabling the candidate sequences (meaning that a sequence has been chosen / resolved)
            if(!isActivated) {
                markCandidateSequence(SequenceBoard.getOneDimCellIdx(x, y), false);
            }
        }
        seqs = null;
        tempSeq = null;
    }

    /**
     * mark all cells that belong to the given sequence, which is represented by
     * the first cell's index of the sequence. The given sequence will be a part of a set of sequence candidates
     * (from where player must choose which sequence to be formed). If there are more than one sets of candidates, the
     * game model will determine which one to be processed first (activated, marked, and chosen).
     * This method is used when player hover to the first cell of a sequence
     * candidate to show the sequence that will be formed from that cell.
     * @param startIdx the first cell's index of a sequence whose cells will be marked.
     * @param isMarked if true then the cells will be marked, otherwise they will be unmarked.
     */
    public void markCandidateSequence(int startIdx, boolean isMarked){
        List<Sequence> tempSeqs = mainModel.getCurrentConsecutiveSequences();

        //find the sequence with startIdx as its first cell's index
        for(int i=0; i<tempSeqs.size(); i++){
            //if found, then mark (or unmark) the rest of the sequence
            if(tempSeqs.get(i).isFirstIdx(startIdx)){
                int x, y;
                for(int ii=1; ii<Sequence.LENGTH; ii++){
                    x = SequenceBoard.getTwoDimCellIdxX(tempSeqs.get(i).getCellIdxAt(ii));
                    y = SequenceBoard.getTwoDimCellIdxY(tempSeqs.get(i).getCellIdxAt(ii));

                    if(!pnlBoard.lblCells[x][y].isSelected())
                        pnlBoard.setCandidateSequence(x, y, isMarked);
                }
                break;
            }
        }
        tempSeqs = null;
    }

    /**
     * choose which sequence to be formed, which is represented by the first cell's index of the sequence. The selected
     * sequence is a part of a set of sequence candidates. If there are more than one sets of candidates, the
     * game model will determine which one to be processed first (activated, marked, and chosen).
     * This method is used when player click the cell on the board to determine which sequence will be formed.
     * @param startIdx the first cell's index of the sequence to be formed.
     */
    public void chooseSequences(int startIdx){
        List<Sequence> tempSeqs = mainModel.getCurrentConsecutiveSequences();

        //find the sequence whose first cell's index is the same as the given one
        Sequence chosenSeq = null;
        for(int i=0; i<tempSeqs.size(); i++){
            if(tempSeqs.get(i).isFirstIdx(startIdx)){
                chosenSeq = tempSeqs.get(i);
                break;
            }
        }

        //improve for error checking
        if(chosenSeq != null) {
            //deactivate previous consecutive sequences
            activateCandidateSequences(false);

            //resolve current consecutive sequences
            mainModel.resolveConsecutiveSequences(chosenSeq, tempSeqs);
            tempSeqs = null;

            //check whether other consecutive sequences exist
            if(mainModel.isConsecutiveSequences()){
                //activate another consecutive sequences
                activateCandidateSequences(true);
            }
            //if no other consecutive sequences exist
            else{
                pnlPlayer.enableCards();
                nextTurn();
            }

        }
    }

    /**
     * remove all highlights (signs that show the cells are selected and can be clicked) from the cells.
     */
    public void removeHighlights(){
        //get the previous selected card to disable the previous available cells
        List<Integer> availableCellsIdx;
        if(mainModel.getSelectedCard() != null){
            availableCellsIdx = mainModel.getSequenceBoard().getAvailableCellsIdx(mainModel.getSelectedCard());
            for(int i=0; i<availableCellsIdx.size(); i++){
                int x = mainModel.getSequenceBoard().getTwoDimCellIdxX(availableCellsIdx.get(i));
                int y = mainModel.getSequenceBoard().getTwoDimCellIdxY(availableCellsIdx.get(i));
                pnlBoard.setHighlightedCell(x, y, false);
            }
        }
    }

    /**
     * method to start a new game, that is to prompt users for the total number of players and board allignment,
     * then recreate / reinitialize the game model.
     */
    public void initializeGame(){
        //pause the thread
        isInitializing = true;

        //determining the total number of players.
        int ttlPlayers;
        while(true){
            try{
                Object answer = JOptionPane.showInputDialog(this, "How many players?",
                        "Total Players", JOptionPane.QUESTION_MESSAGE,
                        null, null, "6");

                //if canceled, then exit
                if(answer == null)
                    System.exit(2);

                //check the number of total players
                ttlPlayers = Integer.parseInt(answer.toString());
                if(ttlPlayers%2 != 0)
                    throw new Exception("Total number of players must be an even number!");
                break;
            }catch (Exception e){
                JOptionPane.showMessageDialog(this, "Must be an even number between 2-12!",
                        "Error Total Players", JOptionPane.ERROR_MESSAGE);
            }
        }

        //determining how many AI
        boolean[] isAi = new boolean[ttlPlayers];
        String defaultIdxAi = "";
        for(int i=0; i<ttlPlayers; i++)
            defaultIdxAi += i + " ";
        while(true){
            try{
                Object answer = JOptionPane.showInputDialog(this, "Which players are AI? " +
                                "(Specify the index separated by space.)",
                        "Total AI", JOptionPane.QUESTION_MESSAGE,
                        null, null, defaultIdxAi);

                //if canceled, then exit
                if(answer == null)
                    System.exit(2);

                //check which players are AI
                if(!answer.toString().trim().equals("")) {
                    for (String eachIdx : answer.toString().trim().split(" ")) {
                        int idx = Integer.parseInt(eachIdx);
                        isAi[idx] = true;
                    }
                }
                break;
            }catch (Exception e){
                JOptionPane.showMessageDialog(this, "Please specify the index correctly",
                        "Error Total Players", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }

        //determining the board's arrangement
        String fileName = null;
        while(true){
            try{
                Object answer = JOptionPane.showInputDialog(this,
                        "Please specify the file name for the board's arrangement " +
                                "(leave it empty for random initialization)!",
                        "Board Structure", JOptionPane.QUESTION_MESSAGE, null, null,
                        "SequenceBoard1.txt");

                //if canceled, then exit
                if(answer == null)
                    System.exit(2);

                //if there is an input, store the option
                if(answer.toString().trim().equals("")){
                    break;
                }
                else {
                    Scanner scan = new Scanner(new File(answer.toString()));
                    fileName = answer.toString();
                    break;
                }
            }catch (Exception ex){
                JOptionPane.showMessageDialog(this, "File not found!",
                        "Error File Not Found", JOptionPane.ERROR_MESSAGE);
            }
        }

        //Create / reset the model
        if(mainModel == null){ //initialize the game model
            try {
                mainModel = new GameModel(ttlPlayers, fileName, isAi);
            }catch(Exception ex) {
                ex.printStackTrace();
                System.exit(2);
            }
        }
        else{ //reuse the game model by resetting it for a new game
            try {
                mainModel.newGame(ttlPlayers, fileName, isAi);
            }catch(Exception ex) {
                ex.printStackTrace();
                System.exit(2);
            }
        }
        isInitializing = false;
    }

    public void resetDisplay(){
        //reset / update the GUI to follow the model
        //panel player
        pnlPlayer.changePlayer(mainModel.getCurrentPlayer());

        //panel board
        pnlBoard.resetBoard();

        //panel info
        pnlInfo.resetPanel(mainModel);
    }

    public void run(){
        try {
            //Thread.sleep(100);
        }catch(Exception e) {
            e.printStackTrace();
        }

        //if(mainModel.isCurrentPlayerAi() && !mainModel.isFinished())
        if(mainModel.isCurrentPlayerAi() && !mainModel.isFinished() && !isInitializing)
            playAi((AI)mainModel.getCurrentPlayer());
    }

    public static void main(String[] args){
        FrmMain mainMenu = new FrmMain();
    }
}