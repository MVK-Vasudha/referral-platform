import Button from "./Button";

const Pagination = ({ page, totalPages, onPageChange }) => {
  if (totalPages <= 1) return null;

  return (
    <div className="mt-6 flex items-center justify-between gap-4 rounded-lg border border-gray-200 bg-white px-4 py-3 shadow-sm">
      <Button
        type="button"
        disabled={page === 0}
        onClick={() => onPageChange(page - 1)}
        className="bg-white text-gray-900 border border-gray-300 hover:bg-gray-50"
      >
        Previous
      </Button>

      <span className="text-sm text-gray-600">
        Page {page + 1} of {totalPages}
      </span>

      <Button
        type="button"
        disabled={page + 1 >= totalPages}
        onClick={() => onPageChange(page + 1)}
      >
        Next
      </Button>
    </div>
  );
};

export default Pagination;
