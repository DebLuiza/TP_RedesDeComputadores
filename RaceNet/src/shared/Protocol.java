package shared;

public final class Protocol {
    public static final int DEFAULT_TCP_PORT = 5000;
    public static final int DEFAULT_UDP_PORT = 5001;

    public static final String ENTER = "ENTRAR";
    public static final String READY = "PRONTO";
    public static final String START_RACE = "INICIAR_CORRIDA";
    public static final String FINISH = "FINALIZAR";
    public static final String WINNER = "VENCEDOR";
    public static final String POSITION = "POSICAO";
    public static final String RANKING = "RANKING";
    public static final String STATUS = "STATUS";
    public static final String ENTER_OK = "ENTRAR_OK";
    public static final String RESET = "RESET";

    private Protocol() {
    }

    public static String[] split(String message) {
        return message == null ? new String[0] : message.trim().split(";");
    }
}
