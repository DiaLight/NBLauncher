package dialight.minecraft;

import java.io.IOException;

public class ForbiddenOperationException extends IOException {

    public ForbiddenOperationException() {
    }

    public ForbiddenOperationException(String message) {
        super(message);
    }

    public ForbiddenOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ForbiddenOperationException(Throwable cause) {
        super(cause);
    }

    public boolean isInvalidUsernameOrPassword() {
        return getMessage().equals("Invalid credentials. Invalid username or password.");
    }
    public boolean isAccountMigrated() {
        return getMessage().equals("Invalid credentials. Account migrated, use email as username.");
    }

}
