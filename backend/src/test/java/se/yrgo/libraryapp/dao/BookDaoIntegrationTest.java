package se.yrgo.libraryapp.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import com.radcortez.flyway.test.annotation.H2;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import se.yrgo.libraryapp.entities.Book;
import se.yrgo.libraryapp.entities.BookEdition;
import se.yrgo.libraryapp.entities.BookId;
import se.yrgo.libraryapp.entities.BookLoan;
import se.yrgo.libraryapp.entities.DdsClassification;
import se.yrgo.libraryapp.entities.UserId;

@Tag("integration")
@H2
class BookDaoIntegrationTest {
    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:test");
        ds = dataSource;
    }

    @Test
    void getReturnsAvailableBook() {
        BookDao bookDao = new BookDao(ds);

        Book book = bookDao.get(BookId.of(1)).orElseThrow();

        assertThat(book.getAvailable()).isTrue();
        assertThat(book.getEdition().getIsbn()).isEqualTo("9781509302000");
        assertThat(book.getEdition().getTitle()).isEqualTo("T-SQL Fundamentals");
    }

    @Test
    void getReturnsUnavailableBookWhenItIsOnLoan() {
        BookDao bookDao = new BookDao(ds);

        Book book = bookDao.get(BookId.of(2)).orElseThrow();

        assertThat(book.getAvailable()).isFalse();
    }

    @Test
    void getReturnsEmptyForUnknownBook() {
        BookDao bookDao = new BookDao(ds);

        assertThat(bookDao.get(BookId.of(999999))).isEmpty();
    }

    @Test
    void findReturnsEmptyWhenNoSearchCriteriaAreProvided() {
        BookDao bookDao = new BookDao(ds);

        assertThat(bookDao.find(null, null, null)).isEmpty();
        assertThat(bookDao.find(" ", " ", " ")).isEmpty();
    }

    @Test
    void findMatchesTitleAndIncludesCopyCount() {
        BookDao bookDao = new BookDao(ds);

        Set<BookEdition> results = bookDao.find(null, "Pippi", null);

        assertThat(results)
                .anySatisfy(edition -> {
                    assertThat(edition.getIsbn()).isEqualTo("9789129697285");
                    assertThat(edition.getTitle()).isEqualTo("Här kommer Pippi Långstrump");
                    assertThat(edition.getCount()).isEqualTo(3);
                });
    }

    @Test
    void findReturnsEmptyWhenThereAreNoMatches() {
        BookDao bookDao = new BookDao(ds);

        assertThat(bookDao.find(null, "no matching title", null)).isEmpty();
    }

    @Test
    void withClassReturnsEditionsInThatClassification() {
        BookDao bookDao = new BookDao(ds);

        Set<BookEdition> editions =
                bookDao.withClass(new DdsClassification(5, null));

        assertThat(editions)
                .extracting(BookEdition::getIsbn)
                .contains("9781509302000");
    }

    @Test
    void loansReturnsLoansForRequestedUser() {
        BookDao bookDao = new BookDao(ds);

        List<BookLoan> loans = bookDao.loans(UserId.of(2));

        assertThat(loans).hasSize(2);
        assertThat(loans)
                .allSatisfy(loan -> assertThat(loan.getUser()).isEqualTo(UserId.of(2)));
        assertThat(loans)
                .extracting(loan -> loan.getBook().getId())
                .containsExactly(BookId.of(4), BookId.of(2));
    }

    @Test
    void loansReturnsEmptyWhenUserHasNoLoans() {
        BookDao bookDao = new BookDao(ds);

        assertThat(bookDao.loans(UserId.of(1))).isEmpty();
    }

    @Test
    void overdueLoansReturnsSeededOverdueLoans() {
        BookDao bookDao = new BookDao(ds);

        List<BookLoan> loans = bookDao.overdueLoans();

        assertThat(loans).hasSize(2);
        assertThat(loans)
                .extracting(loan -> loan.getBook().getId())
                .containsExactly(BookId.of(4), BookId.of(2));
        assertThat(loans).allSatisfy(BookLoan::getOverdue);
    }

    @Test
    void returnBookDeletesExistingLoan() {
        BookDao bookDao = new BookDao(ds);

        assertThat(bookDao.returnBook(BookId.of(2))).isTrue();
        assertThat(bookDao.get(BookId.of(2)).orElseThrow().getAvailable()).isTrue();
    }

    @Test
    void returnBookReturnsFalseWhenBookIsNotOnLoan() {
        BookDao bookDao = new BookDao(ds);

        assertThat(bookDao.returnBook(BookId.of(1))).isFalse();
    }
}