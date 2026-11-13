package se.yrgo.libraryapp.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import javax.sql.DataSource;

import com.radcortez.flyway.test.annotation.H2;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.exception.CredentialsException;
import se.yrgo.libraryapp.entities.UserId;

@Tag("integration")
@H2
class SessionDaoIntegrationTest {
    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test");
        ds = dataSource;
    }

    @Test
    void createSessionCanBeValidated() {
        SessionDao sessionDao = new SessionDao(ds);
        UserId userId = UserId.of(1);

        UUID sessionId = sessionDao.create(userId);

        assertThat(sessionDao.validate(sessionId)).isEqualTo(userId);
    }

    @Test
    void deletedSessionCannotBeValidated() {
        SessionDao sessionDao = new SessionDao(ds);
        UUID sessionId = sessionDao.create(UserId.of(1));

        sessionDao.delete(sessionId);

        assertThatThrownBy(() -> sessionDao.validate(sessionId))
                .isInstanceOf(CredentialsException.class);
    }

    @Test
    void expiredSessionCannotBeValidated() throws Exception {
        UUID sessionId = UUID.randomUUID();

        try (Connection connection = ds.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO session (id, user_id, created) VALUES (?, ?, ?)")) {
            statement.setString(1, sessionId.toString());
            statement.setInt(2, 1);
            statement.setTimestamp(
                    3, Timestamp.from(Instant.now().minusSeconds(8L * 24 * 60 * 60)));
            statement.executeUpdate();
        }

        SessionDao sessionDao = new SessionDao(ds);

        assertThatThrownBy(() -> sessionDao.validate(sessionId))
                .isInstanceOf(CredentialsException.class);
    }
}