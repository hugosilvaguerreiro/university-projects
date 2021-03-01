package pt.ulisboa.tecnico.cnv.system_tester.scripts;

import java.util.*;

public class ScriptRunner {

    public static Map<String, DummyScript> availableScripts;
    private static ScriptRunner instance;

    private ScriptRunner(Scanner s) {
        availableScripts = new HashMap<>();
        availableScripts.put("test", new DummyScript());
    }

    public static ScriptRunner getInstance(Scanner s) {
        if(instance == null) {
            instance = new ScriptRunner(s);
        }
        return instance;
    }

    public String[] listAvailableScripts() {
        return availableScripts.keySet().toArray(new String[availableScripts.keySet().size()]);
    }

    public void runScript(String script) {
        Optional.ofNullable(availableScripts.get(script))
                .orElse(new DummyScript()).run();
    }
}
