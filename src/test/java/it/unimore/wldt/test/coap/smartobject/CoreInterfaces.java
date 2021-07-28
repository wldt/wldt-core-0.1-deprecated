package it.unimore.wldt.test.coap.smartobject;


/**
 +--------------+---------+-----------------+--------------------+
 | Interface    | if=     | Methods         | Content-Formats    |
 +--------------+---------+-----------------+--------------------+
 | Link List    | core.ll | GET             | link-format        |
 |              |         |                 |                    |
 | Batch        | core.b  | GET, PUT, POST  | senml              |
 |              |         |                 |                    |
 | Linked Batch | core.lb | GET, PUT, POST, | link-format, senml |
 |              |         |                 |                    |
 |              |         | DELETE          |                    |
 |              |         |                 |                    |
 | Sensor       | core.s  | GET             | senml,             |
 |              |         |                 |                    |
 |              |         |                 | text/plain         |
 |              |         |                 |                    |
 | Parameter    | core.p  | GET, PUT        | senml,             |
 |              |         |                 |                    |
 |              |         |                 | text/plain         |
 |              |         |                 |                    |
 | Read-only    | core.rp | GET             | senml,             |
 |              |         |                 |                    |
 | Parameter    |         |                 | text/plain         |
 |              |         |                 |                    |
 | Actuator     | core.a  | GET, PUT, POST  | senml,             |
 |              |         |                 |                    |
 |              |         |                 | text/plain         |
 +--------------+---------+-----------------+--------------------+
 */

public enum CoreInterfaces {

    CORE_LL("core.ll"),
    CORE_B("core.b"),
    CORE_LB("core.lb"),
    CORE_S("core.s"),
    CORE_P("core.p"),
    CORE_RP("core.rp"),
    CORE_A("core.a");

    private String value;

    CoreInterfaces(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
