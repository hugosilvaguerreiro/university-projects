package cmov.tecnico.ulisboa.pt.p2photo.gallery.tasks;

public interface AsyncResponse<T> {
    public void onResponse(T response);
    public void onError(Exception e);
}

