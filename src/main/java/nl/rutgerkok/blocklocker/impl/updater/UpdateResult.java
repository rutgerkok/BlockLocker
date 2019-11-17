package nl.rutgerkok.blocklocker.impl.updater;

import java.io.IOException;

import com.google.common.base.Preconditions;
import com.google.gson.JsonObject;

/**
 * Result of an update attempt.
 *
 * @see Updater
 *
 */
final class UpdateResult {

    enum Status {
        NO_UPDATE,
        MANUAL_UPDATE,
        CHECK_FAILED,
        UNSUPPORTED_SERVER;

        public boolean hasNotification() {
            return this == MANUAL_UPDATE
                    || this == UNSUPPORTED_SERVER;
        }
    }

    /**
     * Gets an update result that indicates that the update failed.
     *
     * @return The update result.
     */
    static UpdateResult failed() {
        try {
            return new UpdateResult(Status.CHECK_FAILED, new UpdateCheckResult(new JsonObject()));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private final Status status;
    private final UpdateCheckResult checkResult;

    UpdateResult(Status status, UpdateCheckResult checkResult) {
        this.status = Preconditions.checkNotNull(status);
        this.checkResult = Preconditions.checkNotNull(checkResult);
    }

    /**
     * Gets the status of the update notification.
     * 
     * @return The status.
     */
    Status getStatus() {
        return status;
    }

    /**
     * Gets the result from the update check.
     *
     * @return The result.
     */
    UpdateCheckResult getUpdateCheckResult() {
        return checkResult;
    }

    /**
     * Gets whether this update result has a notification that should be shown.
     * 
     * @return True if this update result has a notification, false otherwise.
     */
    boolean hasNotification() {
        return status.hasNotification();
    }

}
