import java.awt.*;

import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
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
        description = "for fun wcer",
        name = "varrock chopper",
        category = Category.FISHING,
        version = 1.0)
public class varrockcutter extends AbstractScript{

    State state;
    Area treeArea = new Area(3161, 3422, 3172, 3409);
    Area bankArea = new Area(3180, 3441, 3186, 3433);

    private enum State{
        STOP, TOSPOT, CUTTING, TOBANK, BANKING, NOTCUTTING
    }

    @Override
    public void onStart(){
        log("hi");
    }

    private State getState(){
        if(!Client.isLoggedIn()){
            state=State.STOP;
        }else if(!treeArea.contains(getLocalPlayer()) && !Inventory.isFull() && Inventory.contains("Rune axe")){
            state=State.TOSPOT;
        }else if(!bankArea.contains(getLocalPlayer()) && Inventory.isFull()){
            state=State.TOBANK;
        }else if(treeArea.contains(getLocalPlayer()) && !Inventory.isFull() && getLocalPlayer().isAnimating()){
            state=State.CUTTING;
        }else if(treeArea.contains(getLocalPlayer()) && !Inventory.isFull() && !getLocalPlayer().isAnimating()){
            state=State.NOTCUTTING;
        }else if(bankArea.contains(getLocalPlayer()) && Inventory.isFull()){
            state=State.BANKING;
        }
        return state;
    }

    @Override
    public int onLoop() {

        switch (getState()) {
            case STOP:
                log("stop script");
                stop();
                break;

            case TOSPOT:
                if (!getLocalPlayer().isMoving()) {
                    walk(treeArea.getRandomTile());
                }
                sleepUntil(() -> treeArea.contains(getLocalPlayer()), 5000);
                break;

            case TOBANK:
                if (!getLocalPlayer().isMoving()) {
                    walk(bankArea.getRandomTile());
                }
                sleepUntil(() -> bankArea.contains(getLocalPlayer()), 5000);
                break;

            case CUTTING:
                sleepUntil(() -> !getLocalPlayer().isAnimating(), 5000);
                break;

            case NOTCUTTING:
                GameObject tree = GameObjects.closest(c -> c != null && c.getName().contentEquals("Oak")
                        && c.hasAction("Chop down"));
                if(!getLocalPlayer().isMoving()) {
                    tree.interact("Chop down");
                }

                sleepUntil(() -> !getLocalPlayer().isAnimating(), 2000);
                Inventory.dropAll("Logs");
                break;

            case BANKING:
                GameObject booth = GameObjects.closest(c -> c != null && c.getName().contentEquals("Bank booth")
                        && c.hasAction("Bank"));
                booth.interact("Bank");
                sleep(2500);
                if (Bank.isOpen()) {
                    Bank.deposit("Oak logs", 27);
                    sleep(750);
                    Bank.close();
                }
                break;
        }
                return 400;
        }

        @Override
        public void onExit() {
            log("bye");
        }
    }