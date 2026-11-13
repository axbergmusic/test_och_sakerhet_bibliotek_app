package se.yrgo.libraryapp.services;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;
import se.yrgo.libraryapp.dao.UserDao;
import se.yrgo.libraryapp.entities.LoginInfo;
import se.yrgo.libraryapp.entities.UserId;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@MockitoSettings(strictness = Strictness.STRICT_STUBS)
public class UserServiceTest {
    @Mock
    private UserDao userDao;

    @Mock PasswordEncoder encoder;

    @Test
    @SuppressWarnings("deprecation")
    void correctLogin() {
        final String userId = "1";
        final UserId id = UserId.of(userId);
        final String username = "testuser";
        final String password = "password";
        final String passwordHash = "password";
        final LoginInfo info = new LoginInfo(id, passwordHash);
        final PasswordEncoder encoder =
                org.springframework.security.crypto.password.NoOpPasswordEncoder.getInstance();

        when(userDao.getLoginInfo(username)).thenReturn(Optional.of(info));

        UserService userService = new UserService(userDao, encoder);

        assertThat(userService.validate(username,
                password)).isEqualTo(Optional.of(id));
    }

    @Test
    void registerHashesPasswordAndSendsHashToDao() {
        String name = "testuser";
        String realname = "Test User";
        String password = "secret";
        String passwordHash = "hashed-password";

        when(encoder.encode(password)).thenReturn(passwordHash);
        when(userDao.register(name, realname, passwordHash)).thenReturn(true);

        UserService userService = new UserService(userDao, encoder);

        assertThat(userService.register(name, realname, password)).isTrue();
        verify(userDao).register(name, realname, passwordHash);
    }

    @Test
    void isNameAvailableReturnsFalseForShortNameWithoutCallingDao() {
        UserService userService = new UserService(userDao, encoder);

        assertThat(userService.isNameAvailable("ab")).isFalse();
        verifyNoInteractions(userDao);
    }

    @Test
    void isNameAvailableReturnsFalseForNullWithoutCallingDao() {
        UserService userService = new UserService(userDao, encoder);

        assertThat(userService.isNameAvailable(null)).isFalse();
        verifyNoInteractions(userDao);
    }

    @Test
    void isNameAvailableChecksDaoForValidName() {
        when(userDao.isNameAvailable("testuser")).thenReturn(true);
        UserService userService = new UserService(userDao, encoder);

        assertThat(userService.isNameAvailable("testuser")).isTrue();
        verify(userDao).isNameAvailable("testuser");
    }
}

