import { useCallback, useEffect, useState } from "react";
import PageHeader from "../../../components/layout/PageHeader";
import PageLayout from "../../../components/layout/PageLayout";
import Button from "../../../components/ui/Button";
import Pagination from "../../../components/ui/Pagination";
import StatusMessage from "../../../components/ui/StatusMessage";
import usePagination from "../../../hooks/usePagination";
import {
  acceptReferral,
  getReceivedReferrals,
  getSentReferrals,
  rejectReferral,
} from "../services/referralService";

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

const formatStatus = (status) => {
  if (!status) {
    return "Unknown";
  }

  return status.charAt(0) + status.slice(1).toLowerCase();
};

const statusClassNames = {
  PENDING: "border-amber-200 bg-amber-50 text-amber-700",
  ACCEPTED: "border-green-200 bg-green-50 text-green-700",
  REJECTED: "border-red-200 bg-red-50 text-red-700",
};

const ReferralCard = ({ referral, audience, actions = null }) => {
  const statusClasses =
    statusClassNames[referral.status] || "border-gray-200 bg-gray-50 text-gray-700";

  return (
    <div className="rounded-lg border border-gray-200 bg-white p-5 shadow-sm">
      <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div className="space-y-2">
          <div className="space-y-1">
            <p className="text-base font-semibold text-gray-900">{referral.jobTitle}</p>
            <p className="text-sm text-gray-700">{referral.company}</p>
            <p className="text-sm text-gray-500">
              {audience === "sent"
                ? `Requested from @${referral.receiverUsername}`
                : `Requested by @${referral.requesterUsername}`}
            </p>
          </div>

          <div className="space-y-1 text-sm text-gray-600">
            <p>Created on {formatDate(referral.createdAt)}</p>
            <p>Last updated on {formatDate(referral.updatedAt)}</p>
            {referral.message && <p>{referral.message}</p>}
            {referral.resumeLink && (
              <p>
                Resume:{" "}
                <a
                  href={referral.resumeLink}
                  target="_blank"
                  rel="noreferrer"
                  className="text-gray-900 underline"
                >
                  Open link
                </a>
              </p>
            )}
          </div>
        </div>

        <span
          className={`inline-flex w-fit rounded-full border px-3 py-1 text-xs font-medium ${statusClasses}`}
        >
          {formatStatus(referral.status)}
        </span>
      </div>

      {actions && <div className="mt-4 flex gap-3">{actions}</div>}
    </div>
  );
};

const SectionState = ({ loading, error, hasItems, emptyMessage, loadingMessage, children }) => {
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

const ReferralsPage = () => {
  const [sentReferrals, setSentReferrals] = useState([]);
  const [receivedReferrals, setReceivedReferrals] = useState([]);
  const [sentLoading, setSentLoading] = useState(true);
  const [receivedLoading, setReceivedLoading] = useState(true);
  const [sentError, setSentError] = useState("");
  const [receivedError, setReceivedError] = useState("");
  const [actionError, setActionError] = useState("");
  const [activeReferralId, setActiveReferralId] = useState(null);

  const sentPagination = usePagination(10);
  const receivedPagination = usePagination(10);
  const {
    page: sentPage,
    size: sentSize,
    totalPages: sentTotalPages,
    setPage: setSentPage,
    setTotalPages: setSentTotalPages,
  } = sentPagination;
  const {
    page: receivedPage,
    size: receivedSize,
    totalPages: receivedTotalPages,
    setPage: setReceivedPage,
    setTotalPages: setReceivedTotalPages,
  } = receivedPagination;

  const loadSentReferrals = useCallback(async () => {
    try {
      setSentLoading(true);
      setSentError("");

      const data = await getSentReferrals({
        page: sentPage,
        size: sentSize,
      });

      setSentReferrals(data.content || []);
      setSentTotalPages(data.totalPages || 0);
    } catch (error) {
      setSentReferrals([]);
      setSentTotalPages(0);
      setSentError(error.message || "Failed to load sent referrals");
    } finally {
      setSentLoading(false);
    }
  }, [sentPage, sentSize, setSentTotalPages]);

  const loadReceivedReferrals = useCallback(async () => {
    try {
      setReceivedLoading(true);
      setReceivedError("");

      const data = await getReceivedReferrals({
        page: receivedPage,
        size: receivedSize,
      });

      setReceivedReferrals(data.content || []);
      setReceivedTotalPages(data.totalPages || 0);
    } catch (error) {
      setReceivedReferrals([]);
      setReceivedTotalPages(0);
      setReceivedError(error.message || "Failed to load received referrals");
    } finally {
      setReceivedLoading(false);
    }
  }, [receivedPage, receivedSize, setReceivedTotalPages]);

  useEffect(() => {
    loadSentReferrals();
  }, [loadSentReferrals]);

  useEffect(() => {
    loadReceivedReferrals();
  }, [loadReceivedReferrals]);

  const handleReferralAction = async (referralId, action) => {
    try {
      setActiveReferralId(referralId);
      setActionError("");

      if (action === "accept") {
        await acceptReferral(referralId);
      } else {
        await rejectReferral(referralId);
      }

      if (receivedReferrals.length === 1 && receivedPage > 0) {
        setReceivedPage(receivedPage - 1);
      } else {
        await loadReceivedReferrals();
      }
      await loadSentReferrals();
    } catch (error) {
      setActionError(error.message || "Failed to update referral");
    } finally {
      setActiveReferralId(null);
    }
  };

  return (
    <PageLayout maxWidth="max-w-5xl" contentClassName="space-y-8">
        <PageHeader
          title="Referrals"
          description="Track the referral requests you have sent and manage the ones you receive."
        />

        {actionError && <StatusMessage variant="error">{actionError}</StatusMessage>}

        <section className="space-y-4">
          <div className="space-y-1">
            <h2 className="text-xl font-semibold text-gray-900">Received Referrals</h2>
            <p className="text-sm text-gray-500">
              Respond to incoming referral requests from other users.
            </p>
          </div>

          <SectionState
            loading={receivedLoading}
            loadingMessage="Loading received referrals..."
            error={receivedError}
            hasItems={receivedReferrals.length > 0}
            emptyMessage="You have not received any referral requests yet."
          >
            <div className="grid gap-4">
              {receivedReferrals.map((referral) => (
                <ReferralCard
                  key={referral.id}
                  referral={referral}
                  audience="received"
                  actions={
                    referral.status === "PENDING" ? (
                      <>
                        <Button
                          type="button"
                          disabled={activeReferralId === referral.id}
                          onClick={() => handleReferralAction(referral.id, "accept")}
                        >
                          {activeReferralId === referral.id ? "Saving..." : "Accept"}
                        </Button>

                        <Button
                          type="button"
                          disabled={activeReferralId === referral.id}
                          onClick={() => handleReferralAction(referral.id, "reject")}
                          className="bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
                        >
                          Reject
                        </Button>
                      </>
                    ) : null
                  }
                />
              ))}
            </div>
          </SectionState>

          <Pagination
            page={receivedPage}
            totalPages={receivedTotalPages}
            onPageChange={setReceivedPage}
          />
        </section>

        <section className="space-y-4">
          <div className="space-y-1">
            <h2 className="text-xl font-semibold text-gray-900">Sent Referrals</h2>
            <p className="text-sm text-gray-500">
              Monitor the referral requests you have already submitted.
            </p>
          </div>

          <SectionState
            loading={sentLoading}
            loadingMessage="Loading sent referrals..."
            error={sentError}
            hasItems={sentReferrals.length > 0}
            emptyMessage="You have not sent any referral requests yet."
          >
            <div className="grid gap-4">
              {sentReferrals.map((referral) => (
                <ReferralCard
                  key={referral.id}
                  referral={referral}
                  audience="sent"
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
    </PageLayout>
  );
};

export default ReferralsPage;
