package se.yrgo.libraryapp.dao;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.Optional;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import com.radcortez.flyway.test.annotation.H2;
import se.yrgo.libraryapp.entities.User;
import se.yrgo.libraryapp.entities.UserId;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Tag("integration")
@H2
public class UserDaoIntegrationTest {

    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        // this way we do not need to create a new datasource every time
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        UserDaoIntegrationTest.ds = ds;
    }

    @Test
    void getUserById() {
        // this data comes from the test migration files
        final String username = "test";
        final UserId userId = UserId.of(1);
        UserDao userDao = new UserDao(ds);
        Optional<User> maybeUser = userDao.get(Integer.toString(userId.getId()));
        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.get().getName()).isEqualTo(username);
        assertThat(maybeUser.get().getId()).isEqualTo(userId);
    }

    @Test
    void getReturnsEmptyWhenUserDoesNotExist() {
        UserDao userDao = new UserDao(ds);

        assertThat(userDao.get("2147483647")).isEmpty();
    }

    @Test
    void getLoginInfoReturnsSeededUser() {
        UserDao userDao = new UserDao(ds);

        assertThat(userDao.getLoginInfo("test"))
                .hasValueSatisfying(info -> {
                    assertThat(info.getUserId()).isEqualTo(UserId.of(1));
                    assertThat(info.getPasswordHash()).isNotBlank();
                });
    }

    @Test
    void getLoginInfoReturnsEmptyWhenUserDoesNotExist() {
        UserDao userDao = new UserDao(ds);

        assertThat(userDao.getLoginInfo("missing-" + UUID.randomUUID())).isEmpty();
    }

    @Test
    void isNameAvailableReflectsDatabaseContents() {
        UserDao userDao = new UserDao(ds);

        assertThat(userDao.isNameAvailable("test")).isFalse();
        assertThat(userDao.isNameAvailable("new-" + UUID.randomUUID())).isTrue();
    }

    @Test
    void registerStoresUserAndAssignsUserRole() throws SQLException {
        String name = "integration-" + UUID.randomUUID();
        String realname = "Integration Test";
        String passwordHash = "test-hash";

        UserDao userDao = new UserDao(ds);

        assertThat(userDao.register(name, realname, passwordHash)).isTrue();

        try (Connection conn = ds.getConnection();
             PreparedStatement userQuery = conn.prepareStatement(
                     "SELECT realname, password_hash FROM user WHERE user = ?")) {
            userQuery.setString(1, name);

            try (ResultSet rs = userQuery.executeQuery()) {
                assertThat(rs.next()).isTrue();
                assertThat(rs.getString("realname")).isEqualTo(realname);
                assertThat(rs.getString("password_hash")).isEqualTo(passwordHash);
            }

            try (PreparedStatement roleQuery = conn.prepareStatement(
                    "SELECT COUNT(*) FROM user_role ur "
                            + "JOIN user u ON u.id = ur.user_id "
                            + "WHERE u.user = ? AND ur.role_id = 2")) {
                roleQuery.setString(1, name);

                try (ResultSet rs = roleQuery.executeQuery()) {
                    assertThat(rs.next()).isTrue();
                    assertThat(rs.getInt(1)).isEqualTo(1);
                }
            }
        }
    }

    @Test
    void registerReturnsFalseForDuplicateName() {
        UserDao userDao = new UserDao(ds);

        assertThat(userDao.register("test", "Another Test", "test-hash")).isFalse();
    }
}