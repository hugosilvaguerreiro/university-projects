package aasma_proj.agent.modules;

import aasma_proj.agent.Agent;
import aasma_proj.agent.CommunicatingAgent;
import aasma_proj.agent.modules.planningModules.PathPlanningModule;
import aasma_proj.items.Item;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;

public class Message {
    private CommunicatingAgent sender;
    private ArrayList<Object> params;
    public enum Method{FETCH_INGREDIENT, HELP, ACK, CARRY_ON, MOVE_AWAY, MOVE_BACK}
    private Method method;

    public Message(CommunicatingAgent sender, Method method, Object... params){
        this.sender = sender;
        this.method = method;
        this.params = new ArrayList<>();
        Collections.addAll(this.params, params);
    }

    public CommunicatingAgent getSender() {
        return sender;
    }

    public Method getMethod() {
        return method;
    }

    public ArrayList<Object> getParams(){
        return params;
    }

    public Object getParam(int index){
        return params.get(index);
    }
}
