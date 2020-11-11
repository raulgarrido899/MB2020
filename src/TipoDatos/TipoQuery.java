package TipoDatos;

public class TipoQuery {

    private final String id;
    private final String query;

    public TipoQuery(String id, String query) {
        this.id = id;
        this.query = query;
    }

    public String getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

}
