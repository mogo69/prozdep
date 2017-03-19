package department.dao;

import department.model.bo.Department;
import department.model.bo.Scientist;
import department.model.bo.Teacher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import util.DateUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class TeacherDAO implements ITeacherDAO{

    private final static String FIND_ALL = "SELECT * " +
            "FROM teacher t " +
            "INNER JOIN scientist s ON t.scientist_id = s.id;";
    private final static String COUNT = "SELECT COUNT(*) FROM teacher;";
    private final static String FIND_ALL_WITH_PAGINATION = "SELECT * " +
            "FROM teacher t " +
            "INNER JOIN scientist s ON t.scientist_id = s.id " +
            "ORDER BY s.id " +
            "LIMIT ? OFFSET ?;";

    private final static String COUNT_WIHT_NAME_LIKE = "SELECT COUNT(*) " +
            "FROM teacher t " +
            "INNER JOIN scientist s ON t.scientist_id = s.id " +
            "WHERE s.name LIKE CONCAT('%', ? , '%')  COLLATE utf8_general_ci;";
    private final static String FIND_ALL_WITH_NAME_LIKE= "SELECT * " +
            "FROM teacher t " +
            "INNER JOIN scientist s ON t.scientist_id = s.id " +
            "WHERE s.name LIKE CONCAT('%', ? , '%') COLLATE utf8_general_ci;";
    private final static String FIND_ALL_WITH_NAME_LIKE_WITH_PAGINATION = "SELECT * " +
            "FROM teacher t " +
            "INNER JOIN scientist s ON t.scientist_id = s.id " +
            "WHERE s.name LIKE CONCAT('%', ? , '%') COLLATE utf8_general_ci " +
            "ORDER BY s.id " +
            "LIMIT ? OFFSET ?;";

    private final static String FIND = "SELECT * " +
            "FROM teacher t " +
            "INNER JOIN scientist s ON t.scientist_id = s.id " +
            "WHERE t.scientist_id=?;";

    private final static String EAGER_FIND = "SELECT " +
            "t.scientist_id AS teacher_id, " +
            "s.name AS teacher_name, " +
            "s.phone AS teacher_phone, " +
            "t.position AS teacher_position, " +
            "t.degree AS teacher_degree, " +
            "t.start_date AS teacher_start_date, " +
            "t.department_id AS department_id, " +
            "d.name AS department_name, " +
            "d.phone AS department_phone " +
            "FROM teacher t " +
            "INNER JOIN scientist s ON t.scientist_id = s.id " +
            "INNER JOIN department d ON t.department_id = d.id " +
            "WHERE t.scientist_id=?;";

    private final static String INSERT = "INSERT INTO teacher (" +
            "scientist_id, " +
            "position, " +
            "degree, " +
            "start_date, " +
            "department_id " +
            ") VALUES(?,?,?,?,?);";
    private final static String UPDATE = "UPDATE teacher " +
            "SET position = ?, degree = ?,  start_date = ?, department_id = ? " +
            "WHERE scientist_id = ?;";

    private final static String REMOVE = "DELETE FROM teacher WHERE scientist_id = ?;";

    private final TeacherMapper teacherMapper = new TeacherMapper();
    private final EagerTeacherMapper eagerTeacherMapper = new EagerTeacherMapper();

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private IScientistDAO scientistDAO;

    @Override
    public List<Teacher> findAll() {
        return jdbcTemplate.query(FIND_ALL, teacherMapper);
    }

    @Override
    public int count() {
        return jdbcTemplate.queryForObject(COUNT, Integer.class);
    }

    @Override
    public List<Teacher> findAll(long limit, long offset) {
        return jdbcTemplate.query(FIND_ALL_WITH_PAGINATION, new Object[] { limit, offset }, teacherMapper);
    }

    @Override
    public int count(String name) {
        return jdbcTemplate.queryForObject(COUNT_WIHT_NAME_LIKE, new Object[] {name}, Integer.class);
    }

    @Override
    public List<Teacher> findAll(String name) {
        return jdbcTemplate.query(FIND_ALL_WITH_NAME_LIKE, new Object[] {name}, teacherMapper);
    }

    @Override
    public List<Teacher> findAll(String name, long limit, long offset) {
        return jdbcTemplate.query(FIND_ALL_WITH_NAME_LIKE_WITH_PAGINATION, new Object[] {name, limit, offset }, teacherMapper);
    }

    @Override
    public Teacher find(int scientistId) {
        List<Teacher> teachers = jdbcTemplate.query(FIND, new Object[]{ scientistId }, teacherMapper);
        return (teachers.isEmpty()) ? null : teachers.get(0);
    }

    @Override
    public Teacher find(int scientistId, boolean isEager) {
        if(isEager){
            List<Teacher> teachers = jdbcTemplate.query(EAGER_FIND, new Object[]{scientistId}, eagerTeacherMapper);
            return (teachers.isEmpty()) ? null : teachers.get(0);
        } else {
            return find(scientistId);
        }
    }

    public Teacher insert(Teacher teacher) {
        if(teacher.getId() == 0){
            Scientist scientist = scientistDAO.insert(teacher);
            teacher = teacher.toBuilder().id(scientist.getId()).build();
        }
        Object [] values = {
                teacher.getId(),
                teacher.getPosition(),
                teacher.getDegree(),
                DateUtil.convertToSqlDate(teacher.getStartDate()),
                teacher.getDepartment().getId()
        };
        jdbcTemplate.update(INSERT, values);
        return teacher;
    }

    public void update(Teacher teacher) {
        scientistDAO.update(teacher);
        Object [] values = {
                teacher.getPosition(),
                teacher.getDegree(),
                DateUtil.convertToSqlDate(teacher.getStartDate()),
                teacher.getDepartment().getId(),
                teacher.getId()
        };
        jdbcTemplate.update(UPDATE, values);
    }

    public void remove(Teacher teacher) {
        jdbcTemplate.update(REMOVE, new Object[]{
                teacher.getId()
        });
    }

    @Override
    public List<Teacher> getTeachersByDepertmentId(int department) {
        return null;
    }

    @Override
    public List<Teacher> getTeachersByTopicId(int topic) {
        return null;
    }

    @Override
    public List<Teacher> getTeachersByPaperId(int paper) {
        return null;
    }

    @Override
    public List<Teacher> getTeachersByDepertmentId(int department, boolean isEager) {
        return null;
    }

    @Override
    public List<Teacher> getTeachersByTopicId(int topic, boolean isEager) {
        return null;
    }

    @Override
    public List<Teacher> getTeachersByPaperId(int paper, boolean isEager) {
        return null;
    }

    private class TeacherMapper implements RowMapper<Teacher>{
        @Override
        public Teacher mapRow(ResultSet rs, int i) throws SQLException {
            /*Teacher teacher = new Teacher();
            teacher.setId(rs.getInt("scientist_id"));
            teacher.setName(rs.getString("name"));
            teacher.setPhone(rs.getString("phone"));
            teacher.setDepartment(new Department(rs.getInt("department_id")));
            teacher.setPosition(rs.getString("position"));
            teacher.setDegree(rs.getString("degree"));
            teacher.setStartDate(rs.getDate("start_date"));
            return teacher;*/
            return Teacher
                    .builder()
                        .id(rs.getInt("scientist_id"))
                        .name(rs.getString("name"))
                        .phone(rs.getString("phone"))
                        .position(rs.getString("position"))
                        .degree(rs.getString("degree"))
                        .startDate(rs.getTimestamp("start_date"))
                    .department(Department
                            .builder()
                                .id(rs.getInt("department_id"))
                            .build())
                    .build();
        }
    }

    private class EagerTeacherMapper implements RowMapper<Teacher>{
        @Override
        public Teacher mapRow(ResultSet rs, int i) throws SQLException {
            /*Teacher teacher = new Teacher();
            teacher.setId(rs.getInt("teacher_id"));
            teacher.setName(rs.getString("teacher_name"));
            teacher.setPhone(rs.getString("teacher_phone"));
            teacher.setDepartment(new Department(
                    rs.getInt("department_id"),
                    rs.getString("department_name"),
                    rs.getString("department_phone")));
            teacher.setPosition(rs.getString("teacher_position"));
            teacher.setDegree(rs.getString("teacher_degree"));
            teacher.setStartDate(rs.getDate("teacher_start_date"));
            return teacher;*/
            return Teacher
                    .builder()
                        .id(rs.getInt("teacher_id"))
                        .name(rs.getString("teacher_name"))
                        .phone(rs.getString("teacher_phone"))
                        .position(rs.getString("teacher_position"))
                        .degree(rs.getString("teacher_degree"))
                        .startDate(rs.getTimestamp("teacher_start_date"))
                    .department(Department
                            .builder()
                                .id(rs.getInt("department_id"))
                                .name(rs.getString("department_name"))
                                .phone(rs.getString("department_phone"))
                            .build())
                    .build();
        }
    }
}