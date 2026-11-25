import { useState } from "react";
import Button from "../../../components/ui/Button";
import MutualConnectionsPanel from "../../connections/components/MutualConnectionsPanel";
import {
  acceptConnectionRequest,
  cancelOrRemoveConnection,
  rejectConnectionRequest,
  sendConnectionRequest,
} from "../../connections/services/connectionService";
import ReferralRequestForm from "../../referrals/components/ReferralRequestForm";

const statusBadgeConfig = {
  ACCEPTED: {
    label: "Connected",
    className: "border-green-200 bg-green-50 text-green-700",
  },
  PENDING_SENT: {
    label: "Request Sent",
    className: "border-amber-200 bg-amber-50 text-amber-700",
  },
  PENDING_RECEIVED: {
    label: "Requested You",
    className: "border-blue-200 bg-blue-50 text-blue-700",
  },
  REJECTED: {
    label: "Declined",
    className: "border-gray-200 bg-gray-100 text-gray-600",
  },
};

const SearchResultCard = ({ user, onUserUpdated }) => {
  const [connectionLoading, setConnectionLoading] = useState(false);
  const [connectionMessage, setConnectionMessage] = useState("");
  const [connectionError, setConnectionError] = useState("");
  const [isReferralFormOpen, setIsReferralFormOpen] = useState(false);
  const [referralMessage, setReferralMessage] = useState("");

  const status = user.connectionStatus || "NONE";
  const badge = statusBadgeConfig[status];
  const displayName = user.fullName || `@${user.username}`;

  const handleSendOrRetry = async () => {
    try {
      setConnectionLoading(true);
      setConnectionError("");
      setConnectionMessage("");

      const created = await sendConnectionRequest(user.id);
      onUserUpdated?.(user.id, {
        connectionStatus: "PENDING_SENT",
        connectionRequestId: created?.id ?? null,
      });
      setConnectionMessage("Connection request sent successfully.");
    } catch (error) {
      setConnectionError(error.message || "Failed to send connection request");
    } finally {
      setConnectionLoading(false);
    }
  };

  const handleCancelRequest = async () => {
    if (!user.connectionRequestId) return;

    try {
      setConnectionLoading(true);
      setConnectionError("");
      setConnectionMessage("");

      await cancelOrRemoveConnection(user.connectionRequestId);
      onUserUpdated?.(user.id, {
        connectionStatus: "NONE",
        connectionRequestId: null,
      });
      setConnectionMessage("Connection request cancelled.");
    } catch (error) {
      setConnectionError(error.message || "Failed to cancel connection request");
    } finally {
      setConnectionLoading(false);
    }
  };

  const handleRespond = async (action) => {
    if (!user.connectionRequestId) return;

    try {
      setConnectionLoading(true);
      setConnectionError("");
      setConnectionMessage("");

      if (action === "accept") {
        await acceptConnectionRequest(user.connectionRequestId);
        onUserUpdated?.(user.id, { connectionStatus: "ACCEPTED" });
        setConnectionMessage("Connection accepted.");
      } else {
        await rejectConnectionRequest(user.connectionRequestId);
        onUserUpdated?.(user.id, { connectionStatus: "REJECTED" });
        setConnectionMessage("Connection request declined.");
      }
    } catch (error) {
      setConnectionError(error.message || "Failed to update connection request");
    } finally {
      setConnectionLoading(false);
    }
  };

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
        <div className="space-y-1">
          <p className="text-base font-semibold text-gray-900">{displayName}</p>
          {user.fullName && user.username && (
            <p className="text-xs text-gray-400">@{user.username}</p>
          )}
          <p className="text-sm text-gray-700">{user.headline || "No headline added"}</p>
          <p className="text-sm text-gray-500">{user.company || "No company added"}</p>
          <p className="text-sm text-gray-500">{user.skills || "No skills added"}</p>
        </div>

        {badge && (
          <span
            className={`inline-flex w-fit rounded-full border px-3 py-1 text-xs font-medium ${badge.className}`}
          >
            {badge.label}
          </span>
        )}
      </div>

      {(connectionError || connectionMessage || referralMessage) && (
        <div className="mt-4 space-y-2">
          {connectionError && (
            <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">
              {connectionError}
            </p>
          )}
          {connectionMessage && (
            <p className="rounded-md border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">
              {connectionMessage}
            </p>
          )}
          {referralMessage && (
            <p className="rounded-md border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">
              {referralMessage}
            </p>
          )}
        </div>
      )}

      <div className="mt-4 flex flex-wrap gap-3">
        {(status === "NONE" || status === "REJECTED") && (
          <Button
            type="button"
            disabled={connectionLoading}
            onClick={handleSendOrRetry}
          >
            {connectionLoading
              ? "Sending..."
              : status === "REJECTED"
              ? "Connect Again"
              : "Connect"}
          </Button>
        )}

        {status === "PENDING_SENT" && user.connectionRequestId && (
          <Button
            type="button"
            disabled={connectionLoading}
            onClick={handleCancelRequest}
            className="bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
          >
            {connectionLoading ? "Cancelling..." : "Cancel Request"}
          </Button>
        )}

        {status === "PENDING_RECEIVED" && user.connectionRequestId && (
          <>
            <Button
              type="button"
              disabled={connectionLoading}
              onClick={() => handleRespond("accept")}
            >
              {connectionLoading ? "Saving..." : "Accept"}
            </Button>
            <Button
              type="button"
              disabled={connectionLoading}
              onClick={() => handleRespond("reject")}
              className="bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
            >
              Reject
            </Button>
          </>
        )}

        {status === "ACCEPTED" && (
          <Button
            type="button"
            onClick={() => {
              setReferralMessage("");
              setIsReferralFormOpen((current) => !current);
            }}
          >
            {isReferralFormOpen ? "Cancel Referral" : "Request Referral"}
          </Button>
        )}

        <MutualConnectionsPanel userId={user.id} />
      </div>

      {status === "ACCEPTED" && isReferralFormOpen && (
        <ReferralRequestForm
          receiverId={user.id}
          defaultCompany={user.company || ""}
          onSuccess={(message) => {
            setReferralMessage(message);
            setIsReferralFormOpen(false);
          }}
          onCancel={() => setIsReferralFormOpen(false)}
        />
      )}
    </div>
  );
};

export default SearchResultCard;
