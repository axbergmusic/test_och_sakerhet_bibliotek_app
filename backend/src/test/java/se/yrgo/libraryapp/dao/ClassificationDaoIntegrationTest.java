package se.yrgo.libraryapp.dao;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;

import com.radcortez.flyway.test.annotation.H2;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import se.yrgo.libraryapp.entities.DdsClassification;

@Tag("integration")
@H2
class ClassificationDaoIntegrationTest {
    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test");
        ds = dataSource;
    }

    @Test
    void getAvailableReturnsOnlyClassificationsUsedByBooks() {
        ClassificationDao classificationDao = new ClassificationDao(ds);

        assertThat(classificationDao.getAvailable())
                .contains(
                        new DdsClassification(5, "Computer programming, programs and data"),
                        new DdsClassification(898, "Scandinavian"))
                .doesNotContain(new DdsClassification(10, "Bibliography"));
    }
}