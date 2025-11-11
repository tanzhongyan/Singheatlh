import { useState, useEffect, useCallback, useRef } from 'react';
import apiClient from '../api/apiClient';

/**
 * Custom hook for fetching admin data with caching and pagination support
 * @param {string} endpoint - The API endpoint to fetch from (e.g., '/api/system-administrators/users/paginated')
 * @param {number} initialPage - Initial page number (default: 1)
 * @param {number} pageSize - Number of items per page (default: 10)
 * @param {number} cacheTime - Cache duration in milliseconds (default: 5 minutes)
 */
export const useAdminData = (endpoint, initialPage = 1, pageSize = 10, cacheTime = 5 * 60 * 1000) => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [currentPage, setCurrentPage] = useState(initialPage);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterValue, setFilterValue] = useState('');

  // Cache store: key format is "endpoint:page:pageSize:search:filter"
  const cacheRef = useRef(new Map());
  const cacheTimestampRef = useRef(new Map());

  const getCacheKey = useCallback(
    (page, search, filter) => `${endpoint}:${page}:${pageSize}:${search}:${filter}`,
    [endpoint, pageSize]
  );

  const isCacheValid = useCallback((key) => {
    const timestamp = cacheTimestampRef.current.get(key);
    if (!timestamp) return false;
    return Date.now() - timestamp < cacheTime;
  }, [cacheTime]);

  const fetchData = useCallback(
    async (page = 1, search = '', filter = '') => {
      const cacheKey = getCacheKey(page, search, filter);

      // Check if data is in cache and valid
      if (cacheRef.current.has(cacheKey) && isCacheValid(cacheKey)) {
        setData(cacheRef.current.get(cacheKey));
        setLoading(false);
        return;
      }

      setLoading(true);
      setError(null);

      try {
        // Build query parameters
        const params = new URLSearchParams({
          page,
          pageSize,
          ...(search && { search }),
          ...(filter && { role: filter }), // for users endpoint
        });

        const response = await apiClient.get(`${endpoint}?${params.toString()}`);
        const result = response.data;

        // Store in cache
        cacheRef.current.set(cacheKey, result);
        cacheTimestampRef.current.set(cacheKey, Date.now());

        setData(result);
      } catch (err) {
        setError(err.message || 'Failed to fetch data');
      } finally {
        setLoading(false);
      }
    },
    [endpoint, pageSize, getCacheKey, isCacheValid]
  );

  // Fetch data when dependencies change
  useEffect(() => {
    fetchData(currentPage, searchTerm, filterValue);
  }, [currentPage, searchTerm, filterValue, fetchData]);

  // Debounced search handler
  const handleSearch = useCallback((term) => {
    setSearchTerm(term);
    setCurrentPage(1); // Reset to first page on search
  }, []);

  // Filter handler
  const handleFilter = useCallback((filter) => {
    setFilterValue(filter);
    setCurrentPage(1); // Reset to first page on filter
  }, []);

  // Manual refetch function
  const refetch = useCallback(() => {
    const cacheKey = getCacheKey(currentPage, searchTerm, filterValue);
    cacheRef.current.delete(cacheKey);
    cacheTimestampRef.current.delete(cacheKey);
    fetchData(currentPage, searchTerm, filterValue);
  }, [currentPage, searchTerm, filterValue, fetchData, getCacheKey]);

  // Clear all cache
  const clearCache = useCallback(() => {
    cacheRef.current.clear();
    cacheTimestampRef.current.clear();
  }, []);

  return {
    data,
    loading,
    error,
    currentPage,
    setCurrentPage,
    searchTerm,
    setSearchTerm: handleSearch,
    filterValue,
    setFilterValue: handleFilter,
    refetch,
    clearCache,
  };
};

export default useAdminData;
