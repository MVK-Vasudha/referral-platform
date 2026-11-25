import { useState } from "react";
import Button from "../../../components/ui/Button";
import { getMutualConnections } from "../services/connectionService";

const MutualConnectionsPanel = ({ userId }) => {
  const [isOpen, setIsOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [mutuals, setMutuals] = useState([]);
  const [totalElements, setTotalElements] = useState(0);
  const [loaded, setLoaded] = useState(false);

  const handleToggle = async () => {
    const nextOpen = !isOpen;
    setIsOpen(nextOpen);

    if (!nextOpen || loaded) {
      return;
    }

    try {
      setLoading(true);
      setError("");
      const data = await getMutualConnections(userId, { page: 0, size: 10 });
      setMutuals(data.content || []);
      setTotalElements(data.totalElements ?? (data.content?.length || 0));
      setLoaded(true);
    } catch (err) {
      setError(err.message || "Failed to load mutual connections");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <Button
        type="button"
        onClick={handleToggle}
        className="bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
      >
        {isOpen ? "Hide Mutuals" : "Mutual Connections"}
      </Button>

      {isOpen && (
        <div className="mt-3 rounded-md border border-gray-200 bg-gray-50 p-3 text-sm">
          {loading && <p className="text-gray-600">Loading mutual connections...</p>}
          {error && <p className="text-red-600">{error}</p>}
          {!loading && !error && mutuals.length === 0 && (
            <p className="text-gray-500">No mutual connections.</p>
          )}
          {!loading && !error && mutuals.length > 0 && (
            <div className="space-y-1">
              <p className="font-medium text-gray-700">
                {totalElements} mutual connection{totalElements === 1 ? "" : "s"}
              </p>
              <ul className="space-y-1 text-gray-600">
                {mutuals.map((mutual) => (
                  <li key={mutual.userId}>
                    {mutual.fullName || `@${mutual.username}`}
                    {mutual.company ? ` · ${mutual.company}` : ""}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default MutualConnectionsPanel;
