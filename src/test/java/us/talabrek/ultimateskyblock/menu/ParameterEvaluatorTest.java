package us.talabrek.ultimateskyblock.menu;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ParameterEvaluatorTest {

    @Test
    public void testEval() throws Exception {
        Map<String,String> params = new HashMap<>();
        params.put("$PLAYER", "R4zo\u00a79rax");
        params.put("$IS_TRUE", "false");
        ParameterEvaluator evaluator = new ParameterEvaluator(params);

        assertThat(evaluator.eval("&4Hello $PLAYER, how are $YOU, 100$ richer?"), is("&4Hello R4zo\u00a79rax, how are \u00a7cUnknown, 100$ richer?"));
    }

    @Test
    public void testInverse() {
        Map<String,String> params = new HashMap<>();
        params.put("$general.something", "true");
        ParameterEvaluator evaluator = new ParameterEvaluator(params);

        assertThat(evaluator.eval("enabled: $general.something, and !$general.something"), is("enabled: true, and !true"));
    }
}