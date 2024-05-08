package org.netcheck.netcheck;

import java.util.List;

public record ConfigNetCheck(long periodicite, List<Host> hostList) {

}
