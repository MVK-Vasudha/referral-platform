import { useCallback, useEffect, useState } from "react";
import PageHeader from "../../../components/layout/PageHeader";
import PageLayout from "../../../components/layout/PageLayout";
import Button from "../../../components/ui/Button";
import Pagination from "../../../components/ui/Pagination";
import StatusMessage from "../../../components/ui/StatusMessage";
import usePagination from "../../../hooks/usePagination";
import ReferralRequestForm from "../../referrals/components/ReferralRequestForm";
import MutualConnectionsPanel from "../components/MutualConnectionsPanel";
import {
  acceptConnectionRequest,
  cancelOrRemoveConnection,
  getMyConnections,
  getPendingConnections,
  getSentPendingConnections,
  rejectConnectionRequest,
} from "../services/connectionService";

const formatDate = (value) => {
  if (!value) {
    return "Date unavailable";
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return "Date unavailable";
  }

  return date.toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
};

const ConnectionCard = ({
  connection,
  subtitle,
  footer,
  actions = null,
  extra = null,
}) => {
  return (
    <div className="rounded-lg border border-gray-200 bg-white p-4 shadow-sm">
      <div className="space-y-1">
        <p className="text-base font-semibold text-gray-900">
          {connection.fullName || (connection.username ? `@${connection.username}` : "Unnamed user")}
        </p>
        {connection.fullName && connection.username && (
          <p className="text-xs text-gray-400">@{connection.username}</p>
        )}
        <p className="text-sm text-gray-700">
          {connection.headline || "No headline added"}
        </p>
        <p className="text-sm text-gray-500">
          {connection.company || "No company added"}
        </p>
        {subtitle && <p className="text-sm text-gray-500">{subtitle}</p>}
        {footer && <p className="text-xs text-gray-400">{footer}</p>}
      </div>

      {actions && <div className="mt-4 flex flex-wrap gap-3">{actions}</div>}
      {extra}
    </div>
  );
};

const SectionState = ({ loading, error, emptyMessage, hasItems, loadingMessage, children }) => {
  if (loading) {
    return <StatusMessage variant="info">{loadingMessage}</StatusMessage>;
  }

  if (error) {
    return <StatusMessage variant="error">{error}</StatusMessage>;
  }

  if (!hasItems) {
    return <StatusMessage variant="empty">{emptyMessage}</StatusMessage>;
  }

  return children;
};

const ConnectionsPage = () => {
  const [connections, setConnections] = useState([]);
  const [pendingRequests, setPendingRequests] = useState([]);
  const [sentRequests, setSentRequests] = useState([]);
  const [connectionsLoading, setConnectionsLoading] = useState(true);
  const [pendingLoading, setPendingLoading] = useState(true);
  const [sentLoading, setSentLoading] = useState(true);
  const [connectionsError, setConnectionsError] = useState("");
  const [pendingError, setPendingError] = useState("");
  const [sentError, setSentError] = useState("");
  const [actionError, setActionError] = useState("");
  const [actionSuccess, setActionSuccess] = useState("");
  const [activeRequestId, setActiveRequestId] = useState(null);
  const [referralTargetUserId, setReferralTargetUserId] = useState(null);

  const connectionsPagination = usePagination(10);
  const pendingPagination = usePagination(10);
  const sentPagination = usePagination(10);

  const {
    page: connectionsPage,
    size: connectionsSize,
    totalPages: connectionsTotalPages,
    setPage: setConnectionsPage,
    setTotalPages: setConnectionsTotalPages,
  } = connectionsPagination;
  const {
    page: pendingPage,
    size: pendingSize,
    totalPages: pendingTotalPages,
    setPage: setPendingPage,
    setTotalPages: setPendingTotalPages,
  } = pendingPagination;
  const {
    page: sentPage,
    size: sentSize,
    totalPages: sentTotalPages,
    setPage: setSentPage,
    setTotalPages: setSentTotalPages,
  } = sentPagination;

  const loadConnections = useCallback(async () => {
    try {
      setConnectionsLoading(true);
      setConnectionsError("");

      const data = await getMyConnections({
        page: connectionsPage,
        size: connectionsSize,
      });

      setConnections(data.content || []);
      setTotalPagesSafe(setConnectionsTotalPages, data.totalPages);
    } catch (error) {
      setConnections([]);
      setConnectionsTotalPages(0);
      setConnectionsError(error.message || "Failed to load connections");
    } finally {
      setConnectionsLoading(false);
    }
  }, [connectionsPage, connectionsSize, setConnectionsTotalPages]);

  const loadPendingRequests = useCallback(async () => {
    try {
      setPendingLoading(true);
      setPendingError("");

      const data = await getPendingConnections({
        page: pendingPage,
        size: pendingSize,
      });

      setPendingRequests(data.content || []);
      setTotalPagesSafe(setPendingTotalPages, data.totalPages);
    } catch (error) {
      setPendingRequests([]);
      setPendingTotalPages(0);
      setPendingError(error.message || "Failed to load pending requests");
    } finally {
      setPendingLoading(false);
    }
  }, [pendingPage, pendingSize, setPendingTotalPages]);

  const loadSentRequests = useCallback(async () => {
    try {
      setSentLoading(true);
      setSentError("");

      const data = await getSentPendingConnections({
        page: sentPage,
        size: sentSize,
      });

      setSentRequests(data.content || []);
      setTotalPagesSafe(setSentTotalPages, data.totalPages);
    } catch (error) {
      setSentRequests([]);
      setSentTotalPages(0);
      setSentError(error.message || "Failed to load sent requests");
    } finally {
      setSentLoading(false);
    }
  }, [sentPage, sentSize, setSentTotalPages]);

  useEffect(() => {
    loadConnections();
  }, [loadConnections]);

  useEffect(() => {
    loadPendingRequests();
  }, [loadPendingRequests]);

  useEffect(() => {
    loadSentRequests();
  }, [loadSentRequests]);

  const handleRequestAction = async (requestId, action) => {
    try {
      setActiveRequestId(requestId);
      setActionError("");
      setActionSuccess("");

      if (action === "accept") {
        await acceptConnectionRequest(requestId);
        setActionSuccess("Connection accepted.");
      } else {
        await rejectConnectionRequest(requestId);
        setActionSuccess("Connection request rejected.");
      }

      if (pendingRequests.length === 1 && pendingPage > 0) {
        setPendingPage(pendingPage - 1);
      } else {
        await loadPendingRequests();
      }
      await loadConnections();
    } catch (error) {
      setActionError(error.message || "Failed to update connection request");
    } finally {
      setActiveRequestId(null);
    }
  };

  const handleCancelOrRemove = async (requestId, kind) => {
    try {
      setActiveRequestId(requestId);
      setActionError("");
      setActionSuccess("");

      await cancelOrRemoveConnection(requestId);
      setActionSuccess(
        kind === "sent" ? "Connection request cancelled." : "Connection removed."
      );

      if (kind === "sent") {
        if (sentRequests.length === 1 && sentPage > 0) {
          setSentPage(sentPage - 1);
        } else {
          await loadSentRequests();
        }
      } else {
        if (connections.length === 1 && connectionsPage > 0) {
          setConnectionsPage(connectionsPage - 1);
        } else {
          await loadConnections();
        }
      }
    } catch (error) {
      setActionError(error.message || "Failed to update connection");
    } finally {
      setActiveRequestId(null);
    }
  };

  return (
    <PageLayout maxWidth="max-w-5xl" contentClassName="space-y-8">
        <PageHeader
          title="Connections"
          description="Review your network, manage pending requests, and request referrals from your connections."
        />

        {actionError && <StatusMessage variant="error">{actionError}</StatusMessage>}
        {actionSuccess && <StatusMessage variant="success">{actionSuccess}</StatusMessage>}

        <section className="space-y-4">
          <div className="space-y-1">
            <h2 className="text-xl font-semibold text-gray-900">Pending Requests</h2>
            <p className="text-sm text-gray-500">
              Accept or reject requests from other professionals.
            </p>
          </div>

          <SectionState
            loading={pendingLoading}
            loadingMessage="Loading pending requests..."
            error={pendingError}
            hasItems={pendingRequests.length > 0}
            emptyMessage="No pending connection requests."
          >
            <div className="grid gap-4">
              {pendingRequests.map((request) => (
                <ConnectionCard
                  key={request.id}
                  connection={request}
                  footer={`Requested on ${formatDate(request.connectedAt)}`}
                  actions={
                    <>
                      <Button
                        type="button"
                        disabled={activeRequestId === request.id}
                        onClick={() => handleRequestAction(request.id, "accept")}
                      >
                        {activeRequestId === request.id ? "Saving..." : "Accept"}
                      </Button>

                      <Button
                        type="button"
                        disabled={activeRequestId === request.id}
                        onClick={() => handleRequestAction(request.id, "reject")}
                        className="bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
                      >
                        Reject
                      </Button>

                      <MutualConnectionsPanel userId={request.userId} />
                    </>
                  }
                />
              ))}
            </div>
          </SectionState>

          <Pagination
            page={pendingPage}
            totalPages={pendingTotalPages}
            onPageChange={setPendingPage}
          />
        </section>

        <section className="space-y-4">
          <div className="space-y-1">
            <h2 className="text-xl font-semibold text-gray-900">Sent Requests</h2>
            <p className="text-sm text-gray-500">
              Connection requests you have sent that are awaiting a response.
            </p>
          </div>

          <SectionState
            loading={sentLoading}
            loadingMessage="Loading sent requests..."
            error={sentError}
            hasItems={sentRequests.length > 0}
            emptyMessage="No outgoing pending connection requests."
          >
            <div className="grid gap-4 md:grid-cols-2">
              {sentRequests.map((request) => (
                <ConnectionCard
                  key={request.id}
                  connection={request}
                  footer={`Sent on ${formatDate(request.connectedAt)}`}
                  actions={
                    <Button
                      type="button"
                      disabled={activeRequestId === request.id}
                      onClick={() => handleCancelOrRemove(request.id, "sent")}
                      className="bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
                    >
                      {activeRequestId === request.id ? "Cancelling..." : "Cancel Request"}
                    </Button>
                  }
                />
              ))}
            </div>
          </SectionState>

          <Pagination
            page={sentPage}
            totalPages={sentTotalPages}
            onPageChange={setSentPage}
          />
        </section>

        <section className="space-y-4">
          <div className="space-y-1">
            <h2 className="text-xl font-semibold text-gray-900">My Connections</h2>
            <p className="text-sm text-gray-500">
              People connected with your account. You can request referrals directly from them.
            </p>
          </div>

          <SectionState
            loading={connectionsLoading}
            loadingMessage="Loading connections..."
            error={connectionsError}
            hasItems={connections.length > 0}
            emptyMessage="You do not have any accepted connections yet."
          >
            <div className="grid gap-4 md:grid-cols-2">
              {connections.map((connection) => {
                const isReferralOpen = referralTargetUserId === connection.userId;

                return (
                  <ConnectionCard
                    key={`${connection.id}-${connection.userId}`}
                    connection={connection}
                    footer={`Connected on ${formatDate(connection.connectedAt)}`}
                    actions={
                      <>
                        <Button
                          type="button"
                          onClick={() =>
                            setReferralTargetUserId(isReferralOpen ? null : connection.userId)
                          }
                        >
                          {isReferralOpen ? "Cancel Referral" : "Request Referral"}
                        </Button>

                        <MutualConnectionsPanel userId={connection.userId} />

                        <Button
                          type="button"
                          disabled={activeRequestId === connection.id}
                          onClick={() => handleCancelOrRemove(connection.id, "accepted")}
                          className="bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
                        >
                          {activeRequestId === connection.id ? "Removing..." : "Remove"}
                        </Button>
                      </>
                    }
                    extra={
                      isReferralOpen && (
                        <ReferralRequestForm
                          receiverId={connection.userId}
                          defaultCompany={connection.company || ""}
                          onSuccess={(message) => {
                            setActionError("");
                            setActionSuccess(message);
                            setReferralTargetUserId(null);
                          }}
                          onCancel={() => setReferralTargetUserId(null)}
                        />
                      )
                    }
                  />
                );
              })}
            </div>
          </SectionState>

          <Pagination
            page={connectionsPage}
            totalPages={connectionsTotalPages}
            onPageChange={setConnectionsPage}
          />
        </section>
    </PageLayout>
  );
};

function setTotalPagesSafe(setter, totalPages) {
  setter(totalPages || 0);
}

export default ConnectionsPage;
