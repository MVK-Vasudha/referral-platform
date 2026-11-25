
import { useState } from "react";

const usePagination = (initialSize = 10) => {
  const [page, setPage] = useState(0);        // ZERO-BASED (Spring aligned)
  const [totalPages, setTotalPages] = useState(0);
  const [size] = useState(initialSize);       // fixed size

  const resetPage = () => setPage(0);

  return {
    page,
    size,
    totalPages,
    setPage,
    setTotalPages,
    resetPage,
  };
};

export default usePagination;