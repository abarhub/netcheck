package org.netcheck.netcheck.config;

import java.util.List;

public record ConfigNetCheck(long periodicite, List<Host> hostList) {

}
