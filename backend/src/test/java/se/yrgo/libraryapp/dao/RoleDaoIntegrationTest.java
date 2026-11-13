package se.yrgo.libraryapp.dao;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import com.radcortez.flyway.test.annotation.H2;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import se.yrgo.libraryapp.entities.Role;
import se.yrgo.libraryapp.entities.UserId;

@Tag("integration")
@H2
class RoleDaoIntegrationTest {
    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test");
        ds = dataSource;
    }

    @Test
    void getReturnsRolesAssignedToUser() {
        RoleDao roleDao = new RoleDao(ds);

        assertThat(roleDao.get(UserId.of(1))).containsExactly(Role.ADMIN);
        assertThat(roleDao.get(UserId.of(2)))
                .containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
    }

    @Test
    void getReturnsEmptyWhenUserHasNoRoles() {
        RoleDao roleDao = new RoleDao(ds);

        assertThat(roleDao.get(UserId.of(999))).isEmpty();
    }
}