package edu.hawaii.its.api.groupings;

import org.junit.jupiter.api.Test;

public class OwnerResultTest {

    @Test
    public void testOwnerResultConstructor() {
        OwnerResult ownerResult = new OwnerResult(
                "99997010", "Testf-iwt-a TestIAM-staff", "testiwta", "path:to:grouping1"
        );
        assert ownerResult.getUhUuid().equals("99997010");
        assert ownerResult.getName().equals("Testf-iwt-a TestIAM-staff");
        assert ownerResult.getUid().equals("testiwta");
        assert ownerResult.getPaths().size() == 1;
        assert ownerResult.getPaths().get(0).equals("path:to:grouping1");

        ownerResult.addPath("path:to:grouping2");
        assert ownerResult.getPaths().size() == 2;
        assert ownerResult.getPaths().get(1).equals("path:to:grouping2");
    }
}
