import java.util.List;
public class TeamRepository implements Repository<Team>
{
    private Database db;
    public TeamRepository(Database db)
    {
        this.db=db;
    }
    public Team findById(int id) throws Exception
    {
        return db.findTeam(id);
    }
    public List<Team> findAll() throws Exception
    {
        return db.teams();
    }
    public void save(Team item) throws Exception
    {
        db.saveTeam(item);
    }
    public void update(Team item) throws Exception
    {
        throw new UnsupportedOperationException("Team updates are not available");
    }
    public void delete(int id) throws Exception
    {
        throw new UnsupportedOperationException("Team deletion is not available");
    }
}
