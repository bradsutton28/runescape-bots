import java.awt.*;

import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.AdvancedMessageListener;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.message.Message;
import static org.dreambot.api.methods.walking.impl.Walking.*;
import org.dreambot.api.wrappers.interactive.interact.Interactable;
import org.dreambot.core.S;

import javax.print.DocFlavor;
import javax.swing.*;

@ScriptManifest(author="Brad",
        description = "for fun bar buyer",
        name = "bar buyer bot",
        category = Category.MISC,
        version = 1.0)
public class barbuyer extends AbstractScript{


    State state;
    Area barArea = new Area(2953, 3376, 2961, 3365);
    Area bankArea = new Area(2943, 3370, 2949, 3366);

    private enum State{
        STOP, TOBAR, BUYING, BANKING, TOBANK
    }

    @Override
    public void onStart(){
        log("hi");
    }
    //Gets current state based on logic, to then do something when the programs looping
    private State getState(){
        if(!Client.isLoggedIn()){
            state=State.STOP;
        }else if(!barArea.contains(getLocalPlayer()) && !Inventory.isFull() && Inventory.contains("Coins")){
            state=State.TOBAR;
        }else if(barArea.contains(getLocalPlayer()) && !Inventory.isFull() && Inventory.contains("Coins")){
            state=State.BUYING;
        }else if(!bankArea.contains(getLocalPlayer()) && Inventory.isFull()){
            state=State.TOBANK;
        }else if(bankArea.contains(getLocalPlayer()) && Inventory.isFull()){
            state=State.BANKING;
        }

        return state;
    }

    @Override
    public int onLoop() {

        // Function to do something based on state
        switch(getState()){
            case STOP:
                log("stop script");
                stop();
                break;
            //Buys the ming bombs from shop
            case BUYING:
                NPC bargirl = NPCs.closest(c -> c != null && c.getName().contentEquals("Kaylee"));
                bargirl.interact("Talk-to");
                sleep(1500);
                Dialogues d = Dialogues.getDialogues();
                while(Dialogues.inDialogue()){
                    if(Dialogues.canContinue()){
                        Dialogues.spaceToContinue();
                    }
                    if(Dialogues.getOptions() != null){
                        Dialogues.chooseOption("I'll try the Mind Bomb.");
                    }
                }
                break;
            // walks to bar
            case TOBAR:
                if(!getLocalPlayer().isMoving()){
                    walk(barArea.getRandomTile());
                }
                sleepUntil(() -> barArea.contains(getLocalPlayer()), 5000);
                break;
            // walks to bank
            case TOBANK:
                if(!getLocalPlayer().isMoving()) {
                    walk(bankArea.getRandomTile());
                }
                sleepUntil(() -> bankArea.contains(getLocalPlayer()), 5000);
                break;
            // banks your bombs
            case BANKING:
                GameObject booth = GameObjects.closest(c -> c != null && c.getName().contentEquals("Bank booth")
                        && c.hasAction("Bank"));
                booth.interact("Bank");
                sleep(1500);
                Bank bank = Bank.getBank();
                if(Bank.isOpen()){
                    Bank.deposit("Wizard's mind bomb", 27);
                    sleep(750);
                    Bank.close();
                }
                break;
        }

        return 400;
    }

    @Override
    public void onExit(){
        log("bye");
    }
}
