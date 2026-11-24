import java.util.List;
public class DriverRepository implements Repository<Driver>
{
    private Database db;
    public DriverRepository(Database db)
    {
        this.db=db;
    }
    public Driver findById(int id) throws Exception
    {
        return db.findDriver(id);
    }
    public List<Driver> findAll() throws Exception
    {
        return db.drivers();
    }
    public void save(Driver item) throws Exception
    {
        db.saveDriver(item);
    }
    public void update(Driver item) throws Exception
    {
        db.updateDriver(item);
    }
    public void delete(int id) throws Exception
    {
        throw new UnsupportedOperationException("Driver deletion is not available");
    }
}
