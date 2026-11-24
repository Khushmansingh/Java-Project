import java.util.List;
public interface Repository<T>
{
    T findById(int id) throws Exception;
    List<T> findAll() throws Exception;
    void save(T item) throws Exception;
    void update(T item) throws Exception;
    void delete(int id) throws Exception;
}
