import { useEffect, useState } from "react";
import PageHeader from "../../../components/layout/PageHeader";
import PageLayout from "../../../components/layout/PageLayout";
import Pagination from "../../../components/ui/Pagination";
import Input from "../../../components/ui/Input";
import StatusMessage from "../../../components/ui/StatusMessage";
import usePagination from "../../../hooks/usePagination";
import SearchResultCard from "../components/SearchResultCard";
import { searchUsers } from "../services/searchService";

const SearchPage = () => {
  const [filters, setFilters] = useState({
    name: "",
    headline: "",
    company: "",
    skills: "",
  });
  const [debouncedFilters, setDebouncedFilters] = useState(filters);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const {
    page,
    size,
    totalPages,
    setPage,
    setTotalPages,
    resetPage,
  } = usePagination(10);

  useEffect(() => {
    const timerId = setTimeout(() => {
      setDebouncedFilters(filters);
    }, 250);

    return () => clearTimeout(timerId);
  }, [filters]);

  useEffect(() => {
    const controller = new AbortController();

    const fetchUsers = async () => {
      try {
        setLoading(true);
        setError("");

        const data = await searchUsers({
          page,
          size,
          ...debouncedFilters,
          signal: controller.signal,
        });

        setResults(data.content || []);
        setTotalPages(data.totalPages || 0);
      } catch (err) {
        if (err.name === "CanceledError" || err.code === "ERR_CANCELED") {
          return;
        }
        setError(err.message || "Failed to fetch users");
        setResults([]);
        setTotalPages(0);
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    };

    fetchUsers();

    return () => controller.abort();
  }, [debouncedFilters, page, setTotalPages, size]);

  const handleFilterChange = (event) => {
    const { name, value } = event.target;

    setFilters((currentFilters) => ({
      ...currentFilters,
      [name]: value,
    }));
    resetPage();
  };

  const handleUserUpdated = (userId, updates) => {
    setResults((currentResults) =>
      currentResults.map((item) =>
        item.id === userId ? { ...item, ...updates } : item
      )
    );
  };

  return (
    <PageLayout maxWidth="max-w-5xl" contentClassName="space-y-6">
        <PageHeader
          title="Search Users"
          description="Find professionals by name, headline, company, or skills."
        />

        <div className="rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
          <div className="grid gap-4 md:grid-cols-2">
            <Input
              label="Name"
              name="name"
              value={filters.name}
              onChange={handleFilterChange}
              placeholder="Search by name or username"
            />

            <Input
              label="Headline"
              name="headline"
              value={filters.headline}
              onChange={handleFilterChange}
              placeholder="Filter by headline"
            />

            <Input
              label="Company"
              name="company"
              value={filters.company}
              onChange={handleFilterChange}
              placeholder="Filter by company"
            />

            <Input
              label="Skills"
              name="skills"
              value={filters.skills}
              onChange={handleFilterChange}
              placeholder="Filter by skills"
            />
          </div>
        </div>

        <div className="space-y-4">
          {loading && <StatusMessage variant="info">Loading users...</StatusMessage>}
          {error && <StatusMessage variant="error">{error}</StatusMessage>}
          {!loading && !error && results.length === 0 && (
            <StatusMessage variant="empty">No users found.</StatusMessage>
          )}

          {results.map((user) => (
            <SearchResultCard
              key={user.id}
              user={user}
              onUserUpdated={handleUserUpdated}
            />
          ))}
        </div>

        <Pagination
          page={page}
          totalPages={totalPages}
          onPageChange={setPage}
        />
    </PageLayout>
  );
};

export default SearchPage;
