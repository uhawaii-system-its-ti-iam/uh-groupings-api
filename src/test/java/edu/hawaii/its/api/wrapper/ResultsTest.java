package edu.hawaii.its.api.wrapper;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ResultsTest {

    private Results results;

    @BeforeEach
    public void beforeEach() {
        results = new Results() {
            @Override
            public String getResultCode() {
                return "";
            }
        };
    }

    @Test
    public void isEmpty() {
        assertThat(results.isEmpty(null), is(true));
        assertThat(results.isEmpty(new Object[] {}), is(true));
        assertThat(results.isEmpty(new Object[0]), is(true));
        assertThat(results.isEmpty(new Object[] { new Object() }), is(false));
    }

    @Test
    public void getBoolean() {
        assertThat(results.getBoolean(null), is(false));
        assertThat(results.getBoolean(""), is(false));
        assertThat(results.getBoolean("T"), is(true));
        assertThat(results.getBoolean("F"), is(false));
    }

    @Test
    public void getResultCode() {
        assertThat(results.getResultCode(), equalTo(""));
    }

}
