import java.awt.*;

import org.dreambot.api.Client;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.listener.AdvancedMessageListener;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.message.Message;
import static org.dreambot.api.methods.walking.impl.Walking.*;
import org.dreambot.api.wrappers.interactive.interact.Interactable;
import org.dreambot.core.S;

import javax.print.DocFlavor;
import javax.swing.*;

@ScriptManifest(author="Brad",
        description = "for fun fish bot",
        name = "fish bot",
        category = Category.FISHING,
        version = 1.0)
public class fishingbot extends AbstractScript{

    State state;
    Area fisharea = new Area(3238, 3156, 3247, 3145);

    private enum State{
        STOP, TOSPOT, FISHING, NOTFISHING, DROPPING
    }

    @Override
    public void onStart(){
        log("hi");
    }

    private State getState(){
        if(!Client.isLoggedIn()){
            state=State.STOP;
        }else if(!fisharea.contains(getLocalPlayer())){
            state=State.TOSPOT;
        }else if(fisharea.contains(getLocalPlayer()) && !Inventory.isFull() && !getLocalPlayer().isAnimating()){
            state=State.NOTFISHING;
        }else if(fisharea.contains(getLocalPlayer()) && !Inventory.isFull() && getLocalPlayer().isAnimating()){
            state=State.FISHING;
        }else if(fisharea.contains(getLocalPlayer()) && Inventory.isFull()){
            state=State.DROPPING;
        }
        return state;
    }

    @Override
    public int onLoop() {

        switch(getState()){
            case STOP:
                log("stop script");
                stop();
                break;

            case FISHING:
                log("Fishing");
                sleep(200);
                break;

            case TOSPOT:
                walk(fisharea.getRandomTile());
                break;

            case NOTFISHING:
                NPC fishspot = NPCs.closest(f -> f != null && f.getName().contentEquals("Fishing spot"));
                fishspot.interact("Net");
                sleep(500);
                break;

            case DROPPING:
                Inventory.dropAll("Raw shrimps");
                Inventory.dropAll("Raw anchovies");
                break;
        }

        return 400;
    }

    @Override
    public void onExit(){
        log("bye");
    }
}
